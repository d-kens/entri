package com.parrcel.api.modules.user.service;

import com.parrcel.api.common.exception.EmailAlreadyExistException;
import com.parrcel.api.common.exception.NotFoundException;
import com.parrcel.api.modules.user.dto.CreateUserDto;
import com.parrcel.api.modules.user.entity.User;
import com.parrcel.api.modules.user.enums.Role;
import com.parrcel.api.modules.user.mapper.UserMapper;
import com.parrcel.api.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User getUserByEmail(String email) {
        return userRepository.findUserByEmail(email).orElseThrow(
                () -> new NotFoundException("user with email " + email + "not found")
        );
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("user with id " + userId + "not found")
        );
    }

    public User createUser(CreateUserDto dto) {
        if (userRepository.existsByEmail(dto.getEmail()))
            throw new EmailAlreadyExistException();

        var user = userMapper.toEntity(dto);
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));

        userRepository.save(user);

       return user;
    }

    public void updatePassword(User user, String newPassword) {
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}