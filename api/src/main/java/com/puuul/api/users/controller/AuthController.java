package com.puuul.api.users.controller;

import com.puuul.api.users.dto.CreateUserDto;
import com.puuul.api.users.dto.UserResponse;
import com.puuul.api.users.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(
            UriComponentsBuilder uriComponentsBuilder,
            @Valid @RequestBody CreateUserDto userDto
    ) {
        var response = authService.register(userDto);
        var uri = uriComponentsBuilder.path("/users/{id}").buildAndExpand(response.externalKey()).toUri();
        return ResponseEntity.created(uri).body(response);
    }
}
