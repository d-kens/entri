package com.puuul.api.modules.users.service;

import com.puuul.api.common.exception.NotFoundException;
import com.puuul.api.common.utils.PhoneNumberUtils;
import com.puuul.api.modules.users.dto.CreateUserDto;
import com.puuul.api.modules.users.dto.UserResponseDto;
import com.puuul.api.modules.users.entity.Role;
import com.puuul.api.modules.users.entity.User;
import com.puuul.api.modules.users.event.UserCreatedEvent;
import com.puuul.api.modules.users.exception.EmailAlreadyExist;
import com.puuul.api.modules.users.repository.UserRepository;
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

    public UserResponseDto create(CreateUserDto userDto) {
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

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private UserResponseDto toResponse(User user) {
        return new UserResponseDto(
                user.getRole().toString(),
                user.getEmail(),
                user.getLastName(),
                user.getFirstName(),
                user.getPhoneNumber(),
                user.getExternalKey().toString()
        );
    }
}
