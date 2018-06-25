/* Copyright 2016 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/

package me.solidev.tensorflow.lib;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * @author _SOLID
 */
public class TFLiteObjectDetect implements Classifier {

    private static final String TAG = "TFLiteObjectDetect";
    private static final float Y_SCALE = 10.0f;
    private static final float X_SCALE = 10.0f;
    private static final float H_SCALE = 5.0f;
    private static final float W_SCALE = 5.0f;
    private int inputSize;
    private float[][] boxPriors;
    private Vector<String> labels = new Vector<>();
    private int[] intValues;
    private float[][][] outputLocations;
    private float[][][] outputClasses;
    float[][][][] img;
    private Interpreter tfLite;
    private TensorFlowDetectConfig config;


    private TFLiteObjectDetect(TensorFlowDetectConfig config) {
        this.config = config;
    }

    public static Classifier create(TensorFlowDetectConfig config) {
        TFLiteObjectDetect detect = new TFLiteObjectDetect(config);
        detect.boxPriors = new float[4][config.getResultCount()];
        if (config.getFileSource() == FileSource.ASSERT) {
            try {
                detect.loadCoderOptions(config.getContext().getAssets(), config.getBoxsFilePath(), detect.boxPriors);
                InputStream labelsInput;
                String actualFilename = config.getLabelsFilePath().split("file:///android_asset/")[1];
                labelsInput = config.getContext().getAssets().open(actualFilename);
                BufferedReader br;
                br = new BufferedReader(new InputStreamReader(labelsInput));
                String line;
                while ((line = br.readLine()) != null) {
                    detect.labels.add(line);
                }
                br.close();

                detect.inputSize = config.getInputSize();
                detect.tfLite = new Interpreter(TensorFlowUtil.loadModelFile(config.getContext().getAssets(), config.getModelFilePath().split("file:///android_asset/")[1]));
                detect.img = new float[1][config.getInputSize()][config.getInputSize()][3];
                detect.intValues = new int[detect.inputSize * detect.inputSize];
                detect.outputLocations = new float[1][config.getResultCount()][4];
                detect.outputClasses = new float[1][config.getResultCount()][config.getClassCount()];
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return detect;
    }


    private float expit(final float x) {
        return (float) (1. / (1. + Math.exp(-x)));
    }

    private void loadCoderOptions(
            final AssetManager assetManager, final String locationFilename, final float[][] boxPriors)
            throws IOException {
        // Try to be intelligent about opening from assets or sdcard depending on prefix.
        final String assetPrefix = "file:///android_asset/";
        InputStream is;
        if (locationFilename.startsWith(assetPrefix)) {
            is = assetManager.open(locationFilename.split(assetPrefix, -1)[1]);
        } else {
            is = new FileInputStream(locationFilename);
        }

        final BufferedReader reader = new BufferedReader(new InputStreamReader(is));

        for (int lineNum = 0; lineNum < 4; ++lineNum) {
            String line = reader.readLine();
            final StringTokenizer st = new StringTokenizer(line, ", ");
            int priorIndex = 0;
            while (st.hasMoreTokens()) {
                final String token = st.nextToken();
                try {
                    final float number = Float.parseFloat(token);
                    boxPriors[lineNum][priorIndex++] = number;
                } catch (final NumberFormatException e) {
                    // Silently ignore.
                }
            }
            if (priorIndex != config.getResultCount()) {
                throw new RuntimeException(
                        "BoxPrior length mismatch: " + priorIndex + " vs " + config.getResultCount());
            }
        }
        TFLog.i(TAG, "Loaded box priors!");
        reader.close();
    }

    private void decodeCenterSizeBoxes(float[][][] predictions) {
        for (int i = 0; i < config.getResultCount(); ++i) {
            float yACtr = (boxPriors[0][i] + boxPriors[2][i]) / 2;
            float xACtr = (boxPriors[1][i] + boxPriors[3][i]) / 2;
            float ha = boxPriors[2][i] - boxPriors[0][i];
            float wa = boxPriors[3][i] - boxPriors[1][i];

            float ycenter = predictions[0][i][0] / Y_SCALE * ha + yACtr;
            float xcenter = predictions[0][i][1] / X_SCALE * wa + xACtr;
            float h = (float) Math.exp(predictions[0][i][2] / H_SCALE) * ha;
            float w = (float) Math.exp(predictions[0][i][3] / W_SCALE) * wa;
            float ymin = ycenter - h / 2.f;
            float xmin = xcenter - w / 2.f;
            float ymax = ycenter + h / 2.f;
            float xmax = xcenter + w / 2.f;

            predictions[0][i][0] = ymin;
            predictions[0][i][1] = xmin;
            predictions[0][i][2] = ymax;
            predictions[0][i][3] = xmax;
        }
    }


    @Override
    public List<Recognition> recognizeImage(Bitmap bitmap) {
        Matrix frameToCropMatrix = null;
        Matrix cropToFrameMatrix = null;
        if (bitmap.getWidth() != config.getInputSize() || bitmap.getHeight() != config.getInputSize()) {
            frameToCropMatrix = TFBitmapUtil.getTransformationMatrix(bitmap.getWidth(), bitmap.getHeight(),
                    config.getInputSize(), config.getInputSize(), false);
            cropToFrameMatrix = new Matrix();
            frameToCropMatrix.invert(cropToFrameMatrix);
            bitmap = TFBitmapUtil.getCropBitmap(bitmap, config.getInputSize(), frameToCropMatrix);
        }

        TFLog.i(TAG, "recognizeImage start");
        // Preprocess the image data from 0-255 int to normalized float based
        // on the provided parameters.
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        for (int i = 0; i < inputSize; ++i) {
            for (int j = 0; j < inputSize; ++j) {
                int pixel = intValues[j * inputSize + i];
                img[0][j][i][2] = (float) (pixel & 0xFF) / 128.0f - 1.0f;
                img[0][j][i][1] = (float) ((pixel >> 8) & 0xFF) / 128.0f - 1.0f;
                img[0][j][i][0] = (float) ((pixel >> 16) & 0xFF) / 128.0f - 1.0f;
            }
        }

        outputLocations = new float[1][config.getResultCount()][4];
        outputClasses = new float[1][config.getResultCount()][config.getClassCount()];

        Object[] inputArray = {img};
        Map<Integer, Object> outputMap = new HashMap<>(2);
        outputMap.put(0, outputLocations);
        outputMap.put(1, outputClasses);

        long startTime = System.currentTimeMillis();

        tfLite.runForMultipleInputsOutputs(inputArray, outputMap);

        TFLog.i(TAG, "recognizeImage end,time cost:" + (System.currentTimeMillis() - startTime));
        decodeCenterSizeBoxes(outputLocations);

        // Find the best detections.
        final PriorityQueue<Recognition> pq =
                new PriorityQueue<Recognition>(
                        1,
                        new Comparator<Recognition>() {
                            @Override
                            public int compare(final Recognition lhs, final Recognition rhs) {
                                // Intentionally reversed to put high confidence at the head of the queue.
                                return Float.compare(rhs.getConfidence(), lhs.getConfidence());
                            }
                        });

        // Scale them back to the input size.
        for (int i = 0; i < config.getResultCount(); ++i) {
            float topClassScore = -1000f;
            int topClassScoreIndex = -1;

            // Skip the first catch-all class.
            for (int j = 1; j < config.getClassCount(); ++j) {
                float score = expit(outputClasses[0][i][j]);

                if (score > topClassScore) {
                    topClassScoreIndex = j;
                    topClassScore = score;
                }
            }

            if (topClassScore > config.getMinConfidence()) {
                final RectF detection =
                        new RectF(
                                outputLocations[0][i][1] * inputSize,
                                outputLocations[0][i][0] * inputSize,
                                outputLocations[0][i][3] * inputSize,
                                outputLocations[0][i][2] * inputSize);

                pq.add(
                        new Recognition(
                                "" + i,
                                labels.get(topClassScoreIndex),
                                topClassScore,
                                detection));
            }
        }

        final ArrayList<Recognition> recognitions = new ArrayList<Recognition>();
        for (int i = 0; i < Math.min(pq.size(), config.getMaxDetectCount()); ++i) {
            Recognition recog = pq.poll();
            recognitions.add(recog);
        }
        //坐标去重（非极大值抑制）
        final ArrayList<Recognition> keepRecognitions = new ArrayList<Recognition>();
        boolean[] isSuppressed = new boolean[recognitions.size()];
        for (int i = 0; i < recognitions.size(); i++) {
            if (!isSuppressed[i]) {
                for (int j = i + 1; j < recognitions.size(); j++) {
                    if (!isSuppressed[j]) {
                        Recognition r1 = recognitions.get(i);
                        Recognition r2 = recognitions.get(j);
                        if (RectUtil.getRectOverlapRatio(r1.getLocation(), r2.getLocation()) > config.getOverlapRatio()) {
                            isSuppressed[j] = true;
                        }
                    }
                }
            }
        }
        for (int i = 0; i < isSuppressed.length; i++) {
            if (!isSuppressed[i]) {
                //还原坐标
                if (frameToCropMatrix != null) {
                    RectF location = recognitions.get(i).getLocation();
                    //region 防止坐标越界
                    if (location.left < 0) {
                        location.left = 0;
                    }
                    if (location.right < 0) {
                        location.right = 0;
                    }
                    if (location.top < 0) {
                        location.top = 0;
                    }
                    if (location.bottom < 0) {
                        location.bottom = 0;
                    }

                    if (location.right > bitmap.getWidth()) {
                        location.right = bitmap.getWidth();
                    }

                    if (location.bottom > bitmap.getHeight()) {
                        location.bottom = bitmap.getHeight();
                    }
                    //endregion
                    cropToFrameMatrix.mapRect(location);
                    recognitions.get(i).setLocation(location);
                }
                keepRecognitions.add(recognitions.get(i));
            }
        }
        return keepRecognitions;
    }

    @Override
    public void enableStatLogging(final boolean logStats) {
    }

    @Override
    public String getStatString() {
        return "";
    }

    @Override
    public void close() {
        if (tfLite != null) {
            tfLite.close();
        }
    }
}
