package com.example.server.dto;

import java.util.List;

public record ChatResponse(
        Long conversationId,
        String reply,
        Long assistantMessageId,
        List<Citation> citations,
        GroundingConfidence confidence) {
}
