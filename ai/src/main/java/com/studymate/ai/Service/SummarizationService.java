package com.studymate.ai.Service;

import com.studymate.ai.Dto.SummaryResponse;
import com.studymate.ai.Entities.Document;
import com.studymate.ai.Entities.DocumentPage;
import com.studymate.ai.Entities.Users;
import com.studymate.ai.Exception.ResourceNotFoundException;
import com.studymate.ai.Repo.DocumentRepo;
import com.studymate.ai.Repo.PageRepo;
import com.studymate.ai.Repo.UsersRepo;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SummarizationService {

    private static final String SUMMARIZATION_PROMPT = """
            You are a study assistant. Summarize the following study material clearly and concisely.
            
            Rules:
            1. Create a structured summary with key topics as bullet points.
            2. Highlight important concepts, definitions, and formulas.
            3. Do NOT invent information that is not in the material.
            4. Use clear, educational language suitable for students.
            5. If the material covers multiple topics, organize by topic.
            
            Study Material:
            
            """;

    private final DocumentRepo documentRepo;
    private final PageRepo pageRepo;
    private final UsersRepo usersRepo;
    private final ChatClient chatClient;

    public SummarizationService(
            DocumentRepo documentRepo,
            PageRepo pageRepo,
            UsersRepo usersRepo,
            ChatClient chatClient
    ) {
        this.documentRepo = documentRepo;
        this.pageRepo = pageRepo;
        this.usersRepo = usersRepo;
        this.chatClient = chatClient;
    }

    /**
     * Summarize an entire document or specific pages.
     *
     * @param documentId  the document to summarize
     * @param pageNumbers optional list of page numbers to summarize (null = all)
     */
    public SummaryResponse summarize(
            Long documentId,
            List<Integer> pageNumbers,
            Authentication authentication
    ) {
        Users user = usersRepo.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Document document = documentRepo.findByDocumentIdAndUser(documentId, user)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Document not found: " + documentId
                ));

        List<DocumentPage> pages = pageRepo.findByDocumentOrderByPageNumberAsc(document);

        if (pages.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No extracted pages found for document: " + documentId
            );
        }

        // Filter to specific pages if requested
        if (pageNumbers != null && !pageNumbers.isEmpty()) {
            pages = pages.stream()
                    .filter(p -> pageNumbers.contains(p.getPageNumber()))
                    .toList();

            if (pages.isEmpty()) {
                throw new ResourceNotFoundException(
                        "No pages found matching the requested page numbers"
                );
            }
        }

        // Build text from pages
        String fullText = pages.stream()
                .map(p -> "--- Page " + p.getPageNumber() + " ---\n" + p.getExtractedText())
                .collect(Collectors.joining("\n\n"));

        // Truncate if too long for the model context
        if (fullText.length() > 15000) {
            fullText = fullText.substring(0, 15000) + "\n\n[Text truncated...]";
        }

        // Call LLM
        String summary = chatClient.prompt()
                .system(SUMMARIZATION_PROMPT + fullText)
                .user("Please summarize this study material.")
                .call()
                .content();

        return new SummaryResponse(
                document.getDocumentId(),
                document.getFileName(),
                summary
        );
    }
}
