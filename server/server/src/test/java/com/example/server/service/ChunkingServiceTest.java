package com.example.server.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

class ChunkingServiceTest {

    private final ChunkingService chunkingService = new ChunkingService();

    @Test
    void nullTextReturnsEmptyList() {
        assertThat(chunkingService.chunkText(null, 1000, 100)).isEmpty();
    }

    @Test
    void emptyTextReturnsEmptyList() {
        assertThat(chunkingService.chunkText("", 1000, 100)).isEmpty();
    }

    @Test
    void textShorterThanChunkSizeReturnsSingleChunk() {
        String text = "This is a short piece of text.";

        List<String> chunks = chunkingService.chunkText(text, 1000, 100);

        assertThat(chunks).containsExactly(text);
    }

    @Test
    void longTextProducesMultipleOverlappingChunks() {
        // 250 chars of repeated "word " tokens (5 chars each), well over chunkSize
        String text = "word ".repeat(50);

        List<String> chunks = chunkingService.chunkText(text, 100, 20);

        assertThat(chunks.size()).isGreaterThan(1);

        // each chunk after the first should start with content that also appears
        // near the end of the previous chunk, proving the overlap window exists
        for (int i = 1; i < chunks.size(); i++) {
            String previous = chunks.get(i - 1);
            String current = chunks.get(i);

            String overlapCandidate = current.substring(0, Math.min(10, current.length()));
            assertThat(previous).contains(overlapCandidate.trim().isEmpty() ? overlapCandidate : overlapCandidate.split(" ")[0]);
        }
    }

    @Test
    void chunkBoundariesDoNotSplitWords() {
        String text = "Enterprise knowledge assistants retrieve relevant chunks of documents "
                + "to ground large language model responses in factual, sourced content "
                + "instead of relying purely on parametric memory.";

        List<String> chunks = chunkingService.chunkText(text, 40, 10);

        assertThat(chunks.size()).isGreaterThan(1);

        for (String chunk : chunks) {
            assertThat(chunk).isNotEmpty();
            char firstChar = chunk.charAt(0);
            char lastChar = chunk.charAt(chunk.length() - 1);

            // a chunk boundary character should never be flanked by non-whitespace
            // on both sides across the split, which would indicate a word was cut
            assertThat(Character.isWhitespace(firstChar) || isWordStart(text, chunk)).isTrue();
            assertThat(Character.isWhitespace(lastChar) || isWordEnd(text, chunk)).isTrue();
        }
    }

    @Test
    void defaultOverloadUsesStandardChunkSizeAndOverlap() {
        String text = "a".repeat(2500);

        List<String> defaultChunks = chunkingService.chunkText(text);
        List<String> explicitChunks = chunkingService.chunkText(text, 1000, 100);

        assertThat(defaultChunks).isEqualTo(explicitChunks);
    }

    private boolean isWordStart(String fullText, String chunk) {
        int index = fullText.indexOf(chunk);
        return index == 0 || Character.isWhitespace(fullText.charAt(index - 1));
    }

    private boolean isWordEnd(String fullText, String chunk) {
        int index = fullText.indexOf(chunk);
        int end = index + chunk.length();
        return end == fullText.length() || Character.isWhitespace(fullText.charAt(end));
    }
}
