package com.example.server.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import com.example.server.dto.ChromaAddRequest;
import com.example.server.dto.ChromaCollection;
import com.example.server.dto.ChromaCreateCollectionRequest;
import com.example.server.dto.ChromaQueryRequest;
import com.example.server.dto.ChromaQueryResponse;
import com.example.server.dto.RetrievedChunk;

@Service
public class ChromaService {

    private static final String COLLECTION_NAME = "documents";

    private static final String COLLECTIONS_PATH = "/api/v2/tenants/{tenant}/databases/{database}/collections";
    private static final String ADD_PATH = COLLECTIONS_PATH + "/{collectionId}/add";
    private static final String QUERY_PATH = COLLECTIONS_PATH + "/{collectionId}/query";

    private final RestClient restClient;
    private final String tenant;
    private final String database;

    //lazily resolved on first use and cached for the lifetime of the app - see getOrCreateCollectionId()
    private volatile String cachedCollectionId;

    public ChromaService(
            @Value("${chroma.base-url}") String baseUrl,
            @Value("${chroma.tenant}") String tenant,
            @Value("${chroma.database}") String database) {
        this.tenant = tenant;
        this.database = database;
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(new SimpleClientHttpRequestFactory())
                .build();
    }

    /**
     * Resolves the id of the "documents" collection, creating it if it doesn't exist yet, and
     * caches the result for the lifetime of the app. This lets the service self-heal after the
     * underlying Chroma instance restarts and loses its collections (e.g. Render free tier, which
     * has no persistent disk) without needing a hardcoded, manually-updated collection id.
     */
    public String getOrCreateCollectionId() {
        String id = cachedCollectionId;
        if (id != null) {
            return id;
        }

        synchronized (this) {
            if (cachedCollectionId != null) {
                return cachedCollectionId;
            }

            List<ChromaCollection> collections;
            try {
                collections = restClient.get()
                        .uri(COLLECTIONS_PATH, tenant, database)
                        .retrieve()
                        .body(new ParameterizedTypeReference<List<ChromaCollection>>() {});
            } catch (RestClientResponseException ex) {
                throw new ChromaApiException(
                        "Chroma list-collections call failed with status " + ex.getStatusCode().value() + ": " + ex.getResponseBodyAsString(),
                        ex);
            } catch (RestClientException ex) {
                throw new ChromaApiException("Failed to reach Chroma", ex);
            }

            String existingId = collections.stream()
                    .filter(collection -> COLLECTION_NAME.equals(collection.name()))
                    .map(ChromaCollection::id)
                    .findFirst()
                    .orElse(null);

            if (existingId != null) {
                cachedCollectionId = existingId;
                return cachedCollectionId;
            }

            ChromaCollection created;
            try {
                created = restClient.post()
                        .uri(COLLECTIONS_PATH, tenant, database)
                        .body(new ChromaCreateCollectionRequest(COLLECTION_NAME))
                        .retrieve()
                        .body(ChromaCollection.class);
            } catch (RestClientResponseException ex) {
                throw new ChromaApiException(
                        "Chroma create-collection call failed with status " + ex.getStatusCode().value() + ": " + ex.getResponseBodyAsString(),
                        ex);
            } catch (RestClientException ex) {
                throw new ChromaApiException("Failed to reach Chroma", ex);
            }

            cachedCollectionId = created.id();
            return cachedCollectionId;
        }
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
                    .uri(ADD_PATH, tenant, database, getOrCreateCollectionId())
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
                    .uri(QUERY_PATH, tenant, database, getOrCreateCollectionId())
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
