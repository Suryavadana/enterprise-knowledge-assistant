package com.example.server.dto;

import java.util.List;

//"embedding" field of the embedContent response body
public record Embedding(List<Float> values) {
}
