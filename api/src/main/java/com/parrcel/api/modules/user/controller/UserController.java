package com.parrcel.api.modules.user.controller;


import com.parrcel.api.common.dto.ErrorDto;
import com.parrcel.api.common.exception.EmailAlreadyExistException;
import com.parrcel.api.modules.user.dto.CreateUserDto;
import com.parrcel.api.modules.user.dto.UserResponse;
import com.parrcel.api.modules.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @PostMapping
    public UserResponse createUser(
            @Valid @RequestBody CreateUserDto createUserDto
    ) {
        return userService.createUser(createUserDto);
    }

    @ExceptionHandler(EmailAlreadyExistException.class)
    public ResponseEntity<ErrorDto> handleEmailAlreadyExistException(
            EmailAlreadyExistException exception
    ) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new ErrorDto(exception.getMessage())
        );
    }
}