package com.example.server.dto;

//Gemini can return several candidate replies if asked for multiple - we only ever read candidates[0]
public record GeminiCandidate(GeminiContent content) {
}
