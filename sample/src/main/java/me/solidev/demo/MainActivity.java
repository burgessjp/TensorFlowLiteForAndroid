package me.solidev.demo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.yanzhenjie.album.Action;
import com.yanzhenjie.album.Album;
import com.yanzhenjie.album.AlbumFile;

import java.util.ArrayList;
import java.util.List;

import me.solidev.tensorflow.lite.demo.R;
import me.solidev.tensorflow.lib.Classifier;
import me.solidev.tensorflow.lib.FileSource;
import me.solidev.tensorflow.lib.TFBitmapUtil;
import me.solidev.tensorflow.lib.TFLiteImageClassifierQuant;
import me.solidev.tensorflow.lib.TFLiteObjectDetect;
import me.solidev.tensorflow.lib.TensorFlowClassifyConfig;
import me.solidev.tensorflow.lib.TensorFlowDetectConfig;

public class MainActivity extends AppCompatActivity {

    private ImageView ivClassify;
    private TextView tvClassify;
    private ImageView ivDetect;
    private TextView tvDetect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ivClassify = findViewById(R.id.ivClassify);
        tvClassify = findViewById(R.id.tvClassify);
        ivDetect = findViewById(R.id.ivDetect);
        tvDetect = findViewById(R.id.tvDetect);
        TensorFlowClassifyConfig.Builder builder = new TensorFlowClassifyConfig.Builder()
                .setContext(this)
                .setFileSource(FileSource.ASSERT)
                .setInputSize(224, 224)
                .setLabelFilePath("file:///android_asset/classify_labels.txt")
                .setModelFilePath("file:///android_asset/mobilenet_quant_v1_224.tflite");
        final Classifier classify = TFLiteImageClassifierQuant.create(builder.build());


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
        findViewById(R.id.btnSelectClassify).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Album.image(MainActivity.this)
                        .singleChoice()
                        .camera(true)
                        .columnCount(3)
                        .afterFilterVisibility(true)
                        .onResult(new Action<ArrayList<AlbumFile>>() {
                            @Override
                            public void onAction(@NonNull ArrayList<AlbumFile> result) {

                                Bitmap source = BitmapFactory.decodeFile(result.get(0).getPath());
                                ivClassify.setImageBitmap(source);
                                StringBuilder str = new StringBuilder();
                                List<Classifier.Recognition> classifyResult = classify.recognizeImage(source);
                                for (Classifier.Recognition recognition : classifyResult) {
                                    str.append(recognition.toString()).append("\n\n");
                                }
                                tvClassify.setText(str.toString());
                            }
                        })
                        .start();

            }
        });

        findViewById(R.id.btnSelectDetect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Album.image(MainActivity.this)
                        .singleChoice()
                        .camera(true)
                        .columnCount(3)
                        .afterFilterVisibility(true)
                        .onResult(new Action<ArrayList<AlbumFile>>() {
                            @Override
                            public void onAction(@NonNull ArrayList<AlbumFile> result) {
                                Bitmap source = BitmapFactory.decodeFile(result.get(0).getPath());
                                ivDetect.setImageBitmap(source);
                                List<Classifier.Recognition> detectResult = detector.recognizeImage(source);
                                StringBuilder str = new StringBuilder();
                                for (Classifier.Recognition recognition : detectResult) {
                                    str.append(recognition.toString()).append("\n\n");
                                }
                                tvDetect.setText(str.toString());

                            }
                        })
                        .start();
            }
        });


    }
}
