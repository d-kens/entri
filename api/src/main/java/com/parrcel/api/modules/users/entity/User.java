package com.parrcel.api.modules.users.entity;

import com.parrcel.api.common.entity.AbstractAuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "users")
public class User extends AbstractAuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "phone_number", nullable = false, unique = true)
    private String phoneNumber;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    private boolean active = true;

    @Embedded
    private TwoFactorCode twoFactorCode;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    public void requestOtp(int minutesValid) {
        twoFactorCode.generate(minutesValid);
    }

    public boolean verifyOtp(String code) {
        return twoFactorCode.verify(code);
    }
}
