package com.oro.api.modules.user.service;

import com.oro.api.common.dto.PageResponse;
import com.oro.api.common.exception.NotFoundException;
import com.oro.api.modules.notification.entity.NotificationType;
import com.oro.api.modules.notification.events.CreateSubscriberEvent;
import com.oro.api.modules.notification.events.SendNotificationEvent;
import com.oro.api.modules.user.dto.CreateUserRequest;
import com.oro.api.modules.user.dto.RegisterMerchantRequest;
import com.oro.api.modules.user.dto.UpdateUserRequest;
import com.oro.api.modules.user.dto.UserResponse;
import com.oro.api.modules.user.entity.User;
import com.oro.api.modules.user.exception.PhoneNumberAlreadyExistException;
import com.oro.api.modules.user.mapper.UserMapper;
import com.oro.api.modules.user.repository.RoleRepository;
import com.oro.api.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserMapper userMapper;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    public PageResponse<UserResponse> getUsers(Pageable pageable) {
        return PageResponse.of(userRepository.findAll(pageable).map(userMapper::toResponse));
    }

    public UserResponse getUserByExternalId(String externalId) {
        return userMapper.toResponse(findByExternalId(externalId));
    }

    @Transactional
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    public UserResponse registerMerchant(RegisterMerchantRequest request) {
        return doCreateUser(request.name(), request.phoneNumber(), "MERCHANT");
    }

    public UserResponse createUser(CreateUserRequest request) {
        return doCreateUser(request.name(), request.phoneNumber(), request.role());
    }

    @Transactional
    public UserResponse updateUser(String externalId, UpdateUserRequest request) {
        var user = findByExternalId(externalId);
        user.setName(request.name());
        return userMapper.toResponse(user);
    }

    @Transactional
    public void deactivateUser(String externalId) {
        var user = findByExternalId(externalId);
        user.setActive(false);
    }

    private UserResponse doCreateUser(String name, String phoneNumber, String roleName) {
        if (userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new PhoneNumberAlreadyExistException();
        }

        var role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new NotFoundException("Role not found: " + roleName));

        var user = new User();
        user.setName(name);
        user.setPhoneNumber(phoneNumber);
        user.setRoles(Set.of(role));

        userRepository.save(user);

        sendOtp(user);

        return userMapper.toResponse(user);
    }

    private User findByExternalId(String externalId) {
        return userRepository.findByExternalId(externalId)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private void sendOtp(User user) {
        eventPublisher.publishEvent(new CreateSubscriberEvent(user));

        eventPublisher.publishEvent(
                new SendNotificationEvent(
                        user,
                        Map.of(
                                "userName", user.getPhoneNumber(),
                                "otpCode", user.getTwoFactorCode().getTwoFactorCode()
                        ),
                        NotificationType.OTP
                )
        );
    }
}
