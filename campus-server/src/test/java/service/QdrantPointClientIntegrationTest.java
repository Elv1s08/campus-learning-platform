package service;

import com.campus.campus_server.entity.KnowledgeChunk;
import com.campus.campus_server.service.EmbeddingClient;
import com.campus.campus_server.service.QdrantPointClient;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class QdrantPointClientIntegrationTest {
    private static final long TEST_ID = 9_000_000_000L;
    private static final String QDRANT_URL = "http://127.0.0.1:6333";
    private static final String COLLECTION = "knowledge_qwen3_0_6b";

    @Test
    void upsertsAndReadsPoint() {
        EmbeddingClient embedding = new EmbeddingClient(
                RestClient.builder(),
                "http://127.0.0.1:1234",
                "text-embedding-qwen3-embedding-0.6b"
        );
        QdrantPointClient qdrant = new QdrantPointClient(
                RestClient.builder(), QDRANT_URL, COLLECTION
        );

        KnowledgeChunk chunk = new KnowledgeChunk();
        chunk.setId(TEST_ID);
        chunk.setDocumentId(TEST_ID);
        chunk.setChunkIndex(0);
        chunk.setChunkText("数据库主键用于唯一标识一条记录。");

        List<Float> vector = embedding.embed(chunk.getChunkText());
        RestClient reader = RestClient.create(QDRANT_URL);

        try {
            qdrant.upsert(chunk, 1L, vector);

            Map<?, ?> response = reader.get()
                    .uri("/collections/{collection}/points/{id}", COLLECTION, TEST_ID)
                    .retrieve()
                    .body(Map.class);

            assertNotNull(response);
            Map<?, ?> result = (Map<?, ?>) response.get("result");
            assertNotNull(result);
            assertEquals(TEST_ID, ((Number) result.get("id")).longValue());
        } finally {
            reader.post()
                    .uri("/collections/{collection}/points/delete?wait=true", COLLECTION)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("points", List.of(TEST_ID)))
                    .retrieve()
                    .toBodilessEntity();
        }
    }
}