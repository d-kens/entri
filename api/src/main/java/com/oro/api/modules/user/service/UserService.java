package com.oro.api.modules.user.service;

import com.oro.api.common.exception.NotFoundException;
import com.oro.api.modules.notification.entity.NotificationType;
import com.oro.api.modules.notification.events.CreateSubscriberEvent;
import com.oro.api.modules.notification.events.SendNotificationEvent;
import com.oro.api.modules.token.repository.RefreshTokenSessionRepository;
import com.oro.api.modules.user.dto.CreateUserRequest;
import com.oro.api.modules.user.dto.UserResponse;
import com.oro.api.modules.user.entity.Role;
import com.oro.api.modules.user.entity.User;
import com.oro.api.modules.user.exception.PhoneNumberAlreadyExistException;
import com.oro.api.modules.user.mapper.UserMapper;
import com.oro.api.modules.user.repository.RoleRepository;
import com.oro.api.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserMapper userMapper;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByPhoneNumber(request.phoneNumber())) {
            throw new PhoneNumberAlreadyExistException();
        }

        Set<Role> role = request.roles().stream()
                .map(roleName -> roleRepository.findByName(roleName)
                                .orElseThrow(() -> new NotFoundException("Role not found: " + roleName))
                ).collect(Collectors.toSet());

        var user = userMapper.toEntity(request);
        user.setRoles(role);

        userRepository.save(user);

        sendOtp(user);

        return userMapper.toResponse(user);
    }

    private void sendOtp(User user) {
        eventPublisher.publishEvent(
                new CreateSubscriberEvent(user)
        );


        eventPublisher.publishEvent(
                new SendNotificationEvent(
                        user,
                        Map.of(
                                "userName", user.getPhoneNumber(),
                                "otpCode", user.getTwoFactorCode().getCode()
                        ),
                        NotificationType.OTP
                )
        );

    }
}