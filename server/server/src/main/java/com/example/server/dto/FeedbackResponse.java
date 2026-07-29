package com.example.server.dto;

public record FeedbackResponse(Long messageId, String rating, String comment) {
}
