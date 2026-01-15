package com.parrcel.api.modules.user.service;

import com.parrcel.api.common.exception.NotFoundException;
import com.parrcel.api.modules.user.entity.User;
import com.parrcel.api.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User getUserByEmail(String email) {
        return userRepository.findUserByEmail(email).orElseThrow(
                () -> new NotFoundException("user with email " + email + "not found")
        );
    }

}