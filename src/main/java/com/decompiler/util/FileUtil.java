package com.decompiler.util;

import com.decompiler.model.DecompileJob;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FileUtil {
    private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);

    /**
     * 处理输入目录，解压所有JAR文件并收集所有class文件
     */
    public static List<DecompileJob> processDirectory(File directory, String outputPath) throws IOException {
        if (!directory.exists()) {
            throw new FileNotFoundException("目录不存在: " + directory.getAbsolutePath());
        }

        File outputDir = new File(outputPath);
        FileUtils.forceMkdir(outputDir);

        logger.info("处理目录: {}", directory.getAbsolutePath());
        List<DecompileJob> jobs = new ArrayList<>();

        // 1. 找出所有JAR文件并解压
        List<File> jarFiles = new ArrayList<>();
        Files.walk(directory.toPath())
                .filter(path -> {
                    String fileName = path.toString().toLowerCase();
                    return Files.isRegularFile(path) &&
                            (fileName.endsWith(".jar") || fileName.endsWith(".war") ||
                                    fileName.endsWith(".ear") || fileName.endsWith(".aar"));
                })
                .forEach(path -> jarFiles.add(path.toFile()));

        logger.info("找到 {} 个JAR文件", jarFiles.size());

        // 解压所有JAR文件
        for (File jarFile : jarFiles) {
            try {
                String jarName = jarFile.getName();
                String dirName = jarName.substring(0, jarName.lastIndexOf('.'));
                File jarOutputDir = new File(outputDir, dirName);
                JarUtils.extractJar(jarFile, jarOutputDir);

                jobs.addAll(collectClassFiles(jarOutputDir, jarOutputDir.getAbsolutePath()));
            } catch (IOException e) {
                logger.error("解压JAR文件失败: {}", jarFile.getName(), e);
            }
        }

        Path inputBasePath = directory.toPath();
        List<File> originalClassFiles = new ArrayList<>();

        Files.walk(inputBasePath)
                .filter(path -> {
                    String fileName = path.toString().toLowerCase();
                    return Files.isRegularFile(path) && fileName.endsWith(".class");
                })
                .forEach(path -> originalClassFiles.add(path.toFile()));

        logger.info("找到 {} 个原始class文件", originalClassFiles.size());

        // 处理原始class文件
        for (File classFile : originalClassFiles) {
            Path relativePath = inputBasePath.relativize(classFile.toPath().getParent());

            File targetDir = new File(outputDir, relativePath.toString());
            FileUtils.forceMkdir(targetDir);

            File targetFile = new File(targetDir, classFile.getName());

            FileUtils.copyFile(classFile, targetFile);

            jobs.add(new DecompileJob(targetFile, targetDir.getAbsolutePath(), null));
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
        String dirName = jarName.substring(0, jarName.lastIndexOf('.'));
        File extractDir = new File(outputDir, dirName);

        JarUtils.extractJar(jarFile, extractDir);

        // 收集所有class文件，并指定正确的输出路径
        return collectClassFiles(extractDir, extractDir.getAbsolutePath());
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

        // 创建反编译任务 - 注意指定正确的输出目录
        List<DecompileJob> jobs = new ArrayList<>();
        jobs.add(new DecompileJob(targetFile, outputDir.getAbsolutePath(), null));

        return jobs;
    }

    /**
     * 递归收集目录中的所有class文件并创建反编译任务
     * @param directory 要扫描的目录
     * @param outputBasePath 基础输出路径
     */
    public static List<DecompileJob> collectClassFiles(File directory, String outputBasePath) throws IOException {
        List<DecompileJob> jobs = new ArrayList<>();

        List<File> classFiles = Files.walk(directory.toPath())
                .filter(path -> path.toString().toLowerCase().endsWith(".class"))
                .map(Path::toFile)
                .collect(Collectors.toList());

        logger.info("在目录 {} 中找到 {} 个class文件", directory.getAbsolutePath(), classFiles.size());

        // 为每个class文件创建反编译任务
        for (File classFile : classFiles) {
            // 计算相对于基础目录的路径，确保输出保持相同结构
            String relativePath = classFile.getParent().substring(directory.getAbsolutePath().length());
            File targetDir = new File(outputBasePath + relativePath);

            // 创建目标目录
            FileUtils.forceMkdir(targetDir);

            // 创建反编译任务，设置正确的输出路径
            jobs.add(new DecompileJob(classFile, targetDir.getAbsolutePath(), null));
        }

        return jobs;
    }
}