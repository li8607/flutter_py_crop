import 'dart:io';
import 'package:flutter/material.dart';
import 'package:flutter_py_crop/flutter_py_crop.dart';
import 'package:image_picker/image_picker.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';

  File? file;
  @override
  void initState() {
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: GestureDetector(
            onTap: () async {
              // final PickedFile? pickedFile =
              //     await ImagePicker().getImage(source: ImageSource.gallery);
              // if (pickedFile != null) {
              //   file =
              //       await FlutterPyCrop.cropImage(sourcePath: pickedFile.path);
              //   print('result = ${file?.path}');
              // }
              file = await FlutterPyCrop.pickAndCropImage();
              setState(() {});
            },
            child: Column(
              children: [
                if (file != null)
                  Image.file(
                    file!,
                    key: ValueKey(file!),
                  ),
                Text('Running on: $_platformVersion\n'),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
