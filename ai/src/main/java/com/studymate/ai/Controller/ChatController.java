package com.studymate.ai.Controller;

import com.studymate.ai.Dto.*;
import com.studymate.ai.Service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(
            @RequestBody ChatRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                chatService.chat(request, authentication)
        );
    }

    @PostMapping("/conversations")
    public ResponseEntity<ConversationDTO> createConversation(
            @RequestParam String title,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                chatService.createChat(authentication, title)
        );
    }

    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationDTO>> getConversations(
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                chatService.getConversations(authentication)
        );
    }

    @GetMapping("/conversations/{id}/messages")
    public ResponseEntity<List<MessagesResponse>> getChatMessages(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                chatService.getChatMessages(id, authentication)
        );
    }

    @PutMapping("/conversations/{id}")
    public ResponseEntity<ConversationDTO> renameConversation(
            @PathVariable Long id,
            @RequestParam String title,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                chatService.renameConversation(id, title, authentication)
        );
    }

    @DeleteMapping("/conversations/{id}")
    public ResponseEntity<Void> deleteConversation(
            @PathVariable Long id,
            Authentication authentication
    ) {
        chatService.deleteConversation(id, authentication);
        return ResponseEntity.noContent().build();
    }
}
