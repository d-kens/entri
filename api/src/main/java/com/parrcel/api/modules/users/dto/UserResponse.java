package com.parrcel.api.modules.users.dto;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String userName;
    private String phoneNumber;
    private String email;
    private String role;
}