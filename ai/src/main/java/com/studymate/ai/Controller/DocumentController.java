package com.studymate.ai.Controller;

import com.studymate.ai.Dto.DocumentResponse;
import com.studymate.ai.Dto.SummarizeRequest;
import com.studymate.ai.Dto.SummaryResponse;
import com.studymate.ai.Service.DocumentService;
import com.studymate.ai.Service.SummarizationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;
    private final SummarizationService summarizationService;

    public DocumentController(
            DocumentService documentService,
            SummarizationService summarizationService
    ) {
        this.documentService = documentService;
        this.summarizationService = summarizationService;
    }

    @PostMapping("/upload")
    public ResponseEntity<DocumentResponse> upload(
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                documentService.upload(file, authentication)
        );
    }

    @GetMapping
    public ResponseEntity<List<DocumentResponse>> listDocuments(
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                documentService.listDocuments(authentication)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentResponse> getDocument(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                documentService.getDocument(id, authentication)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable Long id,
            Authentication authentication
    ) {
        documentService.deleteDocument(id, authentication);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/summarize")
    public ResponseEntity<SummaryResponse> summarize(
            @PathVariable Long id,
            @RequestBody(required = false) SummarizeRequest request,
            Authentication authentication
    ) {
        List<Integer> pageNumbers = (request != null) ? request.pageNumbers() : null;
        return ResponseEntity.ok(
                summarizationService.summarize(id, pageNumbers, authentication)
        );
    }
}