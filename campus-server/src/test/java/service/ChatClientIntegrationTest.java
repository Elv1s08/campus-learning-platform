package service;

import com.campus.campus_server.service.ChatClient;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ChatClientIntegrationTest {

    @Test
    void answersUsingProvidedContext() {
        ChatClient client = new ChatClient(
                RestClient.builder(),
                "http://127.0.0.1:1234",
                "qwen/qwen3-4b-2507"
        );

        String answer = client.answer(
                "这项活动的代号是什么？",
                "校园测试活动的代号是ELVIS-47。"
        );

        assertTrue(answer.contains("ELVIS-47"), answer);
    }
}