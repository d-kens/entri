package com.entri.modules.users.controller;

import com.entri.common.security.AuthenticatedUser;
import com.entri.modules.users.controller.api.UserApi;
import com.entri.modules.users.dto.UserResponse;
import com.entri.modules.users.service.UserService;
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
            @AuthenticationPrincipal final AuthenticatedUser user
    ) {
        return userService.getUserByExternalKey(externalKey, user);
    }
}