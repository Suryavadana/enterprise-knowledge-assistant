package com.example.server.dto;

//one turn of the conversation, already mapped onto Gemini's own role vocabulary ("user"/"model" - not "assistant")
public record GeminiMessage(String role, String text) {
}
