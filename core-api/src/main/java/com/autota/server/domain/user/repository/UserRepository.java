package com.autota.server.domain.user.repository;

import com.autota.server.domain.user.entity.Role;
import com.autota.server.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Role findRoleById(Long id);
}
