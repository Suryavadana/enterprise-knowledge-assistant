package com.example.server.dto;

public record ChatRequest(Long conversationId, String message) {
}
