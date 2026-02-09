package com.parrcel.api.security.service;

import com.parrcel.api.common.exception.NotFoundException;
import com.parrcel.api.modules.users.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class CustomUserDetailsService implements UserDetailsService  {

    private final UserService userService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        try {
            var user = userService.getUserByEmail(email);
            var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));

            return new User(
                    user.getEmail(),
                    user.getPasswordHash(),
                    authorities
            );
        } catch (NotFoundException exception) {
            throw new UsernameNotFoundException("users not found");
        }
    }
}