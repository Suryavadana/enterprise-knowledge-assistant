package com.example.server.service;

public class ChromaApiException extends RuntimeException {

    public ChromaApiException(String message) {
        super(message);
    }

    public ChromaApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
