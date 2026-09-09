package com.entri.modules.users.service;

import com.entri.exception.ForbiddenException;
import com.entri.exception.ResourceNotFoundException;
import com.entri.exception.UnauthorizedException;
import com.entri.security.UserPrincipal;
import com.entri.users.dto.ChangePasswordRequest;
import com.entri.users.dto.CreateUserRequest;
import com.entri.users.dto.UpdateUserRequest;
import com.entri.users.dto.UserResponse;
import com.entri.users.entity.Role;
import com.entri.users.entity.User;
import com.entri.users.exception.EmailAlreadyExistsException;
import com.entri.users.mapper.UserMapper;
import com.entri.users.repository.UserRepository;
import com.entri.users.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock UserMapper userMapper;

    @InjectMocks UserService userService;

    private User buildUser(String externalKey, Role role) {
        return User.builder()
                .externalKey(externalKey)
                .email("john@example.com")
                .firstName("John")
                .lastName("Doe")
                .phoneNumber("254712345678")
                .passwordHash("hashed")
                .role(role)
                .build();
    }

    @Test
    void create_emailAlreadyExists_throwsEmailAlreadyExistsException() {
        var request = new CreateUserRequest("john@example.com", "Password123!", "John", "Doe", "0712345678", "ORGANIZER");
        when(userRepository.existsByEmailAndDeletedFalse("john@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.create(request))
                .isInstanceOf(EmailAlreadyExistsException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void create_success_normalizesPhoneAndEncodesPasswordAndSaves() {
        var request = new CreateUserRequest("john@example.com", "Password123!", "John", "Doe", "0712345678", "organizer");
        var expected = new UserResponse("ORGANIZER", "john@example.com", "Doe", "John", "254712345678", "ext-key", true);

        when(userRepository.existsByEmailAndDeletedFalse("john@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("hashed-password");
        when(userMapper.toResponse(any(User.class))).thenReturn(expected);

        var result = userService.create(request);

        assertThat(result).isEqualTo(expected);

        var captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        var savedUser = captor.getValue();
        assertThat(savedUser.getPhoneNumber()).isEqualTo("254712345678");
        assertThat(savedUser.getPasswordHash()).isEqualTo("hashed-password");
        assertThat(savedUser.getRole()).isEqualTo(Role.ORGANIZER);
    }

    @Test
    void getUserByExternalKey_ownRecordRequested_returnsResponse() {
        var user = buildUser("ext-key", Role.ORGANIZER);
        var requestingUser = new UserPrincipal(user);
        var expected = new UserResponse("ORGANIZER", "john@example.com", "Doe", "John", "254712345678", "ext-key", true);

        when(userRepository.findByExternalKeyAndDeletedFalse("ext-key")).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(expected);

        var result = userService.getUserByExternalKey("ext-key", requestingUser);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getUserByExternalKey_requestedByDifferentNonAdminUser_throwsForbiddenException() {
        var owner = buildUser("owner-key", Role.ORGANIZER);
        var requestingUser = new UserPrincipal(buildUser("other-key", Role.ORGANIZER));

        when(userRepository.findByExternalKeyAndDeletedFalse("owner-key")).thenReturn(Optional.of(owner));

        assertThatThrownBy(() -> userService.getUserByExternalKey("owner-key", requestingUser))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void findEntityByExternalKey_notFound_throwsResourceNotFoundException() {
        when(userRepository.findByExternalKeyAndDeletedFalse("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findEntityByExternalKey("missing"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findByEmail_notFound_throwsResourceNotFoundException() {
        when(userRepository.findByEmailAndDeletedFalse("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findByEmail("missing@example.com"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void changePassword_currentPasswordIncorrect_throwsUnauthorizedException() {
        var user = buildUser("ext-key", Role.ORGANIZER);
        var requestingUser = new UserPrincipal(user);
        var request = new ChangePasswordRequest("wrong-password", "NewPassword123!");

        when(userRepository.findByExternalKeyAndDeletedFalse("ext-key")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> userService.changePassword("ext-key", request, requestingUser))
                .isInstanceOf(UnauthorizedException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void changePassword_correctCurrentPassword_updatesPasswordHash() {
        var user = buildUser("ext-key", Role.ORGANIZER);
        var requestingUser = new UserPrincipal(user);
        var request = new ChangePasswordRequest("hashed", "NewPassword123!");

        when(userRepository.findByExternalKeyAndDeletedFalse("ext-key")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("hashed", "hashed")).thenReturn(true);
        when(passwordEncoder.encode("NewPassword123!")).thenReturn("new-hashed");

        userService.changePassword("ext-key", request, requestingUser);

        assertThat(user.getPasswordHash()).isEqualTo("new-hashed");
        verify(userRepository).save(user);
    }

    @Test
    void updateUser_emailChangedToOneAlreadyInUse_throwsEmailAlreadyExistsException() {
        var user = buildUser("ext-key", Role.ORGANIZER);
        var requestingUser = new UserPrincipal(user);
        var request = new UpdateUserRequest("taken@example.com", "John", "Doe", "0712345678");

        when(userRepository.findByExternalKeyAndDeletedFalse("ext-key")).thenReturn(Optional.of(user));
        when(userRepository.existsByEmailAndDeletedFalse("taken@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.updateUser("ext-key", request, requestingUser))
                .isInstanceOf(EmailAlreadyExistsException.class);
    }

    @Test
    void updateUser_success_updatesUserFieldsAndReturnsResponse() {
        var user = buildUser("ext-key", Role.ORGANIZER);
        var requestingUser = new UserPrincipal(user);
        var request = new UpdateUserRequest("john.new@example.com", "Jonathan", "Doey", "0798765432");
        var expected = new UserResponse("ORGANIZER", "john.new@example.com", "Doey", "Jonathan", "254798765432", "ext-key", true);

        when(userRepository.findByExternalKeyAndDeletedFalse("ext-key")).thenReturn(Optional.of(user));
        when(userRepository.existsByEmailAndDeletedFalse("john.new@example.com")).thenReturn(false);
        when(userMapper.toResponse(user)).thenReturn(expected);

        var result = userService.updateUser("ext-key", request, requestingUser);

        assertThat(result).isEqualTo(expected);
        assertThat(user.getEmail()).isEqualTo("john.new@example.com");
        assertThat(user.getFirstName()).isEqualTo("Jonathan");
        assertThat(user.getLastName()).isEqualTo("Doey");
        assertThat(user.getPhoneNumber()).isEqualTo("254798765432");
    }

    @Test
    void updateUser_emailUnchanged_doesNotCheckForDuplicate() {
        var user = buildUser("ext-key", Role.ORGANIZER);
        var requestingUser = new UserPrincipal(user);
        var request = new UpdateUserRequest("JOHN@example.com", "John", "Doe", "0712345678");

        when(userRepository.findByExternalKeyAndDeletedFalse("ext-key")).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(null);

        userService.updateUser("ext-key", request, requestingUser);

        verify(userRepository, never()).existsByEmailAndDeletedFalse(any());
    }

    @Test
    void listUsers_returnsMappedPaginationResponse() {
        var user = buildUser("ext-key", Role.ORGANIZER);
        var expected = new UserResponse("ORGANIZER", "john@example.com", "Doe", "John", "254712345678", "ext-key", true);
        var pageable = PageRequest.of(0, 10);
        Page<User> page = new PageImpl<>(List.of(user), pageable, 1);

        when(userRepository.findAllByDeletedFalse(pageable)).thenReturn(page);
        when(userMapper.toResponse(user)).thenReturn(expected);

        var result = userService.listUsers(pageable);

        assertThat(result.content()).containsExactly(expected);
        assertThat(result.totalElements()).isEqualTo(1);
        assertThat(result.first()).isTrue();
        assertThat(result.last()).isTrue();
    }

    @Test
    void deleteUser_success_marksUserDeleted() {
        var user = buildUser("ext-key", Role.ADMIN);
        var requestingUser = new UserPrincipal(user);

        when(userRepository.findByExternalKeyAndDeletedFalse("ext-key")).thenReturn(Optional.of(user));

        userService.deleteUser("ext-key", requestingUser);

        assertThat(user.isDeleted()).isTrue();
        verify(userRepository).save(user);
    }

    @Test
    void setUserEnabled_disable_updatesEnabledFlagAndSaves() {
        var user = buildUser("ext-key", Role.ORGANIZER);
        when(userRepository.findByExternalKeyAndDeletedFalse("ext-key")).thenReturn(Optional.of(user));

        userService.setUserEnabled("ext-key", false);

        assertThat(user.isEnabled()).isFalse();
        verify(userRepository).save(user);
    }
}
