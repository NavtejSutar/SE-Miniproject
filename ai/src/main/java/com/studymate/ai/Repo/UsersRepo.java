package com.studymate.ai.Repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.studymate.ai.Entities.Users;

public interface UsersRepo extends JpaRepository<Users,Long>{
    Optional<Users> findByEmail(String email);
}
