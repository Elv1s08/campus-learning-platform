package com.campus.campus_server.service;

import com.campus.campus_server.entity.KnowledgeChunk;
import com.campus.campus_server.entity.KnowledgeDocument;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Service
public class DocumentProcessingService {

    private final KnowledgeDocumentService documentService;
    private final KnowledgeChunkService chunkService;
    private final DocumentTextExtractor extractor;
    private final TextChunker chunker;
    private final Path uploadDir;

    public DocumentProcessingService(
            KnowledgeDocumentService documentService,
            KnowledgeChunkService chunkService,
            DocumentTextExtractor extractor,
            TextChunker chunker,
            @Value("${app.upload-dir}") String uploadDir) {
        this.documentService = documentService;
        this.chunkService = chunkService;
        this.extractor = extractor;
        this.chunker = chunker;
        this.uploadDir = Path.of(uploadDir).toAbsolutePath().normalize();
    }

    @Transactional(rollbackFor = Exception.class)
    public int process(Long documentId) throws IOException {
        KnowledgeDocument document = documentService.getById(documentId);
        if (document == null) {
            throw new IllegalArgumentException("文档不存在");
        }
        if (!"UPLOADED".equals(document.getStatus())) {
            throw new IllegalArgumentException("文档已处理或状态不正确");
        }

        Path filePath = uploadDir.resolve(document.getStoragePath()).normalize();
        if (!filePath.startsWith(uploadDir)) {
            throw new IllegalArgumentException("文档路径不合法");
        }

        String text = extractor.extract(filePath, document.getFileType());
        List<String> parts = chunker.split(text);
        if (parts.isEmpty()) {
            throw new IllegalArgumentException("文档没有可解析的内容");
        }

        List<KnowledgeChunk> chunks = new ArrayList<>();
        for (int i = 0; i < parts.size(); i++) {
            KnowledgeChunk chunk = new KnowledgeChunk();
            chunk.setDocumentId(documentId);
            chunk.setChunkIndex(i);
            chunk.setChunkText(parts.get(i));
            chunks.add(chunk);
        }

        if (!chunkService.saveBatch(chunks)) {
            throw new IllegalStateException("保存知识块失败");
        }

        document.setStatus("PARSED");
        if (!documentService.updateById(document)) {
            throw new IllegalStateException("更新文档状态失败");
        }

        return chunks.size();
    }
}