package com.studymate.ai.Controller;

import com.studymate.ai.Dto.FolderDTO;
import com.studymate.ai.Service.FolderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/folders")
public class FolderController {

    private final FolderService folderService;

    public FolderController(FolderService folderService) {
        this.folderService = folderService;
    }

    @PostMapping
    public ResponseEntity<FolderDTO> createFolder(
            @RequestParam String name,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                folderService.createFolder(name, authentication)
        );
    }

    @GetMapping
    public ResponseEntity<List<FolderDTO>> listFolders(
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                folderService.listFolders(authentication)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<FolderDTO> renameFolder(
            @PathVariable Long id,
            @RequestParam String name,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                folderService.renameFolder(id, name, authentication)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFolder(
            @PathVariable Long id,
            Authentication authentication
    ) {
        folderService.deleteFolder(id, authentication);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{folderId}/documents/{documentId}")
    public ResponseEntity<Void> assignDocument(
            @PathVariable Long folderId,
            @PathVariable Long documentId,
            Authentication authentication
    ) {
        folderService.assignDocumentToFolder(documentId, folderId, authentication);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/documents/{documentId}")
    public ResponseEntity<Void> unassignDocument(
            @PathVariable Long documentId,
            Authentication authentication
    ) {
        folderService.assignDocumentToFolder(documentId, null, authentication);
        return ResponseEntity.ok().build();
    }
}
