package com.studymate.ai.Dto;

public record AuthResponse(
        String token,
        String email,
        String username
) {}
