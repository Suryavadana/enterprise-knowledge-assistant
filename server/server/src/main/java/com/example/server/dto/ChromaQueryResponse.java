package com.example.server.dto;

import java.util.List;
import java.util.Map;

//top-level response body from .../collections/{collection_id}/query - every field is one list
//per query embedding sent, so ids.get(0)/documents.get(0)/etc all correspond to our single query
public record ChromaQueryResponse(
        List<List<String>> ids,
        List<List<String>> documents,
        List<List<Map<String, Object>>> metadatas,
        List<List<Double>> distances) {
}
