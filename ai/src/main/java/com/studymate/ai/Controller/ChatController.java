package com.studymate.ai.Controller;

import com.studymate.ai.Dto.ConversationDTO;
import com.studymate.ai.Dto.MessagesResponse;
import com.studymate.ai.Entities.Conversation;
import com.studymate.ai.Entities.Users;
import com.studymate.ai.Service.ChatService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ChatController {

    private final ChatService chatService;

    public ChatController(
            ChatService chatService
    ){
        this.chatService=chatService;
    }

    @GetMapping("/chat")
    public String chat(
            @RequestParam Long conversationId,
            @RequestParam String prompt,
            Authentication authentication
    ){
        return chatService.chat(conversationId, prompt, authentication);
    }

    @PostMapping("/chat")
    public ConversationDTO createChat(Authentication authentication , @RequestParam String title){
        return chatService.createChat(authentication, title);
    }

    @GetMapping("/conversations")
    public List<ConversationDTO> getConversations(Authentication authentication){
        return chatService.getConversations(authentication);
    }

    @GetMapping("/chatMessages")
    public List<MessagesResponse> getChatMessages(
            Long conversationId,
            Authentication authentication
    ){
        return chatService.getChatMessages(conversationId, authentication);
    }


}
