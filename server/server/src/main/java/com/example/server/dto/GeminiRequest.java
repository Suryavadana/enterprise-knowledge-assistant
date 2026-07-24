package com.example.server.dto;

import java.util.List;

//top-level request body for POST /v1beta/models/{model}:generateContent - "contents" is the whole conversation, oldest turn first
public record GeminiRequest(List<GeminiContent> contents) {
}
