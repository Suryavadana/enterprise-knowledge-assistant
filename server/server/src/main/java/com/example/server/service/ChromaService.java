package com.example.server.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import com.example.server.dto.ChromaAddRequest;
import com.example.server.dto.ChromaQueryRequest;
import com.example.server.dto.ChromaQueryResponse;
import com.example.server.dto.RetrievedChunk;

@Service
public class ChromaService {

    private static final String ADD_PATH = "/api/v2/tenants/{tenant}/databases/{database}/collections/{collectionId}/add";
    private static final String QUERY_PATH = "/api/v2/tenants/{tenant}/databases/{database}/collections/{collectionId}/query";

    private final RestClient restClient;
    private final String tenant;
    private final String database;
    private final String collectionId;

    public ChromaService(
            @Value("${chroma.base-url}") String baseUrl,
            @Value("${chroma.tenant}") String tenant,
            @Value("${chroma.database}") String database,
            @Value("${chroma.collection-id}") String collectionId) {
        this.tenant = tenant;
        this.database = database;
        this.collectionId = collectionId;
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(new SimpleClientHttpRequestFactory())
                .build();
    }

    /**
     * Adds a batch of chunk records to the configured Chroma collection. The four lists are
     * positional - ids.get(i), embeddings.get(i), documents.get(i) and metadatas.get(i) must all
     * describe the same chunk.
     */
    public void addChunks(
            List<String> ids,
            List<List<Float>> embeddings,
            List<String> documents,
            List<Map<String, Object>> metadatas) {
        ChromaAddRequest request = new ChromaAddRequest(ids, embeddings, documents, metadatas);

        try {
            restClient.post()
                    .uri(ADD_PATH, tenant, database, collectionId)
                    .body(request)
                    .retrieve()
                    .toBodilessEntity(); //success response is "{}" - nothing worth deserializing
        } catch (RestClientResponseException ex) {
            //covers 4xx/5xx responses - e.g. 404 unknown collection, 400 malformed batch, 500 upstream error
            throw new ChromaApiException(
                    "Chroma add call failed with status " + ex.getStatusCode().value() + ": " + ex.getResponseBodyAsString(),
                    ex);
        } catch (RestClientException ex) {
            //network-level failures: DNS, timeout, connection refused, etc
            throw new ChromaApiException("Failed to reach Chroma", ex);
        }
    }

    /**
     * Runs a similarity search against the configured Chroma collection for the given query
     * vector and returns the top nResults chunks, nearest first.
     */
    public List<RetrievedChunk> query(List<Float> queryEmbedding, int nResults) {
        ChromaQueryRequest request = new ChromaQueryRequest(List.of(queryEmbedding), nResults);

        ChromaQueryResponse response;
        try {
            response = restClient.post()
                    .uri(QUERY_PATH, tenant, database, collectionId)
                    .body(request)
                    .retrieve()
                    .body(ChromaQueryResponse.class);
        } catch (RestClientResponseException ex) {
            //covers 4xx/5xx responses - e.g. 404 unknown collection, 400 malformed query, 500 upstream error
            throw new ChromaApiException(
                    "Chroma query call failed with status " + ex.getStatusCode().value() + ": " + ex.getResponseBodyAsString(),
                    ex);
        } catch (RestClientException ex) {
            //network-level failures: DNS, timeout, connection refused, etc
            throw new ChromaApiException("Failed to reach Chroma", ex);
        }

        return toRetrievedChunks(response);
    }

    //Chroma nests every response field one list deep per query embedding sent (we only ever send
    //one), so index [0] of each outer list is always "the results for our query"
    private List<RetrievedChunk> toRetrievedChunks(ChromaQueryResponse response) {
        List<String> documents = response.documents().get(0);
        List<Map<String, Object>> metadatas = response.metadatas().get(0);
        List<Double> distances = response.distances().get(0);

        List<RetrievedChunk> chunks = new ArrayList<>(documents.size());
        for (int i = 0; i < documents.size(); i++) {
            chunks.add(new RetrievedChunk(documents.get(i), metadatas.get(i), distances.get(i)));
        }
        return chunks;
    }
}
