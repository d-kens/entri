package com.oro.api.modules.user.service;

import com.oro.api.common.dto.PageResponse;
import com.oro.api.common.exception.NotFoundException;
import com.oro.api.common.utils.PhoneNumberUtils;
import com.oro.api.modules.user.dto.*;
import com.oro.api.modules.user.entity.User;
import com.oro.api.modules.user.exception.PhoneNumberAlreadyExistException;
import com.oro.api.modules.user.mapper.UserMapper;
import com.oro.api.modules.notification.events.CreateSubscriberEvent;
import com.oro.api.modules.user.repository.RoleRepository;
import com.oro.api.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    public PageResponse<UserResponse> getUsers(Pageable pageable) {
        return PageResponse.of(userRepository.findAll(pageable).map(userMapper::toResponse));
    }

    public UserResponse getUserByExternalId(String externalId) {
        return userMapper.toResponse(findByExternalId(externalId));
    }

    public UserResponse getCurrentUser(String phoneNumber) {
        return userMapper.toResponse(getUserByPhoneNumber(phoneNumber));
    }

    public User getUserByPhoneNumber(String phoneNumber) {
        return userRepository.findUserByPhoneNumber(PhoneNumberUtils.normalize(phoneNumber)).orElseThrow(
                () -> new NotFoundException("User not found")
        );
    }

    @Transactional
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    public UserResponse registerMerchant(RegisterMerchantRequest request) {
        return doCreateUser(request.name(), request.phoneNumber(), request.password(), "MERCHANT");
    }

    public UserResponse createUser(CreateUserRequest request) {
        return doCreateUser(request.name(), request.phoneNumber(), request.password(), request.role());
    }

    @Transactional
    public UserResponse updateUser(String externalId, UpdateUserRequest request) {
        var user = findByExternalId(externalId);
        user.setName(request.name());
        return userMapper.toResponse(userRepository.save(user));
    }

    @Transactional
    public void deactivateUser(String externalId) {
        var user = findByExternalId(externalId);
        user.setActive(false);
        userRepository.save(user);
    }

    private UserResponse doCreateUser(String name, String phoneNumber, String rawPassword, String roleName) {
        var normalizedPhone = PhoneNumberUtils.normalize(phoneNumber);
        if (userRepository.existsByPhoneNumber(normalizedPhone)) {
            throw new PhoneNumberAlreadyExistException();
        }

        var role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new NotFoundException("Role not found: " + roleName));

        var user = new User();
        user.setName(name);
        user.setPhoneNumber(normalizedPhone);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRoles(Set.of(role));

        userRepository.save(user);
        eventPublisher.publishEvent(new CreateSubscriberEvent(user));

        return userMapper.toResponse(user);
    }

    public void changePassword(String phoneNumber, String currentPassword, String newPassword) {
        var user = getUserByPhoneNumber(phoneNumber);
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new BadCredentialsException("Current password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public void resetPassword(String phoneNumber, String newPassword) {
        var user = getUserByPhoneNumber(phoneNumber);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public User findByExternalId(String externalId) {
        return userRepository.findByExternalId(externalId)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }
}
