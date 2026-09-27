package com.studymate.ai.Dto;

public record ChatRequest(
        Long conversationId,
        String prompt
) {}
