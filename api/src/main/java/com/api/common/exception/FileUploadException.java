package com.api.common.exception;

public class FileUploadException extends RuntimeException {
    public FileUploadException(String message, Throwable cause) {
        super(message, cause);
    }
}
