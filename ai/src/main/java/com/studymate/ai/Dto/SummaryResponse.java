package com.studymate.ai.Dto;

public record SummaryResponse(
        Long documentId,
        String fileName,
        String summary
) {}
