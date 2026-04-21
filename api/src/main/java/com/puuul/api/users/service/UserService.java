package com.puuul.api.users.service;

import com.puuul.api.users.dto.CreateUserDto;
import com.puuul.api.users.dto.UserResponse;
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

    public UserResponse create(CreateUserDto userDto) {
        if (userRepository.existsByEmail(userDto.email())) {
            throw new EmailAlreadyExist();
        }

        User user = User.builder()
                .email(userDto.email())
                .lastName(userDto.lastName())
                .firstName(userDto.firstName())
                .phoneNumber(userDto.phoneNumber())
                .passwordHash(passwordEncoder.encode(userDto.password()))
                .build();

        userRepository.save(user);

        return toResponse(user);
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getEmail(),
                user.getExternalKey().toString(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhoneNumber()
        );
    }
}
