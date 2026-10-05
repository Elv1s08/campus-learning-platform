package com.campus.campus_server.service;

import com.campus.campus_server.dto.AnswerResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KnowledgeAnswerService {
    private final KnowledgeSearchService searchService;
    private final ChatClient chatClient;

    public KnowledgeAnswerService(
            KnowledgeSearchService searchService,
            ChatClient chatClient) {
        this.searchService = searchService;
        this.chatClient = chatClient;
    }

    public AnswerResponse answer(Long subjectId, String question) {
        List<QdrantPointClient.SearchHit> hits =
                searchService.search(subjectId, question);

        List<QdrantPointClient.SearchHit> selected =
                hits.stream().limit(3).toList();

        if (selected.isEmpty()) {
            return new AnswerResponse("资料不足，无法回答。", List.of());
        }

        StringBuilder context = new StringBuilder();
        for (QdrantPointClient.SearchHit hit : selected) {
            context.append("[文档ID：").append(hit.documentId())
                    .append("，切片ID：").append(hit.chunkId())
                    .append("]\n")
                    .append(hit.chunkText())
                    .append("\n\n");
        }

        List<AnswerResponse.Source> sources = selected.stream()
                .map(hit -> new AnswerResponse.Source(
                        hit.documentId(), hit.chunkId(), hit.score()))
                .toList();

        return new AnswerResponse(
                chatClient.answer(question, context.toString()),
                sources
        );
    }
}