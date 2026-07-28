package com.example.server.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class ChunkingService {

    public static final int DEFAULT_CHUNK_SIZE = 1000;
    public static final int DEFAULT_OVERLAP = 100;

    public List<String> chunkText(String text) {
        return chunkText(text, DEFAULT_CHUNK_SIZE, DEFAULT_OVERLAP);
    }

    public List<String> chunkText(String text, int chunkSize, int overlap) {
        if (text == null || text.isEmpty()) {
            return List.of();
        }
        if (chunkSize <= 0) {
            throw new IllegalArgumentException("chunkSize must be positive");
        }
        if (overlap < 0 || overlap >= chunkSize) {
            throw new IllegalArgumentException("overlap must be non-negative and smaller than chunkSize");
        }
        if (text.length() <= chunkSize) {
            return List.of(text);
        }

        List<String> chunks = new ArrayList<>();
        int start = 0;

        while (start < text.length()) {
            int end = extendToWordBoundary(text, Math.min(start + chunkSize, text.length()));

            chunks.add(text.substring(start, end));

            if (end >= text.length()) {
                break;
            }

            //the overlap window (end - overlap) can itself land inside a word, so it
            //needs the same word-boundary treatment as the chunk end does
            start = extendToWordBoundary(text, end - overlap);
        }

        return chunks;
    }

    //pushes a boundary index forward to the next whitespace character when it falls
    //between two non-whitespace characters, so a chunk never starts or ends mid-word
    private int extendToWordBoundary(String text, int index) {
        if (index <= 0 || index >= text.length()) {
            return index;
        }
        boolean insideWord = !Character.isWhitespace(text.charAt(index - 1)) && !Character.isWhitespace(text.charAt(index));
        if (!insideWord) {
            return index;
        }
        int i = index;
        while (i < text.length() && !Character.isWhitespace(text.charAt(i))) {
            i++;
        }
        return i;
    }
}
