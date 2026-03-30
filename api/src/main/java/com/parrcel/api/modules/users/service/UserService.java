package com.parrcel.api.modules.users.service;

import com.parrcel.api.common.exception.NotFoundException;
import com.parrcel.api.modules.token.repository.RefreshTokenSessionRepository;
import com.parrcel.api.modules.users.dto.CreateUserReq;
import com.parrcel.api.modules.users.entity.Role;
import com.parrcel.api.modules.users.exception.PhoneNumberAlreadyExistException;
import com.parrcel.api.modules.notification.entity.NotificationType;
import com.parrcel.api.modules.notification.events.CreateSubscriberEvent;
import com.parrcel.api.modules.notification.events.SendNotificationEvent;
import com.parrcel.api.modules.users.entity.User;
import com.parrcel.api.modules.users.mapper.UserMapper;
import com.parrcel.api.modules.users.repository.RoleRepository;
import com.parrcel.api.modules.users.repository.UserRepository;
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
    private final RefreshTokenSessionRepository refreshTokenSessionRepository;

    @Transactional
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    @Transactional
    public User createUser(CreateUserReq dto) {
        if (userRepository.existsByPhoneNumber(dto.phoneNumber())) {
            throw new PhoneNumberAlreadyExistException();
        }

        Set<Role> roles = dto.roles().stream()
                .map(roleName -> roleRepository.findByName(roleName)
                        .orElseThrow(() -> new RuntimeException("Role not found: " + roleName)))
                .collect(Collectors.toSet());

        User user = userMapper.toEntity(dto);
        user.setRoles(roles);
        user.setActive(true);

        user.getTwoFactorCode().generate(15);

        sendOtp(user);

        return userRepository.save(user);
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