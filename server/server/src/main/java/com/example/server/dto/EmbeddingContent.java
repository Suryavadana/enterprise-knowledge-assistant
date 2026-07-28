package com.example.server.dto;

import java.util.List;

//"content" field of the embedContent request body - reuses GeminiPart since a part is just {"text": "..."} either way
public record EmbeddingContent(List<GeminiPart> parts) {
}
