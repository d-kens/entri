package com.entri.modules.users.controller;

import com.entri.common.dto.PaginationResponse;
import com.entri.exception.ForbiddenException;
import com.entri.exception.ResourceNotFoundException;
import com.entri.exception.GlobalExceptionHandler;
import com.entri.security.UserPrincipal;
import com.entri.users.controller.UserController;
import com.entri.users.dto.ChangePasswordRequest;
import com.entri.users.dto.UpdateUserRequest;
import com.entri.users.dto.UserResponse;
import com.entri.users.entity.Role;
import com.entri.users.entity.User;
import com.entri.users.exception.EmailAlreadyExistsException;
import com.entri.users.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    UserService userService;

    MockMvc mockMvc;
    ObjectMapper objectMapper = new ObjectMapper();

    UserPrincipal requestingUser;

    @BeforeEach
    void setUp() {
        UserController controller = new UserController(userService);
        User user = User.builder().externalKey("requester-key").role(Role.ADMIN).build();
        requestingUser = new UserPrincipal(user);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new UserPrincipalArgumentResolver(requestingUser), new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    void getUserByExternalKey_userExists_returnsUser() throws Exception {
        var response = new UserResponse("ORGANIZER", "john@example.com", "Doe", "John", "0712345678", "ext-key", true);
        when(userService.getUserByExternalKey(eq("ext-key"), any(UserPrincipal.class))).thenReturn(response);

        mockMvc.perform(get("/users/{externalKey}", "ext-key"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.externalKey").value("ext-key"));
    }

    @Test
    void getUserByExternalKey_userNotFound_returns404() throws Exception {
        when(userService.getUserByExternalKey(eq("missing"), any(UserPrincipal.class)))
                .thenThrow(new ResourceNotFoundException("User not found"));

        mockMvc.perform(get("/users/{externalKey}", "missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource Not Found"));
    }

    @Test
    void getUserByExternalKey_forbidden_returns403() throws Exception {
        when(userService.getUserByExternalKey(eq("other-key"), any(UserPrincipal.class)))
                .thenThrow(new ForbiddenException("You are not authorized to perform this action"));

        mockMvc.perform(get("/users/{externalKey}", "other-key"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Forbidden"));
    }

    @Test
    void updateUser_validRequest_returnsUpdatedUser() throws Exception {
        var request = new UpdateUserRequest("john@example.com", "John", "Doe", "0712345678");
        var response = new UserResponse("ORGANIZER", "john@example.com", "Doe", "John", "0712345678", "ext-key", true);
        when(userService.updateUser(eq("ext-key"), any(UpdateUserRequest.class), any(UserPrincipal.class))).thenReturn(response);

        mockMvc.perform(put("/users/{externalKey}", "ext-key")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"));
    }

    @Test
    void updateUser_blankEmail_returns400() throws Exception {
        var request = new UpdateUserRequest("", "John", "Doe", "0712345678");

        mockMvc.perform(put("/users/{externalKey}", "ext-key")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Failed"));
    }

    @Test
    void updateUser_duplicateEmail_returns409() throws Exception {
        var request = new UpdateUserRequest("taken@example.com", "John", "Doe", "0712345678");
        when(userService.updateUser(eq("ext-key"), any(UpdateUserRequest.class), any(UserPrincipal.class)))
                .thenThrow(new EmailAlreadyExistsException());

        mockMvc.perform(put("/users/{externalKey}", "ext-key")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void changePassword_validRequest_returns204() throws Exception {
        var request = new ChangePasswordRequest("oldPass123", "newPass123");

        mockMvc.perform(post("/users/{externalKey}/change-password", "ext-key")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(userService).changePassword(eq("ext-key"), any(ChangePasswordRequest.class), any(UserPrincipal.class));
    }

    @Test
    void changePassword_shortNewPassword_returns400() throws Exception {
        var request = new ChangePasswordRequest("oldPass123", "short");

        mockMvc.perform(post("/users/{externalKey}/change-password", "ext-key")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listUsers_returnsPaginatedUsers() throws Exception {
        var response = new UserResponse("ORGANIZER", "john@example.com", "Doe", "John", "0712345678", "ext-key", true);
        var pagination = new PaginationResponse<>(java.util.List.of(response), 0, 20, 1, 1, true, true);
        when(userService.listUsers(any(Pageable.class))).thenReturn(pagination);

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].email").value("john@example.com"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void deleteUser_existingUser_returns204() throws Exception {
        mockMvc.perform(delete("/users/{externalKey}", "ext-key"))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(eq("ext-key"), any(UserPrincipal.class));
    }

    @Test
    void deleteUser_userNotFound_returns404() throws Exception {
        org.mockito.Mockito.doThrow(new ResourceNotFoundException("User not found"))
                .when(userService).deleteUser(eq("missing"), any(UserPrincipal.class));

        mockMvc.perform(delete("/users/{externalKey}", "missing"))
                .andExpect(status().isNotFound());
    }

    @Test
    void enableUser_existingUser_returns204() throws Exception {
        mockMvc.perform(post("/users/{externalKey}/enable", "ext-key"))
                .andExpect(status().isNoContent());

        verify(userService).setUserEnabled("ext-key", true);
    }

    @Test
    void disableUser_existingUser_returns204() throws Exception {
        mockMvc.perform(post("/users/{externalKey}/disable", "ext-key"))
                .andExpect(status().isNoContent());

        verify(userService).setUserEnabled("ext-key", false);
    }

    static class UserPrincipalArgumentResolver implements HandlerMethodArgumentResolver {
        private final UserPrincipal principal;

        UserPrincipalArgumentResolver(UserPrincipal principal) {
            this.principal = principal;
        }

        @Override
        public boolean supportsParameter(MethodParameter parameter) {
            return parameter.getParameterType().equals(UserPrincipal.class);
        }

        @Override
        public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                       NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
            return principal;
        }
    }
}
