package com.parrcel.api.modules.users.controller;

import com.parrcel.api.modules.users.dto.CreateUserDto;
import com.parrcel.api.modules.users.dto.UserResponse;
import com.parrcel.api.modules.users.mapper.UserMapper;
import com.parrcel.api.modules.users.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserMapper userMapper;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponse> createUser(
            UriComponentsBuilder uriComponentsBuilder,
            @Valid @RequestBody CreateUserDto createUserDto
    ) {
        var user = userService.createUser(createUserDto);
        var response = userMapper.toResponse(user);
        var uri = uriComponentsBuilder.path("/users/{userId}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(uri).body(response);
    }
}