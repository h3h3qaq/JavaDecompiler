package com.decompiler.config;

public class DecompilerConfig {
    private final String inputPath;
    private final String outputPath;
    private final int threadCount;
    private final boolean deleteClassFiles;

    private DecompilerConfig(Builder builder) {
        this.inputPath = builder.inputPath;
        this.outputPath = builder.outputPath;
        this.threadCount = builder.threadCount;
        this.deleteClassFiles = builder.deleteClassFiles;
    }

    public String getInputPath() {
        return inputPath;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public int getThreadCount() {
        return threadCount;
    }

    public boolean isDeleteClassFiles() {
        return deleteClassFiles;
    }

    public static class Builder {
        private String inputPath;
        private String outputPath;
        private int threadCount = Runtime.getRuntime().availableProcessors();
        private boolean deleteClassFiles = false; // 默认不删除class文件

        public Builder inputPath(String inputPath) {
            this.inputPath = inputPath;
            return this;
        }

        public Builder outputPath(String outputPath) {
            this.outputPath = outputPath;
            return this;
        }

        public Builder threadCount(int threadCount) {
            this.threadCount = threadCount <= 0 ?
                    Runtime.getRuntime().availableProcessors() : threadCount;
            return this;
        }

        public Builder deleteClassFiles(boolean deleteClassFiles) {
            this.deleteClassFiles = deleteClassFiles;
            return this;
        }

        public DecompilerConfig build() {
            return new DecompilerConfig(this);
        }
    }
}