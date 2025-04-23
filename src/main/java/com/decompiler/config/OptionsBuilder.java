package com.decompiler.config;

import java.util.HashMap;
import java.util.Map;

/**
 * 构建Vineflower反编译器选项的构建器
 */
public class OptionsBuilder {
    private final Map<String, Object> options;

    public OptionsBuilder() {
        this.options = VineflowerOptions.loadDefaultOptions();
    }

    /**
     * 添加自定义选项
     */
    public OptionsBuilder withOption(String key, String value) {
        options.put(key, value);
        return this;
    }

    /**
     * 启用泛型反编译
     */
    public OptionsBuilder withGenerics(boolean enable) {
        options.put(VineflowerOptions.DGS, enable ? "1" : "0");
        return this;
    }

    /**
     * 启用内部类反编译
     */
    public OptionsBuilder withInnerClasses(boolean enable) {
        options.put(VineflowerOptions.DIN, enable ? "1" : "0");
        return this;
    }

    /**
     * 启用断言反编译
     */
    public OptionsBuilder withAssertions(boolean enable) {
        options.put(VineflowerOptions.DAS, enable ? "1" : "0");
        return this;
    }

    /**
     * 启用枚举反编译
     */
    public OptionsBuilder withEnums(boolean enable) {
        options.put(VineflowerOptions.DEN, enable ? "1" : "0");
        return this;
    }

    /**
     * 启用预览特性反编译
     */
    public OptionsBuilder withPreviewFeatures(boolean enable) {
        options.put(VineflowerOptions.DPR, enable ? "1" : "0");
        return this;
    }

    /**
     * 启用switch表达式反编译
     */
    public OptionsBuilder withSwitchExpressions(boolean enable) {
        options.put(VineflowerOptions.SWE, enable ? "1" : "0");
        return this;
    }

    /**
     * 启用模式匹配
     */
    public OptionsBuilder withPatternMatching(boolean enable) {
        options.put(VineflowerOptions.PAM, enable ? "1" : "0");
        return this;
    }

    /**
     * 使用本地变量表名称
     */
    public OptionsBuilder withLocalVarNames(boolean enable) {
        options.put(VineflowerOptions.UDV, enable ? "1" : "0");
        return this;
    }

    /**
     * 使用方法参数名
     */
    public OptionsBuilder withMethodParams(boolean enable) {
        options.put(VineflowerOptions.UMP, enable ? "1" : "0");
        return this;
    }

    /**
     * 移除空的try-catch块
     */
    public OptionsBuilder withRemoveEmptyTryCatch(boolean enable) {
        options.put(VineflowerOptions.RER, enable ? "1" : "0");
        return this;
    }

    /**
     * 隐藏空的super()调用
     */
    public OptionsBuilder withHideEmptySuper(boolean enable) {
        options.put(VineflowerOptions.HES, enable ? "1" : "0");
        return this;
    }

    /**
     * 隐藏默认构造函数
     */
    public OptionsBuilder withHideDefaultConstructor(boolean enable) {
        options.put(VineflowerOptions.HDC, enable ? "1" : "0");
        return this;
    }

    /**
     * 设置线程数
     */
    public OptionsBuilder withThreads(int threadCount) {
        if (threadCount > 0) {
            options.put(VineflowerOptions.THR, String.valueOf(threadCount));
        }
        return this;
    }

    /**
     * 设置缩进字符串
     */
    public OptionsBuilder withIndent(String indentString) {
        options.put(VineflowerOptions.IND, indentString);
        return this;
    }

    /**
     * 构建最终的选项映射
     */
    public Map<String, Object> build() {
        return new HashMap<>(options);
    }
}