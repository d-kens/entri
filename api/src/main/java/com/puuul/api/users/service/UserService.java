package com.puuul.api.users.service;

import com.puuul.api.users.dto.CreateUserDto;
import com.puuul.api.users.dto.UserResponse;
import com.puuul.api.users.entity.User;
import com.puuul.api.users.exception.EmailAlreadyExist;
import com.puuul.api.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public UserResponse create(CreateUserDto userDto) {
        if (userRepository.existsByEmail(userDto.email())) {
            throw new EmailAlreadyExist();
        }

        User user = User.builder()
                .email(userDto.email())
                .lastName(userDto.lastName())
                .firstName(userDto.firstName())
                .passwordHash(userDto.password())
                .phoneNumber(userDto.phoneNumber())
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
