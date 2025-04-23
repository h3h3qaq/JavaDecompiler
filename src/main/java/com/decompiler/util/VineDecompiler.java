package com.decompiler.util;

import org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler;
import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger;

import java.io.File;
import java.util.Map;

/**
 * Vineflower 反编译器包装类
 * 通过继承 ConsoleDecompiler 来访问其 protected 构造函数
 */
public class VineDecompiler extends ConsoleDecompiler {

    /**
     * 创建一个VineDecompiler实例
     *
     * @param destination 输出目录
     * @param options 反编译选项
     * @param logger 日志记录器
     */
    public VineDecompiler(File destination, Map<String, Object> options, IFernflowerLogger logger) {
        super(destination, options, logger);
    }

    /**
     * 创建一个VineDecompiler实例
     *
     * @param destination 输出目录
     * @param options 反编译选项
     * @param logger 日志记录器
     * @param saveType 保存类型
     */
    public VineDecompiler(File destination, Map<String, Object> options, IFernflowerLogger logger, SaveType saveType) {
        super(destination, options, logger, saveType);
    }
}