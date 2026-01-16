package com.parrcel.api.modules.user.controller;


import com.parrcel.api.modules.user.dto.CreateUserDto;
import com.parrcel.api.modules.user.dto.UserResponse;
import com.parrcel.api.modules.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody CreateUserDto createUserDto
    ) {
        var created = userService.createUser(createUserDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}