package com.studymate.ai.Repo;

import com.studymate.ai.Entities.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

import com.studymate.ai.Entities.ChatMessages;

import java.util.List;

public interface ChatMessagesRepo extends JpaRepository<ChatMessages,Long>{
    List<ChatMessages> findByConversationOrderByCreatedAtAsc(Conversation conversation);     
}
