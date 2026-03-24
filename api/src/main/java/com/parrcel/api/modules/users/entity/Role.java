package com.parrcel.api.modules.users.enums;

public enum Role {
    ADMIN,
    CUSTOMER,
    AGENT,

    RIDER;

    public static Role from (String value) {
        return Role.valueOf(value.toUpperCase());
    }
}