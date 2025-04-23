package com.decompiler.exception;

/**
 * 配置相关异常，如无效的参数、缺少必要设置等
 */
public class ConfigException extends Exception {

    public ConfigException(String message) {
        super(message);
    }

}