package com.entri.users.controller;

import com.entri.security.UserPrincipal;
import com.entri.users.controller.api.UserApi;
import com.entri.users.dto.ChangePasswordRequest;
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

    @Override
    public void changePassword(final String externalKey, final ChangePasswordRequest changePasswordRequest, final UserPrincipal requestingUser) {
        userService.changePassword(externalKey, changePasswordRequest, requestingUser);
    }
    // TODO: DELETE /users/{externalKey} — delete a user (admin or self)
    // TODO: POST /users/{externalKey}/enable — admin only, set user enabled = true
    // TODO: POST /users/{externalKey}/disable — admin only, set user enabled = false
    // TODO: GET /users — admin only, return a paginated list of all users
}
