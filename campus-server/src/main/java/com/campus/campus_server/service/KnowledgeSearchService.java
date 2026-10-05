package com.campus.campus_server.service;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KnowledgeSearchService {
    private final EmbeddingClient embeddingClient;
    private final QdrantPointClient qdrantPointClient;

    public KnowledgeSearchService(
            EmbeddingClient embeddingClient,
            QdrantPointClient qdrantPointClient) {
        this.embeddingClient = embeddingClient;
        this.qdrantPointClient = qdrantPointClient;
    }

    public List<QdrantPointClient.SearchHit> search(Long subjectId, String question) {
        if (subjectId == null || subjectId <= 0) {
            throw new IllegalArgumentException("学科 ID 不合法");
        }
        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException("问题不能为空");
        }

        List<Float> vector = embeddingClient.embed(question.trim());
        return qdrantPointClient.searchBySubject(subjectId, vector);
    }
}