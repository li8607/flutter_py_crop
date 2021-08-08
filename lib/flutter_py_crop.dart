
import 'dart:async';
import 'dart:io';

import 'package:flutter/services.dart';

class FlutterPyCrop {
  static const MethodChannel _channel =
      const MethodChannel('flutter_py_crop');

static Future<File?> cropImage({required String sourcePath}) async {
    assert(await File(sourcePath).exists());
    final arguments = <String, dynamic>{
      'source_path': sourcePath,
    };
    try {
      if(!Platform.isAndroid) {
        return null;
      }
      final String? resultPath =
          await _channel.invokeMethod('cropImage', arguments);
      return resultPath == null ? null : new File(resultPath);
    } catch (e) {}
    return null;
  }


static Future<File?> pickAndCropImage() async {
    try {
      if(!Platform.isAndroid) {
        return null;
      }
      final String? resultPath =
          await _channel.invokeMethod('pickAndCropImage');
      return resultPath == null ? null : new File(resultPath);
    } catch (e) {}
    return null;
  }
}
