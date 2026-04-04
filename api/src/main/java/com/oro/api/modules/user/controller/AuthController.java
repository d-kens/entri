package com.oro.api.modules.user.controller;

import com.oro.api.modules.user.dto.AuthRequest;
import com.oro.api.modules.user.dto.AuthResponse;
import com.oro.api.modules.user.dto.ChangePasswordRequest;
import com.oro.api.modules.user.dto.ForgotPasswordRequest;
import com.oro.api.modules.user.dto.RegisterMerchantRequest;
import com.oro.api.modules.user.dto.ResetPasswordRequest;
import com.oro.api.modules.user.dto.UserResponse;
import com.oro.api.modules.user.service.AuthService;
import com.oro.api.modules.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import com.oro.api.security.model.UserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
public class AuthController {
    private final UserService userService;
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(
            UriComponentsBuilder uriComponentsBuilder,
            @Valid @RequestBody RegisterMerchantRequest request
    ) {
        var response = userService.registerMerchant(request);
        var uri = uriComponentsBuilder.path("/users/{id}").buildAndExpand(response.externalId()).toUri();

        return ResponseEntity.created(uri).body(response);
    }

    @PostMapping("/login")
    public AuthResponse authenticate(
            @Valid @RequestBody AuthRequest request,
            HttpServletResponse response
    ) {

        return authService.authenticate(
                request, response
        );
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(
            @AuthenticationPrincipal UserPrincipal principal,
            HttpServletResponse response
    ) {
        authService.logout(principal.getUser(), response);
    }

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal UserPrincipal principal) {
        return userService.getCurrentUser(principal.getUsername());
    }

    @PostMapping("/forgot-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request,
            HttpServletRequest servletRequest
    ) {
        authService.forgotPassword(request.phoneNumber(), servletRequest.getRemoteAddr());
    }

    @PostMapping("/reset-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
    }

    @PostMapping("/change-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        authService.changePassword(principal.getUser(), request);
    }

    @PostMapping("/refresh-token")
    public AuthResponse refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        return authService.refreshToken(request, response);
    }
}
