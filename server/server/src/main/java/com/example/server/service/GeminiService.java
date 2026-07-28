package com.example.server.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import com.example.server.dto.GeminiCandidate;
import com.example.server.dto.GeminiContent;
import com.example.server.dto.GeminiMessage;
import com.example.server.dto.GeminiPart;
import com.example.server.dto.GeminiRequest;
import com.example.server.dto.GeminiResponse;

@Service
public class GeminiService {

    //model is part of the URL path, not the body, and it changes per call - so it stays a template variable rather than baked into the base URL
    private static final String BASE_URL = "https://generativelanguage.googleapis.com";
    private static final String GENERATE_CONTENT_PATH = "/v1beta/models/{model}:generateContent";

    private final RestClient restClient;
    private final String apiKey;
    private final String model;

    public GeminiService(
            @Value("${gemini.api.key}") String apiKey,
            @Value("${gemini.api.model}") String model) {
        this.apiKey = apiKey;
        this.model = model;
        this.restClient = RestClient.builder()
                .baseUrl(BASE_URL)
                .build();
    }

    /**
     * Sends the conversation so far to Gemini and returns just the reply text.
     * The caller must already have mapped each turn's role onto "user"/"model".
     */
    public String generateReply(List<GeminiMessage> conversation) {
        //Gemini has no separate "history" field - the whole conversation (including the latest user
        //turn) is sent as one ordered "contents" array, oldest message first
        List<GeminiContent> contents = conversation.stream()
                .map(message -> new GeminiContent(message.role(), List.of(new GeminiPart(message.text()))))
                .toList();

        GeminiResponse response;
        try {
            response = restClient.post()
                    .uri(GENERATE_CONTENT_PATH, model)
                    .header("x-goog-api-key", apiKey) //Gemini auths via this header, not an Authorization: Bearer token
                    .body(new GeminiRequest(contents))
                    .retrieve()
                    .body(GeminiResponse.class);
        } catch (RestClientResponseException ex) {
            //covers 4xx/5xx responses - e.g. 400 bad key, 429 rate limit exceeded, 500 upstream error
            throw new GeminiApiException(
                    "Gemini API call failed with status " + ex.getStatusCode().value() + ": " + ex.getResponseBodyAsString(),
                    ex);
        } catch (RestClientException ex) {
            //network-level failures: DNS, timeout, connection refused, etc
            throw new GeminiApiException("Failed to reach Gemini API", ex);
        }

        return extractReplyText(response);
    }

    private String extractReplyText(GeminiResponse response) {
        if (response == null || response.candidates() == null || response.candidates().isEmpty()) {
            //happens when Gemini blocks the response (e.g. safety filters) instead of returning a candidate
            throw new GeminiApiException("Gemini API returned no candidates");
        }

        GeminiCandidate candidate = response.candidates().get(0);
        if (candidate.content() == null
                || candidate.content().parts() == null
                || candidate.content().parts().isEmpty()) {
            throw new GeminiApiException("Gemini API returned a candidate with no content");
        }

        return candidate.content().parts().get(0).text();
    }
}
