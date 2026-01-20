package com.parrcel.api.modules.auth.service;


import com.parrcel.api.common.exception.InvalidTokenException;
import com.parrcel.api.common.exception.NotFoundException;
import com.parrcel.api.modules.auth.dto.AuthRequest;
import com.parrcel.api.modules.auth.dto.ForgotPasswordRequest;
import com.parrcel.api.modules.auth.dto.TokenPair;
import com.parrcel.api.modules.user.entity.User;
import com.parrcel.api.modules.user.service.UserService;
import com.parrcel.api.security.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtService jwtService;
    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    public TokenPair login(AuthRequest authRequest) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authRequest.getEmail(),
                        authRequest.getPassword()
                )
        );

        var user = userService.getUserByEmail(authRequest.getEmail());
        var accessToken = jwtService.generateAccessToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        return new TokenPair(
                accessToken.toString(),
                refreshToken.toString()
        );
    }

    public String refreshToken(String refreshToken) {
        var refreshTokenObject = jwtService.parseToken(refreshToken);

        if (refreshTokenObject == null)
            throw new InvalidTokenException("token is invalid or has expired");

        var userId = refreshTokenObject.getUserId();

        User user;

        try {
            user = userService.getUserById(userId);
        } catch (NotFoundException exception) {
            throw new InvalidTokenException("user associated with the token not found");
        }

        var accessTokenObject = jwtService.generateAccessToken(user);
        return accessTokenObject.toString();
    }

    public String forgotPassword(ForgotPasswordRequest request) {

        try {
            var user = userService.getUserByEmail(request.getEmail());
            return "If email exists, a reset link has been sent";
        } catch (NotFoundException exception) {
            return "If email exists, a reset link has been sent";
        }
    }
}