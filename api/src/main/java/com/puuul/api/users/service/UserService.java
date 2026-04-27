package com.puuul.api.users.service;

import com.puuul.api.common.exception.NotFoundException;
import com.puuul.api.users.dto.CreateUserDto;
import com.puuul.api.users.dto.UserResponseDto;
import com.puuul.api.users.entity.Role;
import com.puuul.api.users.entity.User;
import com.puuul.api.users.exception.EmailAlreadyExist;
import com.puuul.api.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponseDto create(CreateUserDto userDto) {
        if (userRepository.existsByEmail(userDto.email())) {
            throw new EmailAlreadyExist();
        }

        User user = User.builder()
                .email(userDto.email())
                .lastName(userDto.lastName())
                .firstName(userDto.firstName())
                .phoneNumber(userDto.phoneNumber())
                .passwordHash(passwordEncoder.encode(userDto.password()))
                .role(Role.valueOf(userDto.role().toUpperCase()))
                .build();

        userRepository.save(user);

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
