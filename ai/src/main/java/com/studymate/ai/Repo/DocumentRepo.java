package com.studymate.ai.Repo;

import com.studymate.ai.Entities.Document;
import com.studymate.ai.Entities.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DocumentRepo extends JpaRepository<Document,Long> {
    List<Document> findByUser(Users users);

    Optional<Document> findByDocumentIdAndUser(Long documentId, Users user);

    List<Document> findByUserAndFileNameContainingIgnoreCase(Users user, String query);
}
