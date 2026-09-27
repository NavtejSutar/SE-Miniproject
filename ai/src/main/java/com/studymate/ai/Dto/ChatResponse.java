package com.studymate.ai.Dto;

import java.util.List;

public record ChatResponse(
        String answer,
        List<SourceDTO> sources
) {}
