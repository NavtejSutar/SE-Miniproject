package com.studymate.ai.Dto;

import java.util.List;

public record FolderDTO(
        Long folderId,
        String name,
        List<DocumentResponse> documents
) {}
