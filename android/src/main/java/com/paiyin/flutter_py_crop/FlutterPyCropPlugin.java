package com.paiyin.flutter_py_crop;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;

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
public class FlutterPyCropPlugin  implements FlutterPlugin, MethodCallHandler, ActivityAware, PluginRegistry.ActivityResultListener {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private MethodChannel channel;
  private Activity activity;
  private ActivityPluginBinding activityPluginBinding;
  private Result methodResult;

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "android_py_crop");
    channel.setMethodCallHandler(this);
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    this.methodResult = result;
    if (call.method.equals("cropImage")) {
      String sourcePath = call.argument("source_path");
      Uri sourceUri = Uri.fromFile(new File(sourcePath));
      beginCrop(sourceUri);
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
    }
    return false;
  }

  private void beginCrop(Uri source) {
    Uri destination = Uri.fromFile(new File(activity.getCacheDir(), "cropped" + (new Date()).getTime()));
    Crop.of(source, destination).asSquare().withMaxSize(30, 30).start(activity);
  }
}
