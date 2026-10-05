package com.campus.campus_server.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class EmbeddingClient {
    private final RestClient restClient;
    private final String model;

    public EmbeddingClient(
            RestClient.Builder builder,
            @Value("${app.embedding.base-url}") String baseUrl,
            @Value("${app.embedding.model}") String model) {
        this.restClient = builder.baseUrl(baseUrl).build();
        this.model = model;
    }

    public List<Float> embed(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("文本不能为空");
        }

        EmbeddingResponse response = restClient.post()
                .uri("/v1/embeddings")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new EmbeddingRequest(model, text))
                .retrieve()
                .body(EmbeddingResponse.class);

        if (response == null || response.data() == null
                || response.data().isEmpty()
                || response.data().get(0).embedding() == null) {
            throw new IllegalStateException("模型未返回有效向量");
        }

        return response.data().get(0).embedding();
    }

    //几个用来装数据的类
    public record EmbeddingRequest(String model, String input) {}
    public record EmbeddingResponse(List<EmbeddingData> data) {}
    public record EmbeddingData(List<Float> embedding) {}
}