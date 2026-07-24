package com.example.server.dto;

import java.time.Instant;

public record ConversationSummary(Long id, String title, Instant createdAt) {
}
