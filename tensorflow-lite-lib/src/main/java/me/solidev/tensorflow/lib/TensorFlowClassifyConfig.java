package me.solidev.tensorflow.lib;

import android.content.Context;

/**
 * 分类模型参数配置
 *
 * @author _SOLID
 */
public class TensorFlowClassifyConfig {

    private Context context;
    private int inputSizeX;
    private int inputSizeY;
    private String modelFilePath;
    private String labelFilePath;
    private FileSource fileSource;
    private int maxResultCount;

    TensorFlowClassifyConfig(Builder builder) {
        this.inputSizeX = builder.inputSizeX;
        this.inputSizeY = builder.inputSizeY;
        this.modelFilePath = builder.modelFilePath;
        this.labelFilePath = builder.labelFilePath;
        this.fileSource = builder.fileSource;
        this.context = builder.context;
        this.maxResultCount = builder.maxResultCount;
    }

    public int getInputSizeX() {
        return inputSizeX;
    }

    public int getInputSizeY() {
        return inputSizeY;
    }

    public String getModelFilePath() {
        return modelFilePath;
    }

    public String getLabelFilePath() {
        return labelFilePath;
    }

    public FileSource getFileSource() {
        return fileSource;
    }

    public Context getContext() {
        return context;
    }

    public int getMaxResultCount() {
        return maxResultCount;
    }

    public static class Builder {
        private Context context;
        private int inputSizeX;
        private int inputSizeY;
        private String modelFilePath;
        private String labelFilePath;
        private FileSource fileSource;
        private int maxResultCount = 1;

        public Builder setInputSize(int inputSizeX, int inputSizeY) {
            this.inputSizeX = inputSizeX;
            this.inputSizeY = inputSizeY;
            return this;
        }

        public Builder setModelFilePath(String modelFilePath) {
            this.modelFilePath = modelFilePath;
            return this;
        }

        public Builder setLabelFilePath(String labelFilePath) {
            this.labelFilePath = labelFilePath;
            return this;
        }

        public Builder setFileSource(FileSource fileSource) {
            this.fileSource = fileSource;
            return this;
        }

        public Builder setContext(Context context) {
            this.context = context.getApplicationContext();
            return this;
        }

        public Builder setMaxResultCount(int maxResultCount) {
            this.maxResultCount = maxResultCount;
            return this;
        }

        public TensorFlowClassifyConfig build() {
            return new TensorFlowClassifyConfig(this);
        }
    }
}
