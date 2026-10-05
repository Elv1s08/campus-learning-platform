package service;

import com.campus.campus_server.service.EmbeddingClient;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EmbeddingClientIntegrationTest {

    @Test
    void returns1024DimensionalVector() {
        EmbeddingClient client = new EmbeddingClient(
                RestClient.builder(),
                "http://127.0.0.1:1234",
                "text-embedding-qwen3-embedding-0.6b"
        );

        assertEquals(1024, client.embed("数据库主键有什么作用？").size());
    }
}