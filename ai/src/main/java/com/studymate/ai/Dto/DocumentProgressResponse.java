package com.studymate.ai.Dto;

import com.studymate.ai.Enum.ProcessingStatus;

public record DocumentProgressResponse(
        Long documentId,
        ProcessingStatus status,
        Integer progress,
        String progressMessage,
        String errorMessage
) {}
