package com.entri.users.controller;

import com.entri.security.UserPrincipal;
import com.entri.users.controller.api.UserApi;
import com.entri.users.dto.UpdateUserRequest;
import com.entri.users.dto.UserResponse;
import com.entri.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController implements UserApi {
    private final UserService userService;

    @Override
    public UserResponse getUserByExternalKey(final String externalKey, final UserPrincipal requestingUser) {
        return userService.getUserByExternalKey(externalKey, requestingUser);
    }

    @Override
    public UserResponse updateUser(final String externalKey, final UpdateUserRequest updateUserRequest, final UserPrincipal requestingUser) {
        return userService.updateUser(externalKey, updateUserRequest, requestingUser);
    }
}
