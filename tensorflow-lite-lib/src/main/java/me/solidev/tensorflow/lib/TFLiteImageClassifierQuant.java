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

import android.graphics.Bitmap;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Vector;

/**
 * A classifier specialized to label images using TensorFlow.
 *
 * @author _SOLID
 */
public class TFLiteImageClassifierQuant implements Classifier {
    private static final String TAG = "TFLiteImageClassifierF";
    private Interpreter tfLite;
    private static final int DIM_BATCH_SIZE = 1;
    private static final int DIM_PIXEL_SIZE = 3;
    private byte[][] labelProb;
    private Vector<String> labels = new Vector<>();
    private int[] intValues;
    private ByteBuffer imgData = null;
    private TensorFlowClassifyConfig config;

    private TFLiteImageClassifierQuant(TensorFlowClassifyConfig config) {
        this.config = config;
    }

    public static Classifier create(TensorFlowClassifyConfig config) {
        TFLiteImageClassifierQuant classifierFloat = new TFLiteImageClassifierQuant(config);

        if (config.getFileSource() == FileSource.ASSERT) {
            BufferedReader br;
            try {
                br = new BufferedReader(new InputStreamReader(config.getContext()
                        .getAssets()
                        .open(config.getLabelFilePath().split("file:///android_asset/")[1])));
                String line;
                while ((line = br.readLine()) != null) {
                    classifierFloat.labels.add(line);
                }
                br.close();
            } catch (IOException e) {
                throw new RuntimeException("Problem reading label file!", e);
            }

            classifierFloat.imgData =
                    ByteBuffer.allocateDirect(
                            DIM_BATCH_SIZE * config.getInputSizeX() * config.getInputSizeX() * DIM_PIXEL_SIZE);
            classifierFloat.imgData.order(ByteOrder.nativeOrder());
            try {
                classifierFloat.tfLite = new Interpreter(TensorFlowUtil.loadModelFile(config.getContext().getAssets(), config.getModelFilePath().split("file:///android_asset/")[1]), 3);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            classifierFloat.intValues = new int[config.getInputSizeX() * config.getInputSizeY()];
            classifierFloat.labelProb = new byte[1][classifierFloat.labels.size()];
        }
        return classifierFloat;
    }

    /**
     * Writes Image data into a {@code ByteBuffer}.
     */
    private void convertBitmapToByteBuffer(Bitmap bitmap) {
        if (imgData == null) {
            return;
        }
        imgData.rewind();
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        // Convert the image to floating point.
        int pixel = 0;
        for (int i = 0; i < config.getInputSizeX(); ++i) {
            for (int j = 0; j < config.getInputSizeY(); ++j) {
                final int val = intValues[pixel++];
                putPixelData(val);
            }
        }
    }

    private void putPixelData(int val) {
        imgData.put((byte) ((val >> 16) & 0xFF));
        imgData.put((byte) ((val >> 8) & 0xFF));
        imgData.put((byte) ((val) & 0xFF));
    }

    @Override
    public List<Recognition> recognizeImage(final Bitmap bitmap) {
        Bitmap cBitmap = TFBitmapUtil.getClassifyBitmap(bitmap, config.getInputSizeX(), config.getInputSizeY());
        convertBitmapToByteBuffer(cBitmap);
        // Run the inference call.
        tfLite.run(imgData, labelProb);

        // Find the best classifications.
        PriorityQueue<Recognition> pq =
                new PriorityQueue<>(
                        3,
                        new Comparator<Recognition>() {
                            @Override
                            public int compare(Recognition lhs, Recognition rhs) {
                                // Intentionally reversed to put high confidence at the head of the queue.
                                return Float.compare(rhs.getConfidence(), lhs.getConfidence());
                            }
                        });
        for (int i = 0; i < labels.size(); ++i) {
            pq.add(
                    new Recognition(
                            "" + i,
                            labels.size() > i ? labels.get(i) : "unknown",
                            (labelProb[0][i] & 0xff) / 255F,
                            null));
        }
        final ArrayList<Recognition> recognitions = new ArrayList<>();
        int recognitionsSize = Math.min(pq.size(), config.getMaxResultCount());
        for (int i = 0; i < recognitionsSize; ++i) {
            recognitions.add(pq.poll());
        }
        return recognitions;
    }

    @Override
    public void enableStatLogging(boolean logStats) {
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
