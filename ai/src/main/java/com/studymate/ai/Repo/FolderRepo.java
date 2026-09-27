package com.studymate.ai.Repo;

import com.studymate.ai.Entities.Folder;
import com.studymate.ai.Entities.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FolderRepo extends JpaRepository<Folder, Long> {
    List<Folder> findByUser(Users user);
    Optional<Folder> findByFolderIdAndUser(Long folderId, Users user);
}
