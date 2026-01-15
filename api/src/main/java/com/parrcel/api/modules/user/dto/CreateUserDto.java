package com.parrcel.api.modules.user.dto;

import com.parrcel.api.modules.user.validation.ValidRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateUserDto {
    @ValidRole
    private String role;

    @NotBlank(message = "email is required")
    @Email(message = "email must be a valid email")
    private String email;

    @NotBlank(message = "phone number is required")
    private String phone;

    @NotBlank(message = "password is required")
    @Size(min = 8, max = 20, message = "password must be between 8 and 20 characters")
    private String password;

    @NotBlank(message = "username is required")
    private String userName;
}