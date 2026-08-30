package com.entri.users.controller;

import com.entri.common.dto.PaginationResponse;
import com.entri.security.UserPrincipal;
import com.entri.users.controller.api.UserApi;
import com.entri.users.dto.ChangePasswordRequest;
import com.entri.users.dto.UpdateUserRequest;
import com.entri.users.dto.UserResponse;
import com.entri.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
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

    @Override
    public PaginationResponse<UserResponse> listUsers(final Pageable pageable, final UserPrincipal requestingUser) {
        return userService.listUsers(pageable);
    }

    @Override
    public void deleteUser(final String externalKey, final UserPrincipal requestingUser) {
        userService.deleteUser(externalKey, requestingUser);
    }

    @Override
    public void enableUser(final String externalKey, final UserPrincipal requestingUser) {
        userService.setUserEnabled(externalKey, true);
    }

    @Override
    public void disableUser(final String externalKey, final UserPrincipal requestingUser) {
        userService.setUserEnabled(externalKey, false);
    }
}
