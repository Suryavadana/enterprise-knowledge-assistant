package com.example.server.dto;

import java.util.List;

//top-level request body for POST .../collections/{collection_id}/query - query_embeddings is a
//list of query vectors, but we only ever send one at a time
public record ChromaQueryRequest(
        List<List<Float>> query_embeddings,
        int n_results) {
}
