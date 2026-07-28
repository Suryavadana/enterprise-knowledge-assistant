package com.example.server.dto;

//top-level request body for POST /v1beta/models/{model}:embedContent - "model" here must be prefixed
//with "models/", unlike the path variable which is the bare model name
public record EmbeddingRequest(String model, EmbeddingContent content) {
}
