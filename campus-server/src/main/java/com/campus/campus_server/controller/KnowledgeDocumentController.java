package com.campus.campus_server.controller;

import com.campus.campus_server.service.DocumentTextExtractor;
import com.campus.campus_server.service.KnowledgeDocumentService;
import com.campus.campus_server.service.SubjectService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.campus.campus_server.common.ApiResponse;
import com.campus.campus_server.entity.KnowledgeDocument;
import com.campus.campus_server.entity.Subject;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.campus.campus_server.service.DocumentProcessingService;
import com.campus.campus_server.service.DocumentIndexingService;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import com.campus.campus_server.entity.KnowledgeChunk;
import com.campus.campus_server.service.KnowledgeChunkService;
import java.util.List;

import java.nio.file.Path;

@RestController
@RequestMapping("/api/documents")
public class KnowledgeDocumentController {

    private final KnowledgeDocumentService documentService;
    private final SubjectService subjectService;
    private final Path uploadDir;
    private final DocumentTextExtractor textExtractor;
    private final DocumentProcessingService processingService;
    private final KnowledgeChunkService chunkService;
    private final DocumentIndexingService indexingService;

    public KnowledgeDocumentController(
            KnowledgeDocumentService documentService,
            SubjectService subjectService,
            @Value("${app.upload-dir}") String uploadDir,
            DocumentTextExtractor textExtractor,
            DocumentProcessingService processingService,
            KnowledgeChunkService chunkService,
            DocumentIndexingService indexingService) {
        this.documentService = documentService;
        this.subjectService = subjectService;
        this.uploadDir = Path.of(uploadDir).toAbsolutePath().normalize();
        this.textExtractor = textExtractor;
        this.processingService = processingService;
        this.chunkService = chunkService;
        this.indexingService = indexingService;
    }

    @PostMapping
    public ApiResponse<Long> upload(
            @RequestAttribute("role") String role,
            @RequestAttribute("userId") String userId,
            @RequestParam("subjectId") Long subjectId,
            @RequestParam("file") MultipartFile file) {

        if (!"ADMIN".equals(role)) {
            return ApiResponse.error(403, "没有上传权限");
        }

        Subject subject = subjectService.getById(subjectId);
        if (subject == null || !Integer.valueOf(1).equals(subject.getStatus())) {
            return ApiResponse.error("学科不存在或已停用");
        }

        if (file.isEmpty()) {
            return ApiResponse.error("文件不能为空");
        }

        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isBlank()) {
            return ApiResponse.error("文件名不能为空");
        }

        // 去掉客户端可能附带的目录，只保留文件名
        String fileName = originalName.replace('\\', '/');
        fileName = fileName.substring(fileName.lastIndexOf('/') + 1);

        int dot = fileName.lastIndexOf('.');
        if (dot < 1 || fileName.length() > 255) {
            return ApiResponse.error("文件名不合法");
        }

        String extension = fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
        if (!Set.of("pdf", "docx", "txt").contains(extension)) {
            return ApiResponse.error("只支持 PDF、DOCX、TXT 文件");
        }

        // 实际存储时用随机名，避免重名覆盖
        String storedName = UUID.randomUUID() + "." + extension;
        Path target = uploadDir.resolve(storedName);
        boolean saved = false;

        try {
            Files.createDirectories(uploadDir);

            try (InputStream input = file.getInputStream()) {
                Files.copy(input, target);
            }

            KnowledgeDocument document = new KnowledgeDocument();
            document.setSubjectId(subjectId);
            document.setUploadedBy(Long.valueOf(userId));
            document.setFileName(fileName);
            document.setStoragePath(storedName);
            document.setFileType(extension);
            document.setStatus("UPLOADED");

            saved = documentService.save(document);
            if (!saved) {
                return ApiResponse.error("文档记录保存失败");
            }

            return ApiResponse.success(document.getId());
        } catch (IOException e) {
            return ApiResponse.error("文件保存失败");
        } finally {
            // 数据库保存失败时，清理已上传的文件
            if (!saved) {
                try {
                    Files.deleteIfExists(target);
                } catch (IOException ignored) {
                    // 清理失败暂不覆盖原始错误
                }
            }
        }
    }

    @GetMapping("/{id}/text")
    public ApiResponse<String> readText(
            @PathVariable Long id,
            @RequestAttribute("role") String role) {

        if (!"ADMIN".equals(role)) {
            return ApiResponse.error(403, "没有查看权限");
        }

        KnowledgeDocument document = documentService.getById(id);
        if (document == null) {
            return ApiResponse.error("文档不存在");
        }

        Path filePath = uploadDir.resolve(document.getStoragePath()).normalize();
        if (!filePath.startsWith(uploadDir)) {
            return ApiResponse.error("文档路径不合法");
        }

        try {
            return ApiResponse.success(
                    textExtractor.extract(filePath, document.getFileType())
            );
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage());
        } catch (IOException e) {
            return ApiResponse.error("文件读取失败");
        }
    }

    @PostMapping("/{id}/process")
    public ApiResponse<Integer> process(
            @PathVariable Long id,
            @RequestAttribute("role") String role) {

        if (!"ADMIN".equals(role)) {
            return ApiResponse.error(403, "没有处理权限");
        }

        try {
            int chunkCount = processingService.process(id);
            return ApiResponse.success(chunkCount);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage());
        } catch (IOException e) {
            return ApiResponse.error("读取文档失败");
        }
    }

    @GetMapping("/{id}/chunks")
    public ApiResponse<List<KnowledgeChunk>> listChunks(
            @PathVariable Long id,
            @RequestAttribute("role") String role) {

        if (!"ADMIN".equals(role)) {
            return ApiResponse.error(403, "没有查看权限");
        }

        if (documentService.getById(id) == null) {
            return ApiResponse.error("文档不存在");
        }

        return ApiResponse.success(chunkService.lambdaQuery()
                .eq(KnowledgeChunk::getDocumentId, id)
                .orderByAsc(KnowledgeChunk::getChunkIndex)
                .list()
        );
    }

    @PostMapping("/{id}/index")
    public ApiResponse<Integer> index(
            @PathVariable Long id,
            @RequestAttribute("role") String role) {

        if (!"ADMIN".equals(role)) {
            return ApiResponse.error(403, "没有索引权限");
        }

        try {
            return ApiResponse.success(indexingService.index(id));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}