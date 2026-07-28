package com.example.server.dto;

import java.util.Map;

public record RetrievedChunk(String text, Map<String, Object> metadata, Double distance) {
}
