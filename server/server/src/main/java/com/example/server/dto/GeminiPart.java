package com.example.server.dto;

//Gemini wraps text in a "part" because a turn can mix multiple parts (text, inline images, etc) - we only ever send/read plain text parts
public record GeminiPart(String text) {
}
