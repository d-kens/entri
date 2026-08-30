package com.entri.users.service;

import com.entri.exception.ResourceNotFoundException;
import com.entri.exception.UnauthorizedException;
import com.entri.security.UserPrincipal;
import com.entri.users.dto.ChangePasswordRequest;
import com.entri.users.dto.UpdateUserRequest;
import com.entri.utils.PhoneNumberUtils;
import com.entri.users.dto.CreateUserRequest;
import com.entri.users.dto.UserResponse;
import com.entri.users.entity.Role;
import com.entri.users.entity.User;
import com.entri.users.dto.UserCreatedMessage;
import com.entri.users.dto.UserUpdatedMessage;
import com.entri.users.exception.EmailAlreadyExistsException;
import com.entri.users.mapper.UserMapper;
import com.entri.users.repository.UserRepository;
import com.entri.users.UserEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserEventPublisher userEventPublisher;
    private final UserMapper userMapper;

    @Transactional
    public UserResponse create(final CreateUserRequest userDto) {
        if (userRepository.existsByEmail(userDto.email())) {
            throw new EmailAlreadyExistsException();
        }
        User user = User.builder()
                .email(userDto.email())
                .lastName(userDto.lastName())
                .firstName(userDto.firstName())
                .phoneNumber(PhoneNumberUtils.normalize(userDto.phoneNumber()))
                .passwordHash(passwordEncoder.encode(userDto.password()))
                .role(Role.valueOf(userDto.role().toUpperCase()))
                .build();
        userRepository.save(user);
        userEventPublisher.publishUserCreated(new UserCreatedMessage(
                user.getExternalKey().toString(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getRole().name()
        ));
        return userMapper.toResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserByExternalKey(final String externalKey, final UserPrincipal requestingUser) {
        var user = findEntityByExternalKey(externalKey);
        requestingUser.assertCanManage(externalKey);
        return userMapper.toResponse(user);
    }

    @Transactional(readOnly = true)
    public User findEntityByExternalKey(final String externalKey) {
        return userRepository.findByExternalKey(externalKey)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Transactional(readOnly = true)
    public User findByEmail(final String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Transactional
    public void changePassword(final String externalKey, final ChangePasswordRequest request, final UserPrincipal requestingUser) {
        requestingUser.assertCanManage(externalKey);
        var user = findEntityByExternalKey(externalKey);
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Current password is incorrect");
        }
        changeUserPassword(user, request.newPassword());
    }

    @Transactional
    public void changeUserPassword(final User user, final String newPassword) {
        String passwordHash = passwordEncoder.encode(newPassword);
        user.setPasswordHash(passwordHash);
        userRepository.save(user);
    }

    @Transactional
    public UserResponse updateUser(final String userExternalKey, final UpdateUserRequest updateUserRequest, final UserPrincipal requestingUser) {
        var user = findEntityByExternalKey(userExternalKey);

        requestingUser.assertCanManage(userExternalKey);

        if (!user.getEmail().equalsIgnoreCase(updateUserRequest.email())
                && userRepository.existsByEmail(updateUserRequest.email())) {
            throw new EmailAlreadyExistsException();
        }

        user.setEmail(updateUserRequest.email());
        user.setFirstName(updateUserRequest.firstName());
        user.setLastName(updateUserRequest.lastName());
        user.setPhoneNumber(PhoneNumberUtils.normalize(updateUserRequest.phoneNumber()));

        userEventPublisher.publishUserUpdated(new UserUpdatedMessage(
                user.getExternalKey().toString(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhoneNumber()
        ));
        return userMapper.toResponse(user);
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> listUsers(final Pageable pageable) {
        return userRepository.findAll(pageable).map(userMapper::toResponse);
    }

    @Transactional
    public void deleteUser(final String externalKey, final UserPrincipal requestingUser) {
        requestingUser.assertCanManage(externalKey);
        var user = findEntityByExternalKey(externalKey);
        userRepository.delete(user);
    }

    @Transactional
    public void setUserEnabled(final String externalKey, final boolean enabled) {
        var user = findEntityByExternalKey(externalKey);
        user.setEnabled(enabled);
        userRepository.save(user);
    }


}

