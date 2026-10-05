package com.campus.campus_server.service;

import com.campus.campus_server.entity.KnowledgeChunk;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@Service
public class QdrantPointClient {
    private final RestClient restClient;
    private final String collection;

    public QdrantPointClient(
            RestClient.Builder builder,
            @Value("${app.qdrant.base-url}") String baseUrl,
            @Value("${app.qdrant.collection}") String collection) {
        this.restClient = builder.baseUrl(baseUrl).build();
        this.collection = collection;
    }

    public void upsert(KnowledgeChunk chunk, Long subjectId, List<Float> vector) {
        if (chunk == null || chunk.getId() == null
                || chunk.getDocumentId() == null || chunk.getChunkIndex() == null
                || chunk.getChunkText() == null || subjectId == null
                || vector == null || vector.isEmpty()) {
            throw new IllegalArgumentException("切片或向量数据不完整");
        }

        Map<String, Object> payload = Map.of(
                "documentId", chunk.getDocumentId(),
                "subjectId", subjectId,
                "chunkIndex", chunk.getChunkIndex(),
                "chunkText", chunk.getChunkText()
        );

        Map<String, Object> point = Map.of(
                "id", chunk.getId(),
                "vector", vector,
                "payload", payload
        );

        //调用的Qdrant的upsert point接口
        restClient.put()
                .uri("/collections/{collection}/points?wait=true", collection)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("points", List.of(point)))
                .retrieve()
                .toBodilessEntity();
    }

    public List<SearchHit> searchBySubject(Long subjectId, List<Float> vector) {
        if (subjectId == null || vector == null || vector.isEmpty()) {
            throw new IllegalArgumentException("学科 ID 或查询向量不能为空");
        }

        Map<String, Object> filter = Map.of(
                "must", List.of(Map.of(
                        "key", "subjectId",
                        "match", Map.of("value", subjectId)
                ))
        );

        Map<?, ?> response = restClient.post()
                .uri("/collections/{collection}/points/query", collection)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "query", vector,
                        "filter", filter,
                        "limit", 5,
                        "with_payload", true
                ))
                .retrieve()
                .body(Map.class);

        if (response == null || !(response.get("result") instanceof Map<?, ?> result)
                || !(result.get("points") instanceof List<?> points)) {
            throw new IllegalStateException("Qdrant 未返回有效检索结果");
        }

        List<SearchHit> hits = new ArrayList<>();
        for (Object item : points) {
            if (!(item instanceof Map<?, ?> point)
                    || !(point.get("payload") instanceof Map<?, ?> payload)
                    || !(point.get("id") instanceof Number id)
                    || !(point.get("score") instanceof Number score)
                    || !(payload.get("documentId") instanceof Number documentId)
                    || !(payload.get("chunkText") instanceof String chunkText)) {
                throw new IllegalStateException("Qdrant 检索结果格式不正确");
            }

            hits.add(new SearchHit(
                    id.longValue(),
                    documentId.longValue(),
                    score.doubleValue(),
                    chunkText
            ));
        }
        return hits;
    }

    //几个检索结果组成的列表
    public record SearchHit(
            Long chunkId,
            Long documentId,
            double score,
            String chunkText
    ) {}
}