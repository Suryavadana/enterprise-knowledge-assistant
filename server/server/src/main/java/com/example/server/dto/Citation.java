package com.example.server.dto;

public record Citation(String filename, Long documentId, int chunkIndex) {
}
