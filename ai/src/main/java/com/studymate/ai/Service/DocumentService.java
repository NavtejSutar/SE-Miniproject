package com.studymate.ai.Service;

import com.studymate.ai.Dto.DocumentProgressResponse;
import com.studymate.ai.Dto.DocumentResponse;
import com.studymate.ai.Entities.Document;
import com.studymate.ai.Entities.Users;
import com.studymate.ai.Exception.FileValidationException;
import com.studymate.ai.Exception.ResourceNotFoundException;
import com.studymate.ai.Repo.DocumentRepo;
import com.studymate.ai.Repo.UsersRepo;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

@Service
public class DocumentService {

    private static final long MAX_FILE_SIZE = 50L * 1024 * 1024; // 50 MB

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf",
            "image/jpeg",
            "image/png"
    );

    private final DocumentRepo documentRepo;
    private final UsersRepo usersRepo;
    private final FileStorageService fileStorageService;
    private final DocumentProcessingService documentProcessingService;
    private final EmbeddingService embeddingService;

    public DocumentService(
            DocumentRepo documentRepo,
            UsersRepo usersRepo,
            FileStorageService fileStorageService,
            DocumentProcessingService documentProcessingService,
            EmbeddingService embeddingService
    ) {
        this.documentRepo = documentRepo;
        this.usersRepo = usersRepo;
        this.fileStorageService = fileStorageService;
        this.documentProcessingService = documentProcessingService;
        this.embeddingService = embeddingService;
    }

    public DocumentResponse upload(
            MultipartFile file,
            Authentication authentication
    ) {
        validateFile(file);

        Users user = getAuthenticatedUser(authentication);

        String filePath = fileStorageService.storeFile(file);

        Document document = Document.builder()
                .fileName(file.getOriginalFilename())
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .filePath(filePath)
                .user(user)
                .build();

        Document saved = documentRepo.save(document);

        // Trigger async processing (PDF extraction, OCR, etc.)
        documentProcessingService.processDocument(saved.getDocumentId());

        return toResponse(saved);
    }

    public List<DocumentResponse> listDocuments(Authentication authentication) {
        Users user = getAuthenticatedUser(authentication);
        return documentRepo.findByUser(user)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public DocumentResponse getDocument(Long documentId, Authentication authentication) {
        Users user = getAuthenticatedUser(authentication);
        Document document = documentRepo.findByDocumentIdAndUser(documentId, user)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Document not found with id: " + documentId
                ));
        return toResponse(document);
    }

    public void deleteDocument(Long documentId, Authentication authentication) {
        Users user = getAuthenticatedUser(authentication);
        Document document = documentRepo.findByDocumentIdAndUser(documentId, user)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Document not found with id: " + documentId
                ));

        // Delete vector embeddings
        embeddingService.deleteByDocumentId(documentId);

        // Delete physical file
        fileStorageService.deleteFile(document.getFilePath());

        // Delete document (cascades to pages)
        documentRepo.delete(document);
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileValidationException("File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new FileValidationException(
                    "File size exceeds the maximum limit of 50 MB"
            );
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new FileValidationException(
                    "Unsupported file type: " + contentType
                            + ". Allowed types: PDF, JPEG, PNG"
            );
        }
    }

    private Users getAuthenticatedUser(Authentication authentication) {
        return usersRepo.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public DocumentProgressResponse getDocumentProgress(Long documentId, Authentication authentication) {
        Users user = getAuthenticatedUser(authentication);
        Document document = documentRepo.findByDocumentIdAndUser(documentId, user)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Document not found with id: " + documentId
                ));
        return new DocumentProgressResponse(
                document.getDocumentId(),
                document.getStatus(),
                document.getProgress(),
                document.getProgressMessage(),
                document.getErrorMessage()
        );
    }

    private DocumentResponse toResponse(Document document) {
        return new DocumentResponse(
                document.getDocumentId(),
                document.getFileName(),
                document.getContentType(),
                document.getFileSize(),
                document.getStatus(),
                document.getProgress(),
                document.getProgressMessage(),
                document.getUploadedAt()
        );
    }
}