package com.example.server.dto;

import java.util.List;
import java.util.Map;

//top-level request body for POST .../collections/{collection_id}/add - all four lists are
//positional (ids[i], embeddings[i], documents[i], metadatas[i] describe the same record)
public record ChromaAddRequest(
        List<String> ids,
        List<List<Float>> embeddings,
        List<String> documents,
        List<Map<String, Object>> metadatas) {
}
