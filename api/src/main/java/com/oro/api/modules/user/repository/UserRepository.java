package com.oro.api.modules.user.repository;

import com.oro.api.modules.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByPhoneNumber(String phoneNumber);
    Optional<User> findUserByPhoneNumber(String phoneNumber);
    Optional<User> findByExternalId(String externalId);
}