package com.studymate.ai.Repo;

import com.studymate.ai.Entities.Document;
import com.studymate.ai.Entities.DocumentPage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PageRepo extends JpaRepository<DocumentPage, Long> {
    List<DocumentPage> findByDocumentOrderByPageNumberAsc(Document document);
}
