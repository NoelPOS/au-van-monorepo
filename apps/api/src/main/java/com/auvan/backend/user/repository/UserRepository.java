package com.auvan.backend.user.repository;

import com.auvan.backend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    Optional<User> findByLineUserId(String lineUserId);

    boolean existsByEmail(String email);

    List<User> findByIsAdminTrue();
}
