package com.campus.campus_server.controller;

import com.campus.campus_server.service.KnowledgeAnswerService;
import com.campus.campus_server.service.KnowledgeSearchService;
import com.campus.campus_server.service.QdrantPointClient;
import com.campus.campus_server.common.ApiResponse;
import com.campus.campus_server.dto.CreateSubjectRequest;
import com.campus.campus_server.entity.Subject;
import com.campus.campus_server.service.SubjectService;
import com.campus.campus_server.dto.AnswerResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@RequestMapping("/api/subjects")
public class SubjectController {

    private final SubjectService subjectService;
    private final KnowledgeSearchService knowledgeSearchService;
    private final KnowledgeAnswerService knowledgeAnswerService;

    public SubjectController(
            SubjectService subjectService,
            KnowledgeSearchService knowledgeSearchService,
            KnowledgeAnswerService knowledgeAnswerService) {
        this.subjectService = subjectService;
        this.knowledgeSearchService = knowledgeSearchService;
        this.knowledgeAnswerService = knowledgeAnswerService;
    }

    @GetMapping
    public ApiResponse<List<Subject>> list(
            @RequestAttribute("role") String role) {

        if ("ADMIN".equals(role)) {
            return ApiResponse.success(subjectService.list());
        }

        return ApiResponse.success(
                subjectService.lambdaQuery()
                        .eq(Subject::getStatus, 1)
                        .list()
        );
    }

    @PostMapping
    public ApiResponse<Boolean> create(
            @RequestAttribute("role") String role,
            @Valid @RequestBody CreateSubjectRequest request) {

        if (!"ADMIN".equals(role)) {
            return ApiResponse.error(403, "没有管理员权限");
        }

        boolean exists = subjectService.lambdaQuery()
                .eq(Subject::getSubjectCode, request.getSubjectCode())
                .exists();

        if (exists) {
            return ApiResponse.error("学科编号已存在");
        }

        Subject subject = new Subject();
        subject.setSubjectCode(request.getSubjectCode());
        subject.setSubjectName(request.getSubjectName());
        subject.setDescription(request.getDescription());
        subject.setStatus(1);

        return ApiResponse.success(subjectService.save(subject));
    }

    @GetMapping("/{subjectId}/search")
    public ApiResponse<List<QdrantPointClient.SearchHit>> search(
            @PathVariable Long subjectId,
            @RequestParam String question,
            @RequestAttribute("role") String role) {

        Subject subject = subjectService.getById(subjectId);
        if (subject == null) {
            return ApiResponse.error("学科不存在");
        }
        if (!"ADMIN".equals(role) && !Integer.valueOf(1).equals(subject.getStatus())) {
            return ApiResponse.error(403, "学科已停用");
        }

        try {
            return ApiResponse.success(
                    knowledgeSearchService.search(subjectId, question)
            );
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/{subjectId}/ask")
    public ApiResponse<AnswerResponse> ask(
            @PathVariable Long subjectId,
            @RequestParam String question,
            @RequestAttribute("role") String role) {

        Subject subject = subjectService.getById(subjectId);
        if (subject == null) {
            return ApiResponse.error("学科不存在");
        }
        if (!"ADMIN".equals(role)
                && !Integer.valueOf(1).equals(subject.getStatus())) {
            return ApiResponse.error(403, "学科已停用");
        }

        try {
            return ApiResponse.success(
                    knowledgeAnswerService.answer(subjectId, question)
            );
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}