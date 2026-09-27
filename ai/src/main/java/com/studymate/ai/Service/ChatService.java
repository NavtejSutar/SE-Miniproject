package com.studymate.ai.Service;

import com.studymate.ai.Dto.*;
import com.studymate.ai.Entities.ChatMessages;
import com.studymate.ai.Entities.Conversation;
import com.studymate.ai.Entities.Users;
import com.studymate.ai.Enum.MessageRole;
import com.studymate.ai.Exception.ResourceNotFoundException;
import com.studymate.ai.Repo.ChatMessagesRepo;
import com.studymate.ai.Repo.ConversationRepo;
import com.studymate.ai.Repo.UsersRepo;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.document.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    private static final String ASSISTANT_SYSTEM_PROMPT = """
            You are StudyMate, an intelligent, insightful, and pedagogical academic tutor.
            
            Key Directives:
            1. Provide comprehensive, articulate, and well-structured answers using clean Markdown with clear headings, organized bullet points, and LaTeX/math expressions (e.g. $f(x)$ or \\[...\\]) where appropriate.
            2. When relevant study materials or excerpts are provided below, use them as your primary foundation to reflect the user's course scope, terminology, and key concepts.
            3. You have full freewill to elaborate, provide intuitive analogies, detailed step-by-step mathematical or conceptual derivations, and real-world examples to ensure thorough understanding.
            4. If a question is general knowledge or goes beyond the provided materials, answer it fully and accurately using your broad academic knowledge.
            5. Do NOT output raw or awkward inline tags like "### Source: ..." or "[Referencing study material]". The platform automatically presents source badges in the user interface.
            6. Always maintain a professional, encouraging, and clear teaching voice.
            """;

    private final ConversationRepo conversationRepo;
    private final UsersRepo usersRepo;
    private final ChatMessagesRepo chatMessagesRepo;
    private final ChatClient chatClient;
    private final RetrievalService retrievalService;

    public ChatService(
            ConversationRepo conversationRepo,
            UsersRepo usersRepo,
            ChatMessagesRepo chatMessagesRepo,
            ChatClient chatClient,
            RetrievalService retrievalService
    ) {
        this.conversationRepo = conversationRepo;
        this.usersRepo = usersRepo;
        this.chatMessagesRepo = chatMessagesRepo;
        this.chatClient = chatClient;
        this.retrievalService = retrievalService;
    }

    public ConversationDTO createChat(Authentication authentication, String title) {
        Users users = getAuthenticatedUser(authentication);
        Conversation conv = Conversation.builder()
                .user(users)
                .title(title)
                .build();
        Conversation saved = conversationRepo.save(conv);
        return new ConversationDTO(
                saved.getConversationId(),
                saved.getTitle()
        );
    }

    public List<ConversationDTO> getConversations(Authentication authentication) {
        Users users = getAuthenticatedUser(authentication);
        return conversationRepo.findByUser(users)
                .stream()
                .map(conv -> new ConversationDTO(
                        conv.getConversationId(),
                        conv.getTitle()
                ))
                .toList();
    }

    public List<MessagesResponse> getChatMessages(
            Long conversationId,
            Authentication authentication
    ) {
        Users users = getAuthenticatedUser(authentication);
        Conversation conv = conversationRepo
                .findByConversationIdAndUser(conversationId, users)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Conversation not found: " + conversationId
                ));

        return chatMessagesRepo.findByConversationOrderByCreatedAtAsc(conv)
                .stream()
                .map(msg -> new MessagesResponse(
                        msg.getRole(),
                        msg.getContent(),
                        msg.getCreatedAt()
                ))
                .toList();
    }

    /**
     * Enhanced RAG Q&A: retrieves user document chunks, attaches course context,
     * but gives the LLM freewill to explain concepts thoroughly and naturally.
     */
    public ChatResponse chat(ChatRequest request, Authentication authentication) {
        Users users = getAuthenticatedUser(authentication);

        Conversation conversation = conversationRepo
                .findByConversationIdAndUser(request.conversationId(), users)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Conversation not found: " + request.conversationId()
                ));

        // Save user message
        // Dynamically update conversation title to the first user message if it has a default title
        if (conversation.getTitle() == null || 
            conversation.getTitle().equalsIgnoreCase("New Conversation") || 
            conversation.getTitle().equalsIgnoreCase("New Study Session")) {
            String newTitle = request.prompt().trim();
            if (newTitle.length() > 40) {
                newTitle = newTitle.substring(0, 40) + "...";
            }
            conversation.setTitle(newTitle);
            conversationRepo.save(conversation);
        }

        ChatMessages userMessage = ChatMessages.builder()
                .role(MessageRole.USER)
                .content(request.prompt())
                .conversation(conversation)
                .build();
        chatMessagesRepo.save(userMessage);

        // Retrieve relevant chunks for this user
        List<Document> retrievedDocs = retrievalService.retrieveRelevantChunks(
                request.prompt(), users.getId()
        );
        String context = retrievalService.buildContext(retrievedDocs);

        // Extract structured source metadata for the frontend
        List<SourceDTO> sources = extractSources(retrievedDocs);

        // Build prompt with course context while giving LLM full explanatory freedom
        StringBuilder systemPrompt = new StringBuilder(ASSISTANT_SYSTEM_PROMPT);
        if (!context.isEmpty()) {
            systemPrompt.append("\n\n=== RELEVANT COURSE STUDY MATERIAL ===\n")
                    .append(context)
                    .append("=== END OF COURSE MATERIAL ===\n");
        }

        // Call LLM with chat memory
        String response = chatClient.prompt()
                .system(systemPrompt.toString())
                .advisors(advisor -> advisor
                        .param(ChatMemory.CONVERSATION_ID,
                                request.conversationId().toString()))
                .user(request.prompt())
                .call()
                .content();

        // Save assistant response
        ChatMessages assistantMessage = ChatMessages.builder()
                .role(MessageRole.ASSISTANT)
                .content(response)
                .conversation(conversation)
                .build();
        chatMessagesRepo.save(assistantMessage);

        return new ChatResponse(response, sources);
    }

    /**
     * Real-time token streaming with SSE.
     * Emits sources event first, then streams each token as Ollama generates it,
     * and finally saves the complete response to the database on completion.
     */
    public SseEmitter streamChat(ChatRequest request, Authentication authentication) {
        Users users = getAuthenticatedUser(authentication);

        Conversation conversation = conversationRepo
                .findByConversationIdAndUser(request.conversationId(), users)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Conversation not found: " + request.conversationId()
                ));

        // Dynamically update conversation title to the first user message if it has a default title
        if (conversation.getTitle() == null || 
            conversation.getTitle().equalsIgnoreCase("New Conversation") || 
            conversation.getTitle().equalsIgnoreCase("New Study Session")) {
            String newTitle = request.prompt().trim();
            if (newTitle.length() > 40) {
                newTitle = newTitle.substring(0, 40) + "...";
            }
            conversation.setTitle(newTitle);
            conversationRepo.save(conversation);
        }

        // Save user message
        ChatMessages userMessage = ChatMessages.builder()
                .role(MessageRole.USER)
                .content(request.prompt())
                .conversation(conversation)
                .build();
        chatMessagesRepo.save(userMessage);

        // Retrieve relevant chunks for this user
        List<Document> retrievedDocs = retrievalService.retrieveRelevantChunks(
                request.prompt(), users.getId()
        );
        String context = retrievalService.buildContext(retrievedDocs);

        // Extract structured source metadata for the frontend
        List<SourceDTO> sources = extractSources(retrievedDocs);

        // Build prompt with course context
        StringBuilder systemPrompt = new StringBuilder(ASSISTANT_SYSTEM_PROMPT);
        if (!context.isEmpty()) {
            systemPrompt.append("\n\n=== RELEVANT COURSE STUDY MATERIAL ===\n")
                    .append(context)
                    .append("=== END OF COURSE MATERIAL ===\n");
        }

        SseEmitter emitter = new SseEmitter(180000L); // 3-minute timeout

        // Immediately send sources event so frontend shows badges with zero delay!
        try {
            emitter.send(SseEmitter.event().name("sources").data(sources));
        } catch (IOException e) {
            emitter.completeWithError(e);
            return emitter;
        }

        StringBuilder fullResponse = new StringBuilder();

        // Stream tokens in real-time from Ollama
        chatClient.prompt()
                .system(systemPrompt.toString())
                .advisors(advisor -> advisor
                        .param(ChatMemory.CONVERSATION_ID,
                                request.conversationId().toString()))
                .user(request.prompt())
                .stream()
                .content()
                .subscribe(
                        token -> {
                            if (token != null && !token.isEmpty()) {
                                fullResponse.append(token);
                                try {
                                    emitter.send(SseEmitter.event().name("token").data(java.util.Map.of("token", token)));
                                } catch (Exception e) {
                                    // client may have disconnected
                                }
                            }
                        },
                        error -> {
                            log.error("Streaming error from Ollama: ", error);
                            try {
                                emitter.send(SseEmitter.event().name("error").data(java.util.Map.of("error", error.getMessage() != null ? error.getMessage() : "Error generating response")));
                            } catch (Exception ignored) {}
                            emitter.complete();
                        },
                        () -> {
                            try {
                                // Save full assistant response to DB
                                ChatMessages assistantMessage = ChatMessages.builder()
                                        .role(MessageRole.ASSISTANT)
                                        .content(fullResponse.toString())
                                        .conversation(conversation)
                                        .build();
                                chatMessagesRepo.save(assistantMessage);

                                emitter.send(SseEmitter.event().name("done").data(""));
                                emitter.complete();
                            } catch (Exception e) {
                                log.error("Error saving assistant message after stream: ", e);
                                emitter.complete();
                            }
                        }
                );

        return emitter;
    }

    public void deleteConversation(Long conversationId, Authentication authentication) {
        Users users = getAuthenticatedUser(authentication);
        Conversation conversation = conversationRepo
                .findByConversationIdAndUser(conversationId, users)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Conversation not found: " + conversationId
                ));
        conversationRepo.delete(conversation);
    }

    public ConversationDTO renameConversation(
            Long conversationId,
            String newTitle,
            Authentication authentication
    ) {
        Users users = getAuthenticatedUser(authentication);
        Conversation conversation = conversationRepo
                .findByConversationIdAndUser(conversationId, users)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Conversation not found: " + conversationId
                ));
        conversation.setTitle(newTitle);
        Conversation saved = conversationRepo.save(conversation);
        return new ConversationDTO(saved.getConversationId(), saved.getTitle());
    }

    private List<SourceDTO> extractSources(List<Document> documents) {
        return documents.stream()
                .map(doc -> {
                    var meta = doc.getMetadata();
                    Long docId = meta.get("documentId") instanceof Number n
                            ? n.longValue() : null;
                    String fileName = (String) meta.getOrDefault("fileName", "Unknown");
                    int pageNum = meta.get("pageNumber") instanceof Number n
                            ? n.intValue() : 0;
                    return new SourceDTO(docId, fileName, pageNum);
                })
                .distinct()
                .collect(Collectors.toList());
    }

    private Users getAuthenticatedUser(Authentication authentication) {
        return usersRepo.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
