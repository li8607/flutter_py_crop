package com.paiyin.flutter_py_crop;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.soundcloud.android.crop.Crop;

import java.io.File;
import java.util.Date;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;

/** FlutterPyCropPlugin */
public class FlutterPyCropPlugin  implements FlutterPlugin, MethodCallHandler, ActivityAware, PluginRegistry.ActivityResultListener, PluginRegistry.RequestPermissionsResultListener {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private MethodChannel channel;
  private Activity activity;
  private ActivityPluginBinding activityPluginBinding;
  private Result methodResult;


  static final int RESULT_CODE_STARTCAMERA = 2345;

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "flutter_py_crop");
    channel.setMethodCallHandler(this);
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    this.methodResult = result;
    if (call.method.equals("cropImage")) {
      String sourcePath = call.argument("source_path");
      Uri sourceUri = Uri.fromFile(new File(sourcePath));
      beginCrop(sourceUri);
    }else if (call.method.equals("pickAndCropImage")) {
      pickImage();
    } else {
      result.success(null);
    }
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    channel.setMethodCallHandler(null);
  }

  @Override
  public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
    activityPluginBinding = binding;
    activity = binding.getActivity();
    binding.addActivityResultListener(this);
    binding.addRequestPermissionsResultListener(this);
  }

  @Override
  public void onDetachedFromActivityForConfigChanges() {

  }

  @Override
  public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {

  }

  @Override
  public void onDetachedFromActivity() {
    activityPluginBinding.removeActivityResultListener(this);
    activityPluginBinding.removeRequestPermissionsResultListener(this);
  }

  @Override
  public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == Crop.REQUEST_CROP) {
      if (resultCode == Activity.RESULT_OK) {
        FileUtils fileUtils = new FileUtils();
        methodResult.success(fileUtils.getPathFromUri(activity, Crop.getOutput(data)));
        return true;
      } else if (resultCode == Crop.RESULT_ERROR) {
        final Throwable cropError = Crop.getError(data);
        methodResult.error("crop_error", cropError.getLocalizedMessage(), cropError);
        return true;
      } else {
        methodResult.success(null);
        return true;
      }
    }else if (requestCode == Crop.REQUEST_PICK && resultCode == Activity.RESULT_OK) {
      beginCrop(data.getData());
      return true;
    }
      return false;
  }

  @Override
  public boolean onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    switch(requestCode){
      case RESULT_CODE_STARTCAMERA:
        boolean cameraAccepted = grantResults[0]==PackageManager.PERMISSION_GRANTED;
        if(cameraAccepted){
          //授权成功之后，调用系统相机进行拍照操作等
          Crop.pickImage(activity);
        }else{
          //用户授权拒绝之后，友情提示一下就可以了
//          ToastUtil.show(context,"请开启应用拍照权限");
        }
        return true;
    }
    return false;
  }

  private void pickImage() {
    //判断是否开户相册权限
    if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(activity, android.Manifest.permission.CAMERA)) {
      Crop.pickImage(activity);
      Log.e("limf", "sdlfjalsdfj");
    }else{
      Log.e("limf", "2222");
      //提示用户开户权限
      ActivityCompat.requestPermissions(activity, new String[] {Manifest.permission.CAMERA}, RESULT_CODE_STARTCAMERA);
    }
  }

  private void beginCrop(Uri source) {
    Uri destination = Uri.fromFile(new File(activity.getCacheDir(), "cropped" + (new Date()).getTime()));
    Crop.of(source, destination).asSquare().start(activity);
  }
}
