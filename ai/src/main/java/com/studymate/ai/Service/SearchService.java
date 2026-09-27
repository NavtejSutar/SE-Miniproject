package com.studymate.ai.Service;

import com.studymate.ai.Dto.ConversationDTO;
import com.studymate.ai.Dto.DocumentResponse;
import com.studymate.ai.Dto.SearchResponse;
import com.studymate.ai.Entities.Users;
import com.studymate.ai.Exception.ResourceNotFoundException;
import com.studymate.ai.Repo.ConversationRepo;
import com.studymate.ai.Repo.DocumentRepo;
import com.studymate.ai.Repo.UsersRepo;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SearchService {

    private final DocumentRepo documentRepo;
    private final ConversationRepo conversationRepo;
    private final UsersRepo usersRepo;

    public SearchService(
            DocumentRepo documentRepo,
            ConversationRepo conversationRepo,
            UsersRepo usersRepo
    ) {
        this.documentRepo = documentRepo;
        this.conversationRepo = conversationRepo;
        this.usersRepo = usersRepo;
    }

    public SearchResponse search(String query, Authentication authentication) {
        Users user = usersRepo.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Search documents by file name
        List<DocumentResponse> documents = documentRepo
                .findByUserAndFileNameContainingIgnoreCase(user, query)
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

        // Search conversations by title
        List<ConversationDTO> conversations = conversationRepo
                .findByUserAndTitleContainingIgnoreCase(user, query)
                .stream()
                .map(conv -> new ConversationDTO(
                        conv.getConversationId(),
                        conv.getTitle()
                ))
                .toList();

        return new SearchResponse(documents, conversations);
    }
}
