package com.puuul.api.users.entity;

import com.puuul.api.common.entity.AbstractAuditableEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name ="users")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User extends AbstractAuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_key", nullable = false, unique = true)
    @Builder.Default
    private UUID externalKey = UUID.randomUUID();

    @Column(unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "phone_number", nullable = true)
    private String phoneNumber;
}