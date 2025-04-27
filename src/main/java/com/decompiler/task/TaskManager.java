package com.decompiler.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class TaskManager {
    private static final Logger logger = LoggerFactory.getLogger(TaskManager.class);
    private final ExecutorService executorService;

    public TaskManager(int threadCount) {
        this.executorService = Executors.newFixedThreadPool(threadCount);
    }

    public <T> List<DecompileResult> processTasks(
            List<T> items,
            Function<T, DecompileTask> taskFactory,
            Consumer<DecompileResult> resultProcessor) {

        List<DecompileResult> results = new ArrayList<>();
        int totalTasks = items.size();
        int completedTasks = 0;
        int lastReportedPercentage = 0;

        try {
            // 创建所有任务
            List<Future<DecompileResult>> futures = new ArrayList<>();
            for (T item : items) {
                DecompileTask task = taskFactory.apply(item);
                futures.add(executorService.submit(task));
            }

            // 处理结果
            for (Future<DecompileResult> future : futures) {
                try {
                    DecompileResult result = future.get();
                    results.add(result);

                    // 处理结果
                    if (resultProcessor != null) {
                        resultProcessor.accept(result);
                    }

                    // 更新和报告进度
                    completedTasks++;
                    int percentage = (completedTasks * 100) / totalTasks;
                    if (percentage >= lastReportedPercentage + 10 || completedTasks == totalTasks) {
                        logger.info("Progress: {}% ({} of {} files)",
                                percentage, completedTasks, totalTasks);
                        lastReportedPercentage = percentage;
                    }

                } catch (InterruptedException | ExecutionException e) {
                    logger.error("Task execution failed", e);
                }
            }
        } finally {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(1, TimeUnit.MINUTES)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        return results;
    }
    public <T> void executeParallel(List<Callable<T>> tasks) {
        if (tasks.isEmpty()) {
            return;
        }

        try {
            List<Future<T>> futures = new ArrayList<>();
            for (Callable<T> task : tasks) {
                futures.add(executorService.submit(task));
            }

            // 等待所有任务完成
            for (Future<T> future : futures) {
                try {
                    future.get();
                } catch (ExecutionException | InterruptedException e) {
                    logger.error("任务执行失败", e);
                }
            }

            logger.info("完成了 {} 个并行任务", tasks.size());
        } catch (Exception e) {
            logger.error("并行任务执行过程中发生错误", e);
        }
    }
}