package com.entri.modules.auth.controller;

import com.entri.auth.controller.AuthController;
import com.entri.auth.dto.AccessToken;
import com.entri.auth.dto.ForgotPasswordRequest;
import com.entri.auth.dto.LoginRequest;
import com.entri.auth.dto.LoginResponse;
import com.entri.auth.dto.LoginResult;
import com.entri.auth.dto.ResetPasswordRequest;
import com.entri.auth.service.AuthService;
import com.entri.auth.service.PasswordResetService;
import com.entri.exception.GlobalExceptionHandler;
import com.entri.exception.UnauthorizedException;
import com.entri.security.JwtConfig;
import com.entri.users.dto.CreateUserRequest;
import com.entri.users.dto.UserResponse;
import com.entri.users.exception.EmailAlreadyExistsException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private JwtConfig jwtConfig;

    @Mock
    private AuthService authService;

    @Mock
    private PasswordResetService passwordResetService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(jwtConfig, authService, passwordResetService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void register_validRequest_returnsCreatedWithLocationHeader() throws Exception {
        var request = new CreateUserRequest("jane@example.com", "Password123!", "Jane", "Doe", "0712345678", "ORGANIZER");
        var response = new UserResponse("ORGANIZER", "jane@example.com", "Doe", "Jane", "0712345678", "ext-1", true);
        when(authService.register(request)).thenReturn(response);

        mockMvc.perform(post("/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("/users/ext-1")))
                .andExpect(jsonPath("$.externalKey").value("ext-1"));
    }

    @Test
    void register_invalidEmail_returnsBadRequest() throws Exception {
        var request = new CreateUserRequest("not-an-email", "Password123!", "Jane", "Doe", "0712345678", "ORGANIZER");

        mockMvc.perform(post("/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_emailAlreadyExists_returnsConflict() throws Exception {
        var request = new CreateUserRequest("jane@example.com", "Password123!", "Jane", "Doe", "0712345678", "ORGANIZER");
        when(authService.register(request)).thenThrow(new EmailAlreadyExistsException());

        mockMvc.perform(post("/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void login_validCredentials_returnsUserAndSetsRefreshTokenCookie() throws Exception {
        var request = new LoginRequest("jane@example.com", "Password123!");
        var userResponse = new UserResponse("ORGANIZER", "jane@example.com", "Doe", "Jane", "0712345678", "ext-1", true);
        var loginResponse = new LoginResponse(userResponse, new AccessToken("access-token", 900));
        lenient().when(jwtConfig.getRefreshTokenExpiration()).thenReturn(604800);
        when(authService.login(request)).thenReturn(new LoginResult(loginResponse, "refresh-token-value"));

        mockMvc.perform(post("/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken.token").value("access-token"))
                .andExpect(cookie().value("refresh_token", "refresh-token-value"))
                .andExpect(cookie().httpOnly("refresh_token", true));
    }

    @Test
    void login_invalidCredentials_returnsUnauthorized() throws Exception {
        var request = new LoginRequest("jane@example.com", "wrong-password");
        when(authService.login(request)).thenThrow(new UnauthorizedException("Invalid credentials"));

        mockMvc.perform(post("/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_blankPassword_returnsBadRequest() throws Exception {
        var request = new LoginRequest("jane@example.com", "");

        mockMvc.perform(post("/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void forgotPassword_validEmail_returnsOkMessage() throws Exception {
        var request = new ForgotPasswordRequest("jane@example.com");

        mockMvc.perform(post("/auth/forgot-password")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void resetPassword_validRequest_returnsNoContent() throws Exception {
        var request = new ResetPasswordRequest("reset-token", "NewPassword123!");

        mockMvc.perform(post("/auth/reset-password")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
    }

    @Test
    void refreshToken_validCookie_returnsNewAccessToken() throws Exception {
        when(authService.refreshToken("valid-refresh-token")).thenReturn(new AccessToken("new-access-token", 900));

        mockMvc.perform(post("/auth/refresh-token")
                        .cookie(new jakarta.servlet.http.Cookie("refresh_token", "valid-refresh-token")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("new-access-token"));
    }

    @Test
    void logout_withRefreshTokenCookie_clearsCookieAndReturnsNoContent() throws Exception {
        mockMvc.perform(post("/auth/logout")
                        .cookie(new jakarta.servlet.http.Cookie("refresh_token", "some-token")))
                .andExpect(status().isNoContent())
                .andExpect(cookie().maxAge("refresh_token", 0));
    }
}
