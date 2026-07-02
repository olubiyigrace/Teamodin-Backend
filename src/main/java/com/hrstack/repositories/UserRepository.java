package com.hrstack.repositories;

import com.hrstack.entities.User;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByWorkspaceUrl(String workspaceUrl);
    User findByEmail(String email);
}
