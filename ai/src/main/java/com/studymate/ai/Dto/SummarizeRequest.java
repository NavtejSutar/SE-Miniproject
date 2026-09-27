package com.studymate.ai.Dto;

import java.util.List;

public record SummarizeRequest(
        List<Integer> pageNumbers
) {}
