package com.oro.api.modules.user.controller;

import com.oro.api.modules.user.dto.RegisterMerchantRequest;
import com.oro.api.modules.user.dto.UserResponse;
import com.oro.api.modules.user.service.UserService;
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
    public ResponseEntity<Void> login() {
        return ResponseEntity.ok().build();
    }
}
