package com.decompiler.task;

import com.decompiler.config.DecompilerConfig;
import com.decompiler.exception.DecompileException;
import com.decompiler.model.DecompileJob;
import com.decompiler.util.VineDecompiler;
import org.apache.commons.io.FileUtils;
import org.jetbrains.java.decompiler.main.decompiler.PrintStreamLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Map;
import java.util.concurrent.Callable;

public class DecompileTask implements Callable<DecompileResult> {
    private static final Logger logger = LoggerFactory.getLogger(DecompileTask.class);

    private final DecompileJob job;
    private final DecompilerConfig config;
    private final Map<String, Object> options;

    public DecompileTask(DecompileJob job, DecompilerConfig config, Map<String, Object> options) {
        this.job = job;
        this.config = config;
        this.options = options;
    }

    @Override
    public DecompileResult call() {
        try {
            File classFile = job.getSourceFile();
            String outputDirPath = job.getTargetPath();

            // 输出目录是job中指定的目标目录
            File outputDir = new File(outputDirPath);
            FileUtils.forceMkdir(outputDir);

            logger.debug("反编译: {} 到 {}", classFile.getAbsolutePath(), outputDir.getAbsolutePath());

            // 验证源文件存在且可读
            if (!classFile.exists() || !classFile.canRead()) {
                throw new DecompileException("源文件不存在或无法读取: " + classFile.getAbsolutePath());
            }

            // 获取class文件基础名称（用于后续验证）
            String className = classFile.getName();
            String baseName = className.toLowerCase().endsWith(".class")
                    ? className.substring(0, className.length() - 6)
                    : className;

            // 特殊处理含有下划线和数字的文件名（可能是内部类或生成类，这里是 vineflower bug貌似，如果是这样的文件反编译结果就会这样： TextBuilder_3.class -> TextBuilder.java）
            String baseNameWithoutNumber = baseName;
            if (baseName.contains("_")) {
                // 尝试去掉数字后缀，如 TextBuilder_3 -> TextBuilder
                int lastUnderscoreIndex = baseName.lastIndexOf("_");
                if (lastUnderscoreIndex > 0 && lastUnderscoreIndex < baseName.length() - 1) {
                    String possibleNumber = baseName.substring(lastUnderscoreIndex + 1);
                    if (possibleNumber.matches("\\d+")) {
                        baseNameWithoutNumber = baseName.substring(0, lastUnderscoreIndex);
                    }
                }
            }

            // 记录反编译前目录中的Java文件
            int javaFileCountBefore = countJavaFiles(outputDir);

            // 创建一个不输出任何内容的日志记录器
            PrintStreamLogger decompilerLogger = new PrintStreamLogger(new PrintStream(OutputStream.nullOutputStream()));

            // 创建VineDecompiler实例并配置
            VineDecompiler decompiler = new VineDecompiler(outputDir, options, decompilerLogger);

            decompiler.addSource(classFile);

            logger.info("正在反编译: {}", classFile.getName());
            decompiler.decompileContext();
            logger.info("反编译完成: {}", classFile.getName());

            // 记录反编译后目录中的Java文件
            int javaFileCountAfter = countJavaFiles(outputDir);

            // 检查是否有新生成的Java文件
            if (javaFileCountAfter > javaFileCountBefore) {
                File[] newJavaFiles = findNewJavaFiles(outputDir, baseNameWithoutNumber);

                if (newJavaFiles.length > 0) {
                    logger.info("Java文件已生成：{}", newJavaFiles[0].getAbsolutePath());

                    // 如果配置了删除class文件
                    if (config.isDeleteClassFiles()) {
                        FileUtils.deleteQuietly(classFile);
                        logger.debug("已删除class文件: {}", classFile.getAbsolutePath());
                    }

                    return new DecompileResult(job, true, null, outputDir.getAbsolutePath());
                }
            }

            // 直接检查可能的Java文件名
            File[] possibleJavaFiles = {
                    new File(outputDir, baseName + ".java"),                        // 完全匹配
                    new File(outputDir, baseNameWithoutNumber + ".java"),           // 去掉数字后缀
                    new File(outputDir, baseName.replace('$', '_') + ".java")       // 内部类替换
            };

            for (File javaFile : possibleJavaFiles) {
                if (javaFile.exists() && javaFile.length() > 0) {
                    logger.info("Java文件已生成：{}", javaFile.getAbsolutePath());

                    // 如果配置了删除class文件
                    if (config.isDeleteClassFiles()) {
                        FileUtils.deleteQuietly(classFile);
                        logger.debug("已删除class文件: {}", classFile.getAbsolutePath());
                    }

                    return new DecompileResult(job, true, null, outputDir.getAbsolutePath());
                }
            }

            // 未找到生成的Java文件，降级策略：接受目录中任何新的 Java 文件
            File[] allJavaFilesAfter = outputDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".java"));
            if (allJavaFilesAfter != null && allJavaFilesAfter.length > javaFileCountBefore) {
                // 找到了至少一个新的Java文件，认为反编译成功
                logger.info("反编译后发现新的Java文件，假定成功");

                // 如果配置了删除class文件
                if (config.isDeleteClassFiles()) {
                    FileUtils.deleteQuietly(classFile);
                    logger.debug("已删除class文件: {}", classFile.getAbsolutePath());
                }

                return new DecompileResult(job, true, null, outputDir.getAbsolutePath());
            }

            // 找不到任何生成的Java文件
            logger.warn("未找到预期的Java文件，class文件: {}, 输出目录: {}",
                    classFile.getName(), outputDir.getAbsolutePath());
            return new DecompileResult(job, false, "未生成Java文件", outputDir.getAbsolutePath());

        } catch (Exception e) {
            logger.error("反编译失败: " + job.getSourceFile().getAbsolutePath(), e);
            return new DecompileResult(job, false, e.getMessage(), null);
        }
    }

    /**
     * 计算目录中Java文件的数量
     */
    private int countJavaFiles(File directory) {
        File[] javaFiles = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".java"));
        return javaFiles != null ? javaFiles.length : 0;
    }

    /**
     * 查找与基础名称匹配的Java文件
     */
    private File[] findNewJavaFiles(File directory, String baseNamePattern) {
        File[] files = directory.listFiles((dir, name) -> {
            if (!name.toLowerCase().endsWith(".java")) {
                return false;
            }

            String javaBaseName = name.substring(0, name.length() - 5);
            return javaBaseName.equals(baseNamePattern) ||       // 完全匹配
                    javaBaseName.startsWith(baseNamePattern) ||   // 前缀匹配
                    javaBaseName.contains(baseNamePattern);       // 包含匹配
        });

        return files != null ? files : new File[0];
    }
}