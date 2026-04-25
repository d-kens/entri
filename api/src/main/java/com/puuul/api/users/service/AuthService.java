package com.puuul.api.users.service;


import com.puuul.api.users.dto.CreateUserDto;
import com.puuul.api.users.dto.LoginRequestDto;
import com.puuul.api.users.dto.LoginResponseDto;
import com.puuul.api.users.dto.UserResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserService userService;

    public UserResponseDto register(CreateUserDto userDto) {
        return userService.create(userDto);
    }

    public LoginResponseDto login(LoginRequestDto loginRequest) {
        return null;
    }
}
