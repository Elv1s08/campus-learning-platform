package com.campus.campus_server.dto;

import java.util.List;

public record AnswerResponse(String answer, List<Source> sources) {
    public record Source(Long documentId, Long chunkId, double score) {}
}