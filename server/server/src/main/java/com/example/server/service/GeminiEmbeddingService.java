package com.example.server.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import com.example.server.dto.Embedding;
import com.example.server.dto.EmbeddingContent;
import com.example.server.dto.EmbeddingRequest;
import com.example.server.dto.EmbeddingResponse;
import com.example.server.dto.GeminiPart;

@Service
public class GeminiEmbeddingService {

    //unlike chat, the embedding model is fixed to this feature rather than user-configurable,
    //so it's a constant here instead of a gemini.api.model-style property
    private static final String BASE_URL = "https://generativelanguage.googleapis.com";
    private static final String EMBED_CONTENT_PATH = "/v1beta/models/{model}:embedContent";
    private static final String EMBEDDING_MODEL = "gemini-embedding-001";

    private final RestClient restClient;
    private final String apiKey;

    public GeminiEmbeddingService(@Value("${gemini.api.key}") String apiKey) {
        this.apiKey = apiKey;
        this.restClient = RestClient.builder()
                .baseUrl(BASE_URL)
                .build();
    }

    /**
     * Sends the given text to Gemini's embedContent endpoint and returns the resulting vector.
     */
    public List<Float> embed(String text) {
        EmbeddingRequest request = new EmbeddingRequest(
                "models/" + EMBEDDING_MODEL, //body field needs the "models/" prefix; the path variable below doesn't
                new EmbeddingContent(List.of(new GeminiPart(text))));

        EmbeddingResponse response;
        try {
            response = restClient.post()
                    .uri(EMBED_CONTENT_PATH, EMBEDDING_MODEL)
                    .header("x-goog-api-key", apiKey) //Gemini auths via this header, not an Authorization: Bearer token
                    .body(request)
                    .retrieve()
                    .body(EmbeddingResponse.class);
        } catch (RestClientResponseException ex) {
            //covers 4xx/5xx responses - e.g. 400 bad key, 429 rate limit exceeded, 500 upstream error
            throw new GeminiApiException(
                    "Gemini embedding API call failed with status " + ex.getStatusCode().value() + ": " + ex.getResponseBodyAsString(),
                    ex);
        } catch (RestClientException ex) {
            //network-level failures: DNS, timeout, connection refused, etc
            throw new GeminiApiException("Failed to reach Gemini embedding API", ex);
        }

        return extractValues(response);
    }

    private List<Float> extractValues(EmbeddingResponse response) {
        Embedding embedding = response == null ? null : response.embedding();
        if (embedding == null || embedding.values() == null) {
            //happens if Gemini returns a 200 with an unexpected/empty body shape
            throw new GeminiApiException("Gemini embedding API returned no values");
        }
        return embedding.values();
    }
}
