package com.studymate.ai.Controller;

import com.studymate.ai.Dto.SearchResponse;
import com.studymate.ai.Service.SearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping
    public ResponseEntity<SearchResponse> search(
            @RequestParam String q,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                searchService.search(q, authentication)
        );
    }
}
