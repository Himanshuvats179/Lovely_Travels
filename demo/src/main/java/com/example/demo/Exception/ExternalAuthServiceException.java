package com.example.demo.Exception;

public class ExternalAuthServiceException extends RuntimeException {
    public ExternalAuthServiceException(String message, Throwable cause) {
        super(message, cause);
    }
    public ExternalAuthServiceException(String message) {
        super(message);
    }
}