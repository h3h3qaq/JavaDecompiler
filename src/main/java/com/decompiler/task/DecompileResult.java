package com.decompiler.task;

import com.decompiler.model.DecompileJob;

public class DecompileResult {
    private final DecompileJob job;
    private final boolean success;
    private final String errorMessage;
    private final String outputPath;

    public DecompileResult(DecompileJob job, boolean success, String errorMessage, String outputPath) {
        this.job = job;
        this.success = success;
        this.errorMessage = errorMessage;
        this.outputPath = outputPath;
    }

    public DecompileJob getJob() {
        return job;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getOutputPath() {
        return outputPath;
    }
}