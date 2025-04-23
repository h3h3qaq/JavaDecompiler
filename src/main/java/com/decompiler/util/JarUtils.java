package com.decompiler.util;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarUtils {
    private static final Logger logger = LoggerFactory.getLogger(JarUtils.class);

    /**
     * 解压JAR文件到指定目录
     */
    public static void extractJar(File jarFile, File outputDir) throws IOException {
        if (jarFile == null || !jarFile.exists() || jarFile.length() == 0) {
            logger.warn("JAR文件无效或为空: {}", jarFile != null ? jarFile.getName() : "null");
            return;
        }

        logger.info("解压JAR文件: {} 到 {}", jarFile.getName(), outputDir.getAbsolutePath());

        // 确保输出目录存在
        FileUtils.forceMkdir(outputDir);

        try (JarFile jar = new JarFile(jarFile)) {
            Enumeration<JarEntry> entries = jar.entries();
            int extractedFiles = 0;

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();
                File outFile = new File(outputDir, entryName);

                if (entry.isDirectory()) {
                    FileUtils.forceMkdir(outFile);
                } else {
                    // 确保父目录存在
                    FileUtils.forceMkdirParent(outFile);

                    try (InputStream in = jar.getInputStream(entry)) {
                        FileUtils.copyInputStreamToFile(in, outFile);
                        extractedFiles++;
                    } catch (IOException e) {
                        logger.warn("解压文件失败: {} - {}", entryName, e.getMessage());
                    }
                }
            }

            logger.info("从JAR文件 {} 解压了 {} 个文件", jarFile.getName(), extractedFiles);
        } catch (Exception e) {
            logger.error("解压JAR文件失败: {}", jarFile.getName(), e);
            throw new IOException("解压JAR文件失败: " + e.getMessage(), e);
        }
    }

    /**
     * 检查文件是否是JAR文件
     */
    public static boolean isJarFile(File file) {
        if (file == null || !file.exists() || file.isDirectory() || file.length() == 0) {
            return false;
        }

        String name = file.getName().toLowerCase();
        return name.endsWith(".jar") || name.endsWith(".war") ||
                name.endsWith(".ear") || name.endsWith(".aar");
    }
}