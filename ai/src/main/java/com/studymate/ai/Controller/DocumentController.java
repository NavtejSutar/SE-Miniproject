package com.studymate.ai.Controller;

import com.studymate.ai.Dto.DocumentResponse;
import com.studymate.ai.Entities.Document;
import com.studymate.ai.Service.DocumentService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    public DocumentResponse upload(
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) {

        return documentService.upload(
                file,
                authentication
        );
    }
}