package com.example.server.dto;

import java.time.Instant;

public record PromptTemplateResponse(Long id, String title, String content, Instant createdAt) {
}
