package com.decompiler.util;

import com.decompiler.model.DecompileJob;
import com.decompiler.task.TaskManager;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

public class FileUtil {
    private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);

    /**
     * 处理输入目录，解压所有JAR文件并收集所有class文件
     */
    public static List<DecompileJob> processDirectory(File directory, String outputPath, TaskManager taskManager) throws IOException {
        if (!directory.exists()) {
            throw new FileNotFoundException("目录不存在: " + directory.getAbsolutePath());
        }

        File outputDir = new File(outputPath);
        FileUtils.forceMkdir(outputDir);

        logger.info("处理目录: {}", directory.getAbsolutePath());
        final List<DecompileJob> jobs = Collections.synchronizedList(new ArrayList<>());

        // 1. 找出所有JAR文件和class文件
        List<File> jarFiles = new ArrayList<>();
        List<File> classFiles = new ArrayList<>();

        Files.walk(directory.toPath())
                .filter(Files::isRegularFile)
                .forEach(path -> {
                    String fileName = path.toString().toLowerCase();
                    File file = path.toFile();

                    if (fileName.endsWith(".jar") || fileName.endsWith(".war") ||
                            fileName.endsWith(".ear") || fileName.endsWith(".aar")) {
                        jarFiles.add(file);
                    } else if (fileName.endsWith(".class")) {
                        classFiles.add(file);
                    }
                });

        logger.info("找到 {} 个JAR文件和 {} 个class文件", jarFiles.size(), classFiles.size());

        // 2. 使用TaskManager并行解压所有JAR文件
        final List<File> extractedDirs = Collections.synchronizedList(new ArrayList<>());
        // 创建JAR提取任务列表
        List<Callable<Void>> extractionTasks = new ArrayList<>();
        for (File jarFile : jarFiles) {
            String jarName = jarFile.getName();
            String dirName = FilenameUtils.getBaseName(jarName);
            File extractDir = new File(outputDir, dirName);

            extractionTasks.add(() -> {
                try {
                    JarUtils.extractJar(jarFile, extractDir);
                    extractedDirs.add(extractDir);
                    return null;
                } catch (Exception e) {
                    logger.error("解压JAR文件失败: {}", jarFile.getName(), e);
                    return null;
                }
            });
        }

        // 并行执行所有JAR提取任务
        if (!extractionTasks.isEmpty()) {
            taskManager.executeParallel(extractionTasks);
        }

        // 3. 收集所有class文件
        // 处理原始class文件
        for (File classFile : classFiles) {
            Path relativePath = directory.toPath().relativize(classFile.toPath().getParent());
            File targetDir = new File(outputDir, relativePath.toString());
            FileUtils.forceMkdir(targetDir);

            File targetFile = new File(targetDir, classFile.getName());
            FileUtils.copyFile(classFile, targetFile);

            jobs.add(new DecompileJob(targetFile, targetDir.getAbsolutePath(), null));
        }
        // 处理从JAR中解压出来的class文件
        for (File extractDir : extractedDirs) {
            List<File> jarClassFiles = new ArrayList<>();
            Files.walk(extractDir.toPath())
                    .filter(path -> path.toString().toLowerCase().endsWith(".class"))
                    .forEach(path -> jarClassFiles.add(path.toFile()));

            logger.info("从 {} 中提取了 {} 个class文件", extractDir.getName(), jarClassFiles.size());

            for (File classFile : jarClassFiles) {
                String outputDirPath = classFile.getParent();
                jobs.add(new DecompileJob(classFile, outputDirPath, null));
            }
        }

        logger.info("总共找到 {} 个待反编译的class文件", jobs.size());
        return jobs;
    }

    /**
     * 处理单个JAR文件
     */
    public static List<DecompileJob> processJarFile(File jarFile, String outputPath) throws IOException {
        if (!jarFile.exists()) {
            throw new FileNotFoundException("文件不存在: " + jarFile.getAbsolutePath());
        }

        File outputDir = new File(outputPath);
        FileUtils.forceMkdir(outputDir);

        // 解压JAR文件到输出目录
        String jarName = jarFile.getName();
        String dirName = FilenameUtils.getBaseName(jarName);
        File extractDir = new File(outputDir, dirName);

        JarUtils.extractJar(jarFile, extractDir);

        // 收集所有class文件
        List<DecompileJob> jobs = new ArrayList<>();
        List<File> classFiles = new ArrayList<>();

        Files.walk(extractDir.toPath())
                .filter(path -> path.toString().toLowerCase().endsWith(".class"))
                .forEach(path -> classFiles.add(path.toFile()));

        logger.info("从JAR文件中提取了 {} 个class文件", classFiles.size());

        for (File classFile : classFiles) {
            String outputDirPath = classFile.getParent();
            jobs.add(new DecompileJob(classFile, outputDirPath, null));
        }

        return jobs;
    }

    /**
     * 处理单个class文件
     */
    public static List<DecompileJob> processClassFile(File classFile, String outputPath) throws IOException {
        if (!classFile.exists()) {
            throw new FileNotFoundException("文件不存在: " + classFile.getAbsolutePath());
        }

        File outputDir = new File(outputPath);
        FileUtils.forceMkdir(outputDir);

        // 复制class文件到输出目录
        File targetFile = new File(outputDir, classFile.getName());
        FileUtils.copyFile(classFile, targetFile);

        // 创建反编译任务
        List<DecompileJob> jobs = new ArrayList<>();
        jobs.add(new DecompileJob(targetFile, outputDir.getAbsolutePath(), null));

        return jobs;
    }
}