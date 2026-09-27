package com.studymate.ai.Dto;

import com.studymate.ai.Enum.ProcessingStatus;

import java.time.LocalDateTime;

public record DocumentResponse(
        Long documentId,
        String fileName,
        String contentType,
        Long fileSize,
        ProcessingStatus status,
        LocalDateTime uploadedAt
) {
}
