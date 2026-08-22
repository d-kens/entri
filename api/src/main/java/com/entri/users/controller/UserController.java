package com.entri.users.controller;

import com.entri.security.UserPrincipal;
import com.entri.users.controller.api.UserApi;
import com.entri.users.dto.UserResponse;
import com.entri.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController implements UserApi {
    private final UserService userService;

    @Override
    public UserResponse getUserByExternalKey(
            @PathVariable String externalKey,
            @AuthenticationPrincipal final UserPrincipal user
    ) {
        return userService.getUserByExternalKey(externalKey, user);
    }
}