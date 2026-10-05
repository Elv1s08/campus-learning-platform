package com.campus.campus_server.service;

import com.campus.campus_server.entity.KnowledgeChunk;
import com.campus.campus_server.entity.KnowledgeDocument;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DocumentIndexingService {
    private final KnowledgeDocumentService documentService;
    private final KnowledgeChunkService chunkService;
    private final EmbeddingClient embeddingClient;
    private final QdrantPointClient qdrantPointClient;

    public DocumentIndexingService(
            KnowledgeDocumentService documentService,
            KnowledgeChunkService chunkService,
            EmbeddingClient embeddingClient,
            QdrantPointClient qdrantPointClient) {
        this.documentService = documentService;
        this.chunkService = chunkService;
        this.embeddingClient = embeddingClient;
        this.qdrantPointClient = qdrantPointClient;
    }

    public int index(Long documentId) {
        KnowledgeDocument document = documentService.getById(documentId);
        if (document == null) {
            throw new IllegalArgumentException("文档不存在");
        }
        if (!"PARSED".equals(document.getStatus())) {
            throw new IllegalArgumentException("请先完成文档切片");
        }

        List<KnowledgeChunk> chunks = chunkService.lambdaQuery()
                .eq(KnowledgeChunk::getDocumentId, documentId)
                .orderByAsc(KnowledgeChunk::getChunkIndex)
                .list();
        if (chunks.isEmpty()) {
            throw new IllegalStateException("文档没有知识切片");
        }

        for (KnowledgeChunk chunk : chunks) {
            List<Float> vector = embeddingClient.embed(chunk.getChunkText());
            qdrantPointClient.upsert(chunk, document.getSubjectId(), vector);

            chunk.setVectorId(String.valueOf(chunk.getId()));
            if (!chunkService.updateById(chunk)) {
                throw new IllegalStateException("更新切片向量 ID 失败");
            }
        }

        document.setStatus("INDEXED");
        if (!documentService.updateById(document)) {
            throw new IllegalStateException("更新文档状态失败");
        }
        return chunks.size();
    }
}