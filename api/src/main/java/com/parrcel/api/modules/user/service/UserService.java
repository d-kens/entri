package com.parrcel.api.modules.user.service;

import com.parrcel.api.common.exception.EmailAlreadyExistException;
import com.parrcel.api.common.exception.NotFoundException;
import com.parrcel.api.modules.user.dto.CreateUserDto;
import com.parrcel.api.modules.user.dto.UserResponse;
import com.parrcel.api.modules.user.entity.User;
import com.parrcel.api.modules.user.enums.Role;
import com.parrcel.api.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User getUserByEmail(String email) {
        return userRepository.findUserByEmail(email).orElseThrow(
                () -> new NotFoundException("user with email " + email + "not found")
        );
    }

    public UserResponse createUser(CreateUserDto dto) {
        if (userRepository.existsByEmail(dto.getEmail()))
            throw new EmailAlreadyExistException();

        var user = new User();
        user.setEmail(dto.getEmail());
        user.setUserName(dto.getUserName());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setRole(Role.valueOf(dto.getRole()));
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));

        userRepository.save(user);

       return new UserResponse(
                user.getId(),
                user.getUserName(),
                user.getPhoneNumber(),
                user.getEmail(),
                user.getRole().toString()
       );
    }
}