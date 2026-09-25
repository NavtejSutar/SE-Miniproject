package com.studymate.ai.Repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.studymate.ai.Entities.ChatMessages;

public interface ChatMessagesRepo extends JpaRepository<ChatMessages,Long>{
    
}
