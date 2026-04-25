package com.puuul.api.users.service;


import com.puuul.api.users.dto.CreateUserDto;
import com.puuul.api.users.dto.LoginRequestDto;
import com.puuul.api.users.dto.LoginResponseDto;
import com.puuul.api.users.dto.UserResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    public UserResponseDto register(CreateUserDto userDto) {
        return userService.create(userDto);
    }

    public LoginResponseDto login(LoginRequestDto loginRequest) {
        Authentication authRequest = UsernamePasswordAuthenticationToken.unauthenticated(
                loginRequest.email(), loginRequest.password()
        );
        Authentication authResponse = authenticationManager.authenticate(authRequest);
        return null;
    }
}
