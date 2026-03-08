package com.parrcel.api.modules.users.service;

import com.parrcel.api.common.exception.EmailAlreadyExistException;
import com.parrcel.api.common.exception.NotFoundException;
import com.parrcel.api.modules.notification.enums.NotificationType;
import com.parrcel.api.modules.notification.events.CreateSubscriberEvent;
import com.parrcel.api.modules.notification.events.SendNotificationEvent;
import com.parrcel.api.modules.users.dto.CreateUserDto;
import com.parrcel.api.modules.users.model.User;
import com.parrcel.api.modules.users.enums.Role;
import com.parrcel.api.modules.users.mapper.UserMapper;
import com.parrcel.api.modules.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {

    @Value("${app.base-url}")
    private String baseUrl;
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    public User getUserByEmail(String email) {
        return userRepository.findUserByEmail(email).orElseThrow(
                () -> new NotFoundException("users with email " + email + "not found")
        );
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("users with id " + userId + "not found")
        );
    }

    public User createUser(CreateUserDto dto) {
        if (userRepository.existsByEmail(dto.email()))
            throw new EmailAlreadyExistException();

        var user = userMapper.toEntity(dto);
        user.setPasswordHash(passwordEncoder.encode(dto.password()));

        userRepository.save(user);

        if (user.getRole() == Role.CUSTOMER)
            sendWelcomeEmail(user);

       return user;
    }

    public void sendWelcomeEmail(User user) {

        eventPublisher.publishEvent(
                new CreateSubscriberEvent(user)
        );

        eventPublisher.publishEvent(
                new SendNotificationEvent(
                        user,
                        Map.of(
                                "userName", user.getUserName(),
                                "dashboardUrl", String.format("%s/dashboard", baseUrl)
                        ),
                        NotificationType.WELCOME_EMAIL
                )
        );
    }

    public void updatePassword(User user, String newPassword) {
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}