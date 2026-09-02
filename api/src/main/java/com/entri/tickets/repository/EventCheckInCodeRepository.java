package com.entri.tickets.repository;

import com.entri.tickets.entity.EventCheckInCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface EventCheckInCodeRepository extends JpaRepository<EventCheckInCode, Long> {

    @Query("SELECT c FROM EventCheckInCode c JOIN FETCH c.event WHERE c.code = :code AND c.expiresAt > :now")
    Optional<EventCheckInCode> findValidCode(@Param("code") String code, @Param("now") Instant now);
}
