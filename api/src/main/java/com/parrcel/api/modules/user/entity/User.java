package com.parrcel.api.modules.user.entity;

import com.parrcel.api.modules.user.enums.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "user_name")
    private String userName;

    private String email;

    @Column(name = "password_hash")
    private String passwordHash;

    private String phone;

    @Enumerated(EnumType.STRING)
    private Role role = Role.MERCHANT;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" +
                "id = " + id + ", " +
                "userName = " + userName + ", " +
                "email = " + email + ", " +
                "phone = " + phone + ", " +
                "role = " + role + ")";
    }
}