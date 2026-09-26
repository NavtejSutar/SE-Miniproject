package com.studymate.ai.Service;

import com.studymate.ai.Dto.ConversationDTO;
import com.studymate.ai.Dto.MessagesResponse;
import com.studymate.ai.Entities.ChatMessages;
import com.studymate.ai.Entities.Conversation;
import com.studymate.ai.Entities.Users;
import com.studymate.ai.Enum.MessageRole;
import com.studymate.ai.Repo.ChatMessagesRepo;
import com.studymate.ai.Repo.ConversationRepo;
import com.studymate.ai.Repo.UsersRepo;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChatService {

    private final ConversationRepo conversationRepo;
    private final UsersRepo usersRepo;
    private final ChatMessagesRepo chatMessagesRepo;
    private final ChatClient chatClient;

    public ChatService(
            ConversationRepo conversationRepo,
            UsersRepo usersRepo,
            ChatMessagesRepo chatMessagesRepo,
            ChatClient chatClient
    ){
        this.conversationRepo=conversationRepo;
        this.usersRepo=usersRepo;
        this.chatMessagesRepo= chatMessagesRepo;
        this.chatClient=chatClient;
    }

    public ConversationDTO createChat(Authentication authentication, String title){
        Users users = usersRepo.findByEmail(authentication.getName()).orElseThrow();
        Conversation conv= Conversation.builder()
                .user(users)
                .title(title)
                .build();
        Conversation saved= conversationRepo.save(conv);
        return new ConversationDTO(
                saved.getConversationId(),
                saved.getTitle()
        );
    }

    public List<ConversationDTO> getConversations(Authentication authentication){
        Users users = usersRepo.findByEmail(authentication.getName()).orElseThrow();
        List<Conversation> ls=conversationRepo.findByUser(users);
        List<ConversationDTO> res=new ArrayList<>();
        for(Conversation conv: ls){
            ConversationDTO temp=new ConversationDTO(
                    conv.getConversationId(),
                    conv.getTitle()
            );
            res.add(temp);
        }
        return res;
    }

    public List<MessagesResponse> getChatMessages(
            Long conversationId,
            Authentication authentication
    ){
        Users users= usersRepo.findByEmail(authentication.getName()).orElseThrow();
        Conversation conv = conversationRepo.findByConversationIdAndUser(conversationId,users).orElseThrow();
        List<ChatMessages> ls= chatMessagesRepo.findByConversationOrderByCreatedAtAsc(conv);
        List<MessagesResponse> res=new ArrayList<>();
        for(ChatMessages chatMessages:ls){
            MessagesResponse temp=new MessagesResponse(
                    chatMessages.getRole(),
                    chatMessages.getContent(),
                    chatMessages.getCreatedAt()
            );
            res.add(temp);
        }
        return res;
    }

    public String chat(
            Long conversationId,
            String prompt,
            Authentication authentication
    ){
        Users users= usersRepo.findByEmail(
                authentication.getName()
        ).orElseThrow();

        Conversation conversation=conversationRepo
                .findByConversationIdAndUser(conversationId,users)
                .orElseThrow();

        ChatMessages chatMessages= ChatMessages.builder()
                .role(MessageRole.USER)
                .content(prompt)
                .conversation(conversation)
                .build();

        chatMessagesRepo.save(chatMessages);

        String response=chatClient.prompt()
                .advisors(advisor -> advisor
                        .param(ChatMemory.CONVERSATION_ID, conversationId.toString()))
                .user(prompt)
                .call()
                .content();

        ChatMessages chatResponse= ChatMessages.builder()
                .role(MessageRole.ASSISTANT)
                .content(response)
                .conversation(conversation)
                .build();

        chatMessagesRepo.save(chatResponse);

        return response;

    }
}
