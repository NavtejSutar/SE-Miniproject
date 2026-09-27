package com.studymate.ai.Dto;

import java.util.List;

public record SearchResponse(
        List<DocumentResponse> documents,
        List<ConversationDTO> conversations
) {}
