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
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private static final String GROUNDING_PROMPT = """
            You are a helpful study assistant. You MUST follow these rules strictly:
            
            1. Answer ONLY using the supplied study material below.
            2. If the answer cannot be found in the material, say: "I cannot answer this from the provided study material."
            3. Do NOT invent or hallucinate information.
            4. When referencing information, mention the source document and page number.
            5. Be clear, concise, and educational in your responses.
            
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
     * Grounded Q&A: retrieve relevant chunks, inject context, call LLM.
     * Returns structured response with answer + source citations.
     */
    public ChatResponse chat(ChatRequest request, Authentication authentication) {
        Users users = getAuthenticatedUser(authentication);

        Conversation conversation = conversationRepo
                .findByConversationIdAndUser(request.conversationId(), users)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Conversation not found: " + request.conversationId()
                ));

        // Save user message
        ChatMessages userMessage = ChatMessages.builder()
                .role(MessageRole.USER)
                .content(request.prompt())
                .conversation(conversation)
                .build();
        chatMessagesRepo.save(userMessage);

        // RAG: retrieve relevant chunks
        List<Document> retrievedDocs = retrievalService.retrieveRelevantChunks(
                request.prompt(), users.getId()
        );
        String context = retrievalService.buildContext(retrievedDocs);

        // Extract sources for the response
        List<SourceDTO> sources = extractSources(retrievedDocs);

        // Build prompt with grounding
        String systemPrompt = GROUNDING_PROMPT;
        if (!context.isEmpty()) {
            systemPrompt += context;
        } else {
            systemPrompt += "No study material is currently available. " +
                    "Let the user know they should upload documents first.";
        }

        // Call LLM with memory
        String response = chatClient.prompt()
                .system(systemPrompt)
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
