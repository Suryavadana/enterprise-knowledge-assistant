package com.example.server.dto;

import java.util.List;

//top-level response body from generateContent - the reply text lives at candidates[0].content.parts[0].text
public record GeminiResponse(List<GeminiCandidate> candidates) {
}
