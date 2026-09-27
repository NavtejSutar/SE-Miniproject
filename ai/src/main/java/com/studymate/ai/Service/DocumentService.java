package com.studymate.ai.Service;

import com.studymate.ai.Dto.DocumentResponse;
import com.studymate.ai.Entities.Document;
import com.studymate.ai.Entities.Users;
import com.studymate.ai.Repo.DocumentRepo;
import com.studymate.ai.Repo.UsersRepo;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DocumentService {

    private final DocumentRepo documentRepo;
    private final UsersRepo usersRepo;
    private final FileStorageService fileStorageService;

    public DocumentService(
            DocumentRepo documentRepo,
            UsersRepo usersRepo,
            FileStorageService fileStorageService
    ) {
        this.documentRepo = documentRepo;
        this.usersRepo = usersRepo;
        this.fileStorageService = fileStorageService;
    }

    public DocumentResponse upload(
            MultipartFile file,
            Authentication authentication
    ) {

        Users user = usersRepo
                .findByEmail(authentication.getName())
                .orElseThrow();

        String filePath =
                fileStorageService.storeFile(file);

        Document document = Document.builder()
                .fileName(file.getOriginalFilename())
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .filePath(filePath)
                .user(user)
                .build();

        Document saved=documentRepo.save(document);

        return new DocumentResponse(
                saved.getDocumentId(),
                saved.getFileName(),
                saved.getContentType(),
                saved.getFileSize(),
                saved.getUploadedAt()
        );
    }
}