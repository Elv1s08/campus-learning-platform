package com.campus.campus_server.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class ChatClient {
    private final RestClient restClient;
    private final String model;

    public ChatClient(
            RestClient.Builder builder,
            @Value("${app.chat.base-url}") String baseUrl,
            @Value("${app.chat.model}") String model) {
        this.restClient = builder.baseUrl(baseUrl).build();
        this.model = model;
    }

    public String answer(String question, String context) {
        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException("问题不能为空");
        }
        if (context == null || context.isBlank()) {
            return "资料不足，无法回答。";
        }

        String prompt = "请只依据以下资料回答问题。资料没有答案时，请明确说“资料不足，无法回答”。\n\n"
                + "资料：\n" + context + "\n\n问题：" + question;

        ChatResponse response = restClient.post()
                .uri("/v1/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ChatRequest(model, List.of(new Message("user", prompt))))
                .retrieve()
                .body(ChatResponse.class);

        if (response == null || response.choices() == null
                || response.choices().isEmpty()
                || response.choices().get(0).message() == null
                || response.choices().get(0).message().content() == null) {
            throw new IllegalStateException("聊天模型未返回有效回答");
        }

        return response.choices().get(0).message().content();
    }

    public record ChatRequest(String model, List<Message> messages) {}
    public record Message(String role, String content) {}
    public record ChatResponse(List<Choice> choices) {}
    public record Choice(Message message) {}
}