package com.studymate.ai.Service;

import com.studymate.ai.Dto.DocumentResponse;
import com.studymate.ai.Dto.FolderDTO;
import com.studymate.ai.Entities.Document;
import com.studymate.ai.Entities.Folder;
import com.studymate.ai.Entities.Users;
import com.studymate.ai.Exception.ResourceNotFoundException;
import com.studymate.ai.Repo.DocumentRepo;
import com.studymate.ai.Repo.FolderRepo;
import com.studymate.ai.Repo.UsersRepo;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FolderService {

    private final FolderRepo folderRepo;
    private final DocumentRepo documentRepo;
    private final UsersRepo usersRepo;

    public FolderService(
            FolderRepo folderRepo,
            DocumentRepo documentRepo,
            UsersRepo usersRepo
    ) {
        this.folderRepo = folderRepo;
        this.documentRepo = documentRepo;
        this.usersRepo = usersRepo;
    }

    public FolderDTO createFolder(String name, Authentication authentication) {
        Users user = getAuthenticatedUser(authentication);
        Folder folder = Folder.builder()
                .name(name)
                .user(user)
                .build();
        Folder saved = folderRepo.save(folder);
        return toDTO(saved);
    }

    public List<FolderDTO> listFolders(Authentication authentication) {
        Users user = getAuthenticatedUser(authentication);
        return folderRepo.findByUser(user)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public FolderDTO renameFolder(Long folderId, String newName, Authentication authentication) {
        Users user = getAuthenticatedUser(authentication);
        Folder folder = folderRepo.findByFolderIdAndUser(folderId, user)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Folder not found: " + folderId
                ));
        folder.setName(newName);
        Folder saved = folderRepo.save(folder);
        return toDTO(saved);
    }

    public void deleteFolder(Long folderId, Authentication authentication) {
        Users user = getAuthenticatedUser(authentication);
        Folder folder = folderRepo.findByFolderIdAndUser(folderId, user)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Folder not found: " + folderId
                ));

        // Unassign documents from this folder (don't delete them)
        for (Document doc : folder.getDocuments()) {
            doc.setFolder(null);
            documentRepo.save(doc);
        }

        folderRepo.delete(folder);
    }

    public void assignDocumentToFolder(
            Long documentId,
            Long folderId,
            Authentication authentication
    ) {
        Users user = getAuthenticatedUser(authentication);

        Document document = documentRepo.findByDocumentIdAndUser(documentId, user)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Document not found: " + documentId
                ));

        Folder folder = null;
        if (folderId != null) {
            folder = folderRepo.findByFolderIdAndUser(folderId, user)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Folder not found: " + folderId
                    ));
        }

        document.setFolder(folder);
        documentRepo.save(document);
    }

    private FolderDTO toDTO(Folder folder) {
        List<DocumentResponse> docs = folder.getDocuments()
                .stream()
                .map(doc -> new DocumentResponse(
                        doc.getDocumentId(),
                        doc.getFileName(),
                        doc.getContentType(),
                        doc.getFileSize(),
                        doc.getStatus(),
                        doc.getUploadedAt()
                ))
                .toList();

        return new FolderDTO(
                folder.getFolderId(),
                folder.getName(),
                docs
        );
    }

    private Users getAuthenticatedUser(Authentication authentication) {
        return usersRepo.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
