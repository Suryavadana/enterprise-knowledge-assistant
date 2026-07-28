package com.example.server.dto;

import java.time.Instant;

public record DocumentSummary(Long id, String filename, String fileType, Instant uploadedAt) {
}
