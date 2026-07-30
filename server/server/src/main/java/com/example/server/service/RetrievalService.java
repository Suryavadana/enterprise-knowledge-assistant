package com.example.server.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.server.dto.GroundingConfidence;
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

    /**
     * Derives a grounding confidence label from the closest (minimum-distance) chunk in the
     * given results — see explanation in the accompanying chat response for why minimum, not
     * average, is used.
     */
    public GroundingConfidence computeGroundingConfidence(List<RetrievedChunk> chunks) {
        if (chunks.isEmpty()) {
            return GroundingConfidence.NONE;
        }

        double minDistance = chunks.stream()
                .map(RetrievedChunk::distance)
                .min(Comparator.naturalOrder())
                .orElseThrow();

        if (minDistance < 0.3) {
            return GroundingConfidence.HIGH;
        }
        if (minDistance < 0.6) {
            return GroundingConfidence.MEDIUM;
        }
        return GroundingConfidence.LOW;
    }
}
