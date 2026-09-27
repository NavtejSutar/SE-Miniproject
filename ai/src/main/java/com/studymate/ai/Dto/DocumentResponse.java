package com.studymate.ai.Dto;

import java.time.LocalDateTime;

public record DocumentResponse(
        Long documentId,
        String fileName,
        String contentType,
        Long fileSize,
        LocalDateTime uploadedAt
) {
}
