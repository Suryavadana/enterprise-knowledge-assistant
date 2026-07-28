package com.example.server.dto;

//top-level response body from embedContent - the vector lives at embedding.values
public record EmbeddingResponse(Embedding embedding) {
}
