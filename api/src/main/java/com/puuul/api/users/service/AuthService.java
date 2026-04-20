package com.puuul.api.users.service;


import com.puuul.api.users.dto.CreateUserDto;
import com.puuul.api.users.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserService userService;

    public UserResponse register(CreateUserDto userDto) {
        return userService.create(userDto);
    }
}
