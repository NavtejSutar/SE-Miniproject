package com.studymate.ai.Repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.studymate.ai.Entities.Conversation;

public interface ConversationRepo extends JpaRepository<Conversation,Long>{
    
}
