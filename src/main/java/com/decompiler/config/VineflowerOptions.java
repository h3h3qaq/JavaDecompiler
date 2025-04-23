package com.decompiler.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

// 这里只使用了部分的选项，如果有更多需要可以参考 https://github.com/Vineflower/vineflower/blob/master/src/org/jetbrains/java/decompiler/main/extern/IFernflowerPreferences.java 加入
public class VineflowerOptions {
    private static final Logger logger = LoggerFactory.getLogger(VineflowerOptions.class);

    // 核心反编译选项
    public static final String DIN = "din";      // 反编译内部类
    public static final String DGS = "dgs";      // 反编译泛型
    public static final String DAS = "das";      // 反编译断言
    public static final String DEN = "den";      // 反编译枚举
    public static final String DPR = "dpr";      // 反编译预览特性
    public static final String SWE = "swe";      // 反编译Switch表达式
    public static final String PAM = "pam";      // 模式匹配

    // 代码质量选项
    public static final String UDV = "udv";      // 使用LVT名称
    public static final String UMP = "ump";      // 使用方法参数名
    public static final String RER = "rer";      // 移除空的try-catch块
    public static final String HES = "hes";      // 隐藏空的super()
    public static final String HDC = "hdc";      // 隐藏默认构造函数

    // 格式化选项
    public static final String THR = "thr";      // 线程数
    public static final String IND = "ind";      // 缩进字符串

    // 加载默认选项
    public static Map<String, Object> loadDefaultOptions() {
        Map<String, Object> options = new HashMap<>();
        Properties properties = new Properties();

        try (InputStream in = VineflowerOptions.class.getClassLoader()
                .getResourceAsStream("default-options.properties")) {
            if (in != null) {
                properties.load(in);
                for (String key : properties.stringPropertyNames()) {
                    options.put(key, properties.getProperty(key));
                }
            }
        } catch (IOException e) {
            logger.warn("Failed to load default options. Using built-in defaults.", e);
            // 设置核心选项默认值
            options.put(DIN, "1");  // 默认反编译内部类
            options.put(DGS, "1");  // 默认反编译泛型
            options.put(DAS, "1");  // 默认反编译断言
            options.put(DEN, "1");  // 默认反编译枚举
            options.put(DPR, "1");  // 默认反编译预览特性
            options.put(SWE, "1");  // 默认反编译Switch表达式
            options.put(PAM, "1");  // 默认启用模式匹配

            // 代码质量选项默认值
            options.put(UDV, "1");  // 默认使用LVT名称
            options.put(UMP, "1");  // 默认使用方法参数名
            options.put(RER, "1");  // 默认移除空的try-catch块
            options.put(HES, "1");  // 默认隐藏空的super()
            options.put(HDC, "1");  // 默认隐藏默认构造函数

            // 格式化选项默认值
            // 默认使用所有可用处理器
            options.put(THR, String.valueOf(Runtime.getRuntime().availableProcessors()));
            // 默认缩进3个空格
            options.put(IND, "   ");
        }

        return options;
    }
}