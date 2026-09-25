package com.studymate.ai.Controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatController {

    private final ChatMemory chatMemory;
    private final ChatClient chatClient;

    public ChatController(
            ChatClient chatClient,
            ChatMemory chatMemory
    ){
        this.chatClient=chatClient;
        this.chatMemory=chatMemory;
    }

    @GetMapping("/chat")
    public String chat(
            @RequestParam String conversationId,
            @RequestParam String prompt
    ){
        return chatClient.prompt()
                .advisors(advisor->advisor
                        .param(ChatMemory.CONVERSATION_ID,conversationId))
                .user(prompt)
                .call()
                .content();
    }
}
