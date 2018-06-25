对 TensorFlow Lite 库的二次封装，使得项目中使用 TensorFlow Lite 更加的方便。


- 分类

    创建：

    ```html
    TensorFlowClassifyConfig.Builder builder = new TensorFlowClassifyConfig.Builder()
                    .setContext(this)
                    .setFileSource(FileSource.ASSERT)
                    .setInputSize(224, 224)
                    .setLabelFilePath("file:///android_asset/classify_labels.txt")
                    .setModelFilePath("file:///android_asset/mobilenet_quant_v1_224.tflite");
     Classifier classify = TFLiteImageClassifierQuant.create(builder.build());
    ```
    使用：
    ```html
    classify.recognizeImage(TFBitmapUtil.getClassifyBitmap(bitmap, 224, 224));
    ```
- 对象检测

    创建：
    ```html
     TensorFlowDetectConfig.Builder detectBuilder = new TensorFlowDetectConfig.Builder()
                    .setContext(this)
                    .setMaxDetectCount(200)
                    .setResultCount(1917)
                    .setInputSize(300)
                    .setMinConfidence(0.01f)
                    .setClassCount(91)
                    .setFileSource(FileSource.ASSERT)
                    .setModelFilePath("file:///android_asset/mobilenet_ssd.tflite")
                    .setBoxsFilePath("file:///android_asset/box_priors.txt")
                    .setLabelsFilePath("file:///android_asset/coco_labels_list.txt");
            final Classifier detector = TFLiteObjectDetect.create(detectBuilder.build());
    ```
    使用：
    ```html
     detector.recognizeImage(bitmap);
    ```