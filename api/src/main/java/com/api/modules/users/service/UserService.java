package com.api.modules.users.service;

import com.api.common.exception.NotFoundException;
import com.api.common.utils.PhoneNumberUtils;
import com.api.modules.users.dto.CreateUserRequest;
import com.api.modules.users.dto.UserResponse;
import com.api.modules.users.entity.Role;
import com.api.modules.users.entity.User;
import com.api.modules.users.event.UserCreatedEvent;
import com.api.modules.users.exception.EmailAlreadyExist;
import com.api.modules.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    public UserResponse create(CreateUserRequest userDto) {
        if (userRepository.existsByEmail(userDto.email())) {
            throw new EmailAlreadyExist();
        }
        User user = User.builder()
                .email(userDto.email())
                .lastName(userDto.lastName())
                .firstName(userDto.firstName())
                .phoneNumber(PhoneNumberUtils.normalize(userDto.phoneNumber()))
                .passwordHash(passwordEncoder.encode(userDto.password()))
                .role(Role.valueOf(userDto.role().toUpperCase()))
                .build();
        userRepository.save(user);
        eventPublisher.publishEvent(new UserCreatedEvent(user));
        return toResponse(user);
    }

    public UserResponse getUserByExternalKey(String externalKey) {
        return toResponse(findEntityByExternalKey(externalKey));
    }

    public User findEntityByExternalKey(String externalKey) {
        return userRepository.findByExternalKey(externalKey)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getRole().toString(),
                user.getEmail(),
                user.getLastName(),
                user.getFirstName(),
                user.getPhoneNumber(),
                user.getExternalKey().toString()
        );
    }
}
