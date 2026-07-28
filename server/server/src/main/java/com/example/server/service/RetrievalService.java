package com.example.server.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.server.dto.RetrievedChunk;

@Service
public class RetrievalService {

    private final GeminiEmbeddingService geminiEmbeddingService;
    private final ChromaService chromaService;

    public RetrievalService(GeminiEmbeddingService geminiEmbeddingService, ChromaService chromaService) {
        this.geminiEmbeddingService = geminiEmbeddingService;
        this.chromaService = chromaService;
    }

    /**
     * Embeds the given question and returns the topK most similar chunks from Chroma, nearest
     * first.
     */
    public List<RetrievedChunk> retrieveRelevantChunks(String question, int topK) {
        List<Float> queryEmbedding = geminiEmbeddingService.embed(question);
        return chromaService.query(queryEmbedding, topK);
    }
}
