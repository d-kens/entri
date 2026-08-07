package com.entri.modules.users.controller.api;

import com.entri.modules.users.dto.AccessToken;
import com.entri.modules.users.dto.CreateUserRequest;
import com.entri.modules.users.dto.ForgotPasswordRequest;
import com.entri.modules.users.dto.LoginRequest;
import com.entri.modules.users.dto.LoginResponse;
import com.entri.modules.users.dto.ResetPasswordRequest;
import com.entri.modules.users.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.util.UriComponentsBuilder;

@RequestMapping("/auth")
public interface AuthApi {

    @Operation(
            operationId = "register",
            summary = "Register User",
            description = "Creates a new user account with the provided details. On success, the API returns the newly created user"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "400",
                    description = "The request is invalid. One or more validation errors were found",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "A user with the provided email already exists",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "201",
                    description = "User registered successfully"
            )
    })
    @PostMapping("/register")
    ResponseEntity<UserResponse> register(
            @Parameter(hidden = true) UriComponentsBuilder uriComponentsBuilder,
            @Valid @RequestBody CreateUserRequest userDto
    );

    @Operation(
            operationId = "login",
            summary = "Login",
            description = "Authenticates a user with the provided credentials. On success, the API returns an access token and sets a refresh token cookie"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "400",
                    description = "The request is invalid. One or more validation errors were found",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "The provided credentials are invalid",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "200",
                    description = "Login successful"
            )
    })
    @PostMapping("/login")
    LoginResponse login(
            @Parameter(hidden = true) HttpServletResponse response,
            @Valid @RequestBody LoginRequest loginRequest
    );

    @Operation(
            operationId = "forgotPassword",
            summary = "Forgot Password",
            description = "Initiates the password reset flow by sending a reset link to the provided email address if it exists in the system"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "400",
                    description = "The request is invalid. One or more validation errors were found",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "200",
                    description = "Reset link sent if the email was found"
            )
    })
    @PostMapping("/forgot-password")
    ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request);

    @Operation(
            operationId = "resetPassword",
            summary = "Reset Password",
            description = "Resets the user's password using the token received in the password reset email"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "400",
                    description = "The request is invalid or the reset token has expired",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "204",
                    description = "Password reset successfully"
            )
    })
    @PostMapping("/reset-password")
    ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request);

    @Operation(
            operationId = "refreshToken",
            summary = "Refresh Access Token",
            description = "Issues a new access token using the refresh token stored in the HTTP-only cookie"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "401",
                    description = "The refresh token is missing, invalid, or expired",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "200",
                    description = "Access token refreshed successfully"
            )
    })
    @PostMapping("/refresh-token")
    AccessToken refreshToken(
            @Parameter(hidden = true) HttpServletResponse response,
            @Parameter(hidden = true) @CookieValue(value = "refresh_token") String refreshToken
    );

    @Operation(
            operationId = "logout",
            summary = "Logout",
            description = "Invalidates the current session by clearing the refresh token cookie"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Logged out successfully"
            )
    })
    @PostMapping("/logout")
    ResponseEntity<Void> logout(
            @Parameter(hidden = true) HttpServletRequest request,
            @Parameter(hidden = true) HttpServletResponse response
    );
}
