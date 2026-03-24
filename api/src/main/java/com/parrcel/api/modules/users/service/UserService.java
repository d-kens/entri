package com.parrcel.api.modules.users.service;

import com.parrcel.api.common.exception.NotFoundException;
import com.parrcel.api.modules.users.exception.PhoneNumberAlreadyExistException;
import com.parrcel.api.common.utils.PhoneNumberUtils;
import com.parrcel.api.modules.notification.entity.NotificationType;
import com.parrcel.api.modules.notification.events.CreateSubscriberEvent;
import com.parrcel.api.modules.notification.events.SendNotificationEvent;
import com.parrcel.api.modules.users.dto.CreateUserDto;
import com.parrcel.api.modules.users.entity.User;
import com.parrcel.api.modules.users.entity.Role;
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

    public User getUserByPhoneNumber(String phoneNumber) {

        System.out.println("-------------------------");
        System.out.println(PhoneNumberUtils.normalize(phoneNumber));
        System.out.println("-------------------------");

        return userRepository.findUserByPhoneNumber(PhoneNumberUtils.normalize(phoneNumber)).orElseThrow(
                () -> new NotFoundException("user with phone number " + phoneNumber + " not found")
        );
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("users with id " + userId + "not found")
        );
    }

    public User createUser(CreateUserDto dto) {
        String normalizedPhone = PhoneNumberUtils.normalize(dto.phoneNumber());

        if (userRepository.existsByPhoneNumber(dto.phoneNumber()))
            throw new PhoneNumberAlreadyExistException();

        var user = userMapper.toEntity(dto);
        user.setPhoneNumber(normalizedPhone);
        user.setPasswordHash(passwordEncoder.encode(dto.password()));

        userRepository.save(user);

        if (user.getRole() == Role.CUSTOMER)
            sendWelcomeNotification(user);

       return user;
    }

    public void sendWelcomeNotification(User user) {

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
                        NotificationType.WELCOME_NOTIFICATION
                )
        );
    }

    public void updatePassword(User user, String newPassword) {
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}