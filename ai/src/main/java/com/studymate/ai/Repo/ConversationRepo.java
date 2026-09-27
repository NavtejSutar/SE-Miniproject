package com.studymate.ai.Repo;

import com.studymate.ai.Entities.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import com.studymate.ai.Entities.Conversation;

import java.util.List;
import java.util.Optional;

public interface ConversationRepo extends JpaRepository<Conversation,Long>{
    List<Conversation> findByUser(Users users);
    Optional<Conversation> findByConversationIdAndUser(Long conversationId, Users users);
    List<Conversation> findByUserAndTitleContainingIgnoreCase(Users user, String query);
}
