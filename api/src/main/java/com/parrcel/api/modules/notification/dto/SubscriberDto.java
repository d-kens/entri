package com.parrcel.api.modules.notification.dto;


import lombok.Data;

@Data
public class SubscriberDto {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
}