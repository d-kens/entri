package com.parrcel.api.modules.users.dto;

import com.parrcel.api.modules.users.validation.ValidRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateUserDto {
    @NotBlank(message = "username is required")
    private String userName;

    @NotBlank(message = "password is required")
    @Size(min = 8, max = 20, message = "password must be between 8 and 20 characters")
    private String password;

    @NotBlank(message = "phone number is required")
    @Size(min = 10, max = 15, message = "phone number should be between 10 and 15 characters")
    private String phoneNumber;

    @NotBlank(message = "email is required")
    @Email(message = "email must be a valid email")
    private String email;

    @ValidRole
    private String role;
}