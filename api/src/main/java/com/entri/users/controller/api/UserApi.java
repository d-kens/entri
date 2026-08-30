package com.entri.users.controller.api;

import com.entri.security.UserPrincipal;
import com.entri.users.dto.ChangePasswordRequest;
import com.entri.users.dto.UpdateUserRequest;
import com.entri.users.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

@RequestMapping("/users")
public interface UserApi {

    @Operation(
            operationId = "getUserByExternalKey",
            summary = "Get User by External Key",
            description = "Retrieves the profile of a user identified by their external key. On success, the API returns the user details"
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is required or the access token is invalid",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "The authenticated user is not authorized to view this user",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "The specified user was not found",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "200",
                    description = "User retrieved successfully"
            )
    })
    @GetMapping("/{externalKey}")
    UserResponse getUserByExternalKey(
            @Parameter(
                    description = "The unique external key of the user",
                    required = true
            )
            @PathVariable final String externalKey,
            @Parameter(hidden = true) @AuthenticationPrincipal final UserPrincipal requestingUser
    );

    @Operation(
            operationId = "updateUser",
            summary = "Update a user",
            description = "Updates the details of an existing user."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is required or the access token is invalid",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "The authenticated user is not authorized to update this user",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "The specified user was not found",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "The provided email address is already in use by another account",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "200",
                    description = "User details updated successfully"
            )
    })
    @PutMapping("/{externalKey}")
    UserResponse updateUser(
            @Parameter(
                    description = "The unique external key of the user to update",
                    required = true,
                    example = "550e8400-e29b-41d4-a716-446655440000"
            )
            @PathVariable final String externalKey,

            @RequestBody(
                    description = "The updated user details.",
                    required = true
            )
            @Valid
            @org.springframework.web.bind.annotation.RequestBody final UpdateUserRequest updateUserRequest,

            @Parameter(hidden = true)
            @AuthenticationPrincipal final UserPrincipal requestingUser
    );

    @Operation(
            operationId = "changePassword",
            summary = "Change password",
            description = "Changes the authenticated user's password. Requires the current password for verification."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Password changed successfully"),
            @ApiResponse(
                    responseCode = "401",
                    description = "Current password is incorrect or token is invalid",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Not authorized to change this user's password",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/{externalKey}/change-password")
    void changePassword(
            @PathVariable final String externalKey,

            @Valid
            @org.springframework.web.bind.annotation.RequestBody final ChangePasswordRequest changePasswordRequest,

            @Parameter(hidden = true)
            @AuthenticationPrincipal final UserPrincipal requestingUser
    );

}
