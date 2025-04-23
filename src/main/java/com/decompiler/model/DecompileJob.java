package com.decompiler.model;

import java.io.File;

/**
 * 表示单个反编译任务
 */
public class DecompileJob {
    private final File sourceFile;
    private final String targetPath;
    private final String relativePath;

    public DecompileJob(File sourceFile, String targetPath, String relativePath) {
        this.sourceFile = sourceFile;
        this.targetPath = targetPath;
        this.relativePath = relativePath;
    }

    public File getSourceFile() {
        return sourceFile;
    }

    public String getTargetPath() {
        return targetPath;
    }

    public String getRelativePath() {
        return relativePath;
    }

    @Override
    public String toString() {
        return "DecompileJob{" +
                "sourceFile=" + sourceFile +
                ", targetPath='" + targetPath + '\'' +
                ", relativePath='" + relativePath + '\'' +
                '}';
    }
}