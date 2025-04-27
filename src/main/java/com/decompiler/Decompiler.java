package com.decompiler;

import com.decompiler.config.DecompilerConfig;
import com.decompiler.model.DecompileJob;
import com.decompiler.task.DecompileResult;
import com.decompiler.task.DecompileTask;
import com.decompiler.task.TaskManager;
import com.decompiler.util.FileUtil;
import com.decompiler.util.JarUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class Decompiler {
    private static final Logger logger = LoggerFactory.getLogger(Decompiler.class);

    private final DecompilerConfig config;
    private final Map<String, Object> vineflowerOptions;
    private final TaskManager taskManager;
    private final AtomicInteger successCount = new AtomicInteger(0);
    private final AtomicInteger failureCount = new AtomicInteger(0);

    public Decompiler(DecompilerConfig config, Map<String, Object> vineflowerOptions) {
        this.config = config;
        this.vineflowerOptions = vineflowerOptions;
        this.taskManager = new TaskManager(config.getThreadCount());
    }

    public void execute() throws IOException {
        logger.info("使用 {} 个线程启动反编译", config.getThreadCount());
        logger.info("输入目录：{}", config.getInputPath());
        logger.info("输出目录: {}", config.getOutputPath());

        // 准备需要反编译的文件
        List<DecompileJob> jobs = prepareDecompileJobs();

        logger.info("共找到 {} 个等待反编译的文件", jobs.size());

        if (jobs.isEmpty()) {
            logger.warn("没有找到可反编译的文件");
            return;
        }

        // 执行反编译任务
        List<DecompileResult> results = taskManager.processTasks(jobs,
                (job) -> new DecompileTask(job, config, vineflowerOptions),
                this::processResult);

        // 输出统计信息
        printStats();
    }

    private void processResult(DecompileResult result) {
        if (result.isSuccess()) {
            successCount.incrementAndGet();
        } else {
            failureCount.incrementAndGet();
            logger.warn("反编译失败: {}, 错误: {}",
                    result.getJob().getSourceFile().getName(),
                    result.getErrorMessage());
        }
    }

    private List<DecompileJob> prepareDecompileJobs() throws IOException {
        File inputFile = new File(config.getInputPath());

        // 如果是目录
        if (inputFile.isDirectory()) {
            return FileUtil.processDirectory(inputFile, config.getOutputPath(), taskManager);
        }
        // 如果是 JAR
        else if (JarUtils.isJarFile(inputFile)) {
            return FileUtil.processJarFile(inputFile, config.getOutputPath());
        }
        // 如果是单个 class 文件
        else if (inputFile.getName().toLowerCase().endsWith(".class")) {
            List<DecompileJob> jobs = new ArrayList<>();
            jobs.add(new DecompileJob(inputFile, config.getOutputPath(), null));
            return jobs;
        } else {
            throw new IllegalArgumentException("输入必须是.class文件、.jar文件或目录");
        }
    }

    /**
     * 打印反编译统计信息
     */
    private void printStats() {
        int total = successCount.get() + failureCount.get();
        logger.info("反编译已完成");
        logger.info("文件总数：{}", total);
        logger.info("已成功反编译：{}", successCount.get());
        logger.info("失败：{}", failureCount.get());

    }
}