package com.studymate.ai.Controller;

import com.studymate.ai.Dto.DocumentResponse;
import com.studymate.ai.Service.DocumentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(
            DocumentService documentService
    ) {
        this.documentService = documentService;
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
}