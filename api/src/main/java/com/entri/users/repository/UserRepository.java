package com.entri.users.repository;

import com.entri.users.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmailAndDeletedFalse(String email);
    Optional<User> findByEmailAndDeletedFalse(String email);
    Optional<User> findByExternalKeyAndDeletedFalse(String externalKey);
    Page<User> findAllByDeletedFalse(Pageable pageable);
}
