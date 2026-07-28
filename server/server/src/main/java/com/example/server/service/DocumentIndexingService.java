package com.example.server.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.server.entity.Document;

@Service
public class DocumentIndexingService {

    private final ChunkingService chunkingService;
    private final GeminiEmbeddingService geminiEmbeddingService;
    private final ChromaService chromaService;

    public DocumentIndexingService(
            ChunkingService chunkingService,
            GeminiEmbeddingService geminiEmbeddingService,
            ChromaService chromaService) {
        this.chunkingService = chunkingService;
        this.geminiEmbeddingService = geminiEmbeddingService;
        this.chromaService = chromaService;
    }

    public void indexDocument(Document document) {
        List<String> chunks = chunkingService.chunkText(document.getExtractedText());
        if (chunks.isEmpty()) {
            return;
        }

        List<String> ids = new ArrayList<>(chunks.size());
        List<List<Float>> embeddings = new ArrayList<>(chunks.size());
        List<Map<String, Object>> metadatas = new ArrayList<>(chunks.size());

        for (int i = 0; i < chunks.size(); i++) {
            ids.add("document-" + document.getId() + "-chunk-" + i);
            embeddings.add(geminiEmbeddingService.embed(chunks.get(i)));
            metadatas.add(Map.of(
                    "documentId", document.getId(),
                    "filename", document.getFilename(),
                    "chunkIndex", i));
        }

        chromaService.addChunks(ids, embeddings, chunks, metadatas);
    }
}
