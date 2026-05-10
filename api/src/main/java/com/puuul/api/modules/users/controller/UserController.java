package com.puuul.api.modules.users.controller;

import com.puuul.api.modules.users.dto.UserResponse;
import com.puuul.api.modules.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/{externalKey}")
    public UserResponse getUserByExternalKey(
            @PathVariable String externalKey
    ) {
        return userService.getUserByExternalKey(externalKey);
    }
}