package com.entri.users.service;

import com.entri.exception.ResourceNotFoundException;
import com.entri.exception.UnauthorizedException;
import com.entri.security.UserPrincipal;
import com.entri.users.dto.UpdateUserRequest;
import com.entri.utils.PhoneNumberUtils;
import com.entri.users.dto.CreateUserRequest;
import com.entri.users.dto.UserResponse;
import com.entri.users.entity.Role;
import com.entri.users.entity.User;
import com.entri.users.event.UserCreatedEvent;
import com.entri.users.event.UserUpdatedEvent;
import com.entri.users.exception.EmailAlreadyExistsException;
import com.entri.users.mapper.UserMapper;
import com.entri.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;
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
        eventPublisher.publishEvent(new UserCreatedEvent(user));
        return userMapper.toResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserByExternalKey(final String externalKey, final UserPrincipal requestingUser) {
        var user = findEntityByExternalKey(externalKey);
        assertCanManage(externalKey, requestingUser);
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
    public void changeUserPassword(final User user, final String newPassword) {
        String passwordHash = passwordEncoder.encode(newPassword);
        user.setPasswordHash(passwordHash);
        userRepository.save(user);
    }

    @Transactional
    public UserResponse updateUser(final String userExternalKey, final UpdateUserRequest updateUserRequest, final UserPrincipal requestingUser) {
        var user = findEntityByExternalKey(userExternalKey);

        assertCanManage(userExternalKey, requestingUser);

        if (!user.getEmail().equalsIgnoreCase(updateUserRequest.email())
                && userRepository.existsByEmail(updateUserRequest.email())) {
            throw new EmailAlreadyExistsException();
        }

        user.setEmail(updateUserRequest.email());
        user.setFirstName(updateUserRequest.firstName());
        user.setLastName(updateUserRequest.lastName());
        user.setPhoneNumber(PhoneNumberUtils.normalize(updateUserRequest.phoneNumber()));

        eventPublisher.publishEvent(new UserUpdatedEvent(user));
        return userMapper.toResponse(user);
    }

    private void assertCanManage(String externalKey, UserPrincipal requestingUser) {
        if (!requestingUser.isAdmin() && !requestingUser.getExternalKey().equals(externalKey)) {
            throw new UnauthorizedException("You are not authorized to perform this action");
        }
    }

}
