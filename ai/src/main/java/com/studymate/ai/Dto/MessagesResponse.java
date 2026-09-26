package com.studymate.ai.Dto;

import com.studymate.ai.Enum.MessageRole;

import java.time.LocalDateTime;

public record MessagesResponse(
        MessageRole role,
        String content,
        LocalDateTime createdAt
) {
}
