package com.decompiler.exception;

/**
 * 文件处理异常，包括文件读写、解析等操作中出现的错误
 */
public class FileProcessException extends Exception {

    public FileProcessException(String message) {
        super(message);
    }

}