package com.example.server.dto;

import java.util.List;

//same shape appears twice in the API: as a request turn ({role, parts}) and inside each response candidate ({content: {role, parts}}) - one record models both
public record GeminiContent(String role, List<GeminiPart> parts) {
}
