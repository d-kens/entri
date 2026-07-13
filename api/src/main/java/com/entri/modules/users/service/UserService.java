package com.entri.modules.users.service;

import com.entri.common.exception.ResourceNotFoundException;
import com.entri.common.utils.PhoneNumberUtils;
import com.entri.modules.users.dto.CreateUserRequest;
import com.entri.modules.users.dto.UserResponse;
import com.entri.modules.users.entity.Role;
import com.entri.modules.users.entity.User;
import com.entri.modules.users.event.UserCreatedEvent;
import com.entri.modules.users.exception.EmailAlreadyExist;
import com.entri.modules.users.service.mapper.UserMapper;
import com.entri.modules.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;
    private final UserMapper userMapper;

    public UserResponse create(CreateUserRequest userDto) {
        if (userRepository.existsByEmail(userDto.email())) {
            throw new EmailAlreadyExist();
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
    public UserResponse getUserByExternalKey(String externalKey) {
        return userMapper.toResponse(findEntityByExternalKey(externalKey));
    }

    @Transactional(readOnly = true)
    public User findEntityByExternalKey(String externalKey) {
        return userRepository.findByExternalKey(externalKey)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public void changeUserPassword(User user, String newPassword) {
        String passwordHash = passwordEncoder.encode(newPassword);
        user.setPasswordHash(passwordHash);
        userRepository.save(user);
    }
}
