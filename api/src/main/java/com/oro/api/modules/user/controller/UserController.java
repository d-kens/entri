package com.oro.api.modules.user.controller;

import com.oro.api.common.dto.PageResponse;
import com.oro.api.modules.user.dto.CreateUserRequest;
import com.oro.api.modules.user.dto.UpdateUserRequest;
import com.oro.api.modules.user.dto.UserResponse;
import com.oro.api.modules.user.service.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/users")
@AllArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class UserController {
    private final UserService userService;

    @GetMapping
    public ResponseEntity<PageResponse<UserResponse>> getUsers(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(userService.getUsers(pageable));
    }

    @GetMapping("/{externalId}")
    public ResponseEntity<UserResponse> getUser(@PathVariable String externalId) {
        return ResponseEntity.ok(userService.getUserByExternalId(externalId));
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(
            UriComponentsBuilder uriComponentsBuilder,
            @Valid @RequestBody CreateUserRequest request
    ) {
        var response = userService.createUser(request);
        var uri = uriComponentsBuilder.path("/users/{id}").buildAndExpand(response.externalId()).toUri();

        return ResponseEntity.created(uri).body(response);
    }

    @PatchMapping("/{externalId}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable String externalId,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        return ResponseEntity.ok(userService.updateUser(externalId, request));
    }

    @DeleteMapping("/{externalId}")
    public ResponseEntity<Void> deactivateUser(@PathVariable String externalId) { // TODO: Will return user object with the token (token is transient field)
        userService.deactivateUser(externalId);
        return ResponseEntity.noContent().build();
    }
}
