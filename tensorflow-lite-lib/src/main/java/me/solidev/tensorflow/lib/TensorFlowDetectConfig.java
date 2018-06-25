package me.solidev.tensorflow.lib;

import android.content.Context;

/**
 * 识别模型参数配置
 *
 * @author _SOLID
 * @since 2018/6/6.
 */
public class TensorFlowDetectConfig {
    private Context context;
    private FileSource fileSource;
    private int resultCount;
    private int classCount;
    private int inputSize;
    private int maxDetectCount;
    private float overlapRatio;
    private float minConfidence;
    private String modelFilePath;
    private String labelsFilePath;
    private String boxsFilePath;

    TensorFlowDetectConfig(Builder builder) {
        context = builder.context;
        fileSource = builder.fileSource;
        resultCount = builder.resultCount;
        classCount = builder.classCount;
        inputSize = builder.inputSize;
        maxDetectCount = builder.maxDetectCount;
        overlapRatio = builder.overlapRatio;
        minConfidence = builder.minConfidence;
        modelFilePath = builder.modelFilePath;
        labelsFilePath = builder.labelsFilePath;
        boxsFilePath = builder.boxsFilePath;
    }

    public Context getContext() {
        return context;
    }

    public FileSource getFileSource() {
        return fileSource;
    }

    public int getResultCount() {
        return resultCount;
    }

    public int getClassCount() {
        return classCount;
    }

    public int getInputSize() {
        return inputSize;
    }


    public int getMaxDetectCount() {
        return maxDetectCount;
    }

    public float getOverlapRatio() {
        return overlapRatio;
    }

    public float getMinConfidence() {
        return minConfidence;
    }

    public String getModelFilePath() {
        return modelFilePath;
    }

    public String getLabelsFilePath() {
        return labelsFilePath;
    }

    public String getBoxsFilePath() {
        return boxsFilePath;
    }

    public static class Builder {
        private Context context;
        private FileSource fileSource;
        private int resultCount;
        private int classCount;
        private int inputSize;
        private int maxDetectCount = 200;
        private float overlapRatio = 0.1f;
        private float minConfidence = 0.1f;
        private String modelFilePath;
        private String labelsFilePath;
        private String boxsFilePath;

        public Builder setContext(Context context) {
            this.context = context.getApplicationContext();
            return this;
        }

        public Builder setFileSource(FileSource fileSource) {
            this.fileSource = fileSource;
            return this;
        }

        public Builder setResultCount(int resultCount) {
            this.resultCount = resultCount;
            return this;
        }

        public Builder setClassCount(int classCount) {
            this.classCount = classCount;
            return this;
        }

        public Builder setInputSize(int inputSize) {
            this.inputSize = inputSize;
            return this;
        }


        public Builder setMaxDetectCount(int maxDetectCount) {
            this.maxDetectCount = maxDetectCount;
            return this;
        }

        public Builder setOverlapRatio(float overlapRatio) {
            this.overlapRatio = overlapRatio;
            return this;
        }

        public Builder setMinConfidence(float minConfidence) {
            this.minConfidence = minConfidence;
            return this;
        }

        public Builder setModelFilePath(String modelFilePath) {
            this.modelFilePath = modelFilePath;
            return this;
        }

        public Builder setLabelsFilePath(String labelsFilePath) {
            this.labelsFilePath = labelsFilePath;
            return this;
        }

        public Builder setBoxsFilePath(String boxsFilePath) {
            this.boxsFilePath = boxsFilePath;
            return this;
        }

        public TensorFlowDetectConfig build() {
            return new TensorFlowDetectConfig(this);
        }
    }
}
