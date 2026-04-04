package com.oro.api.modules.user.controller;

import com.oro.api.modules.user.dto.AuthRequest;
import com.oro.api.modules.user.dto.AuthResponse;
import com.oro.api.modules.user.dto.RegisterMerchantRequest;
import com.oro.api.modules.user.dto.UserResponse;
import com.oro.api.modules.user.service.AuthService;
import com.oro.api.modules.user.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
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
    public ResponseEntity<AuthResponse> authenticate(
            @Valid @RequestBody AuthRequest request,
            HttpServletResponse response
    ) {
        return authService.authenticate(request, response)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }
}
