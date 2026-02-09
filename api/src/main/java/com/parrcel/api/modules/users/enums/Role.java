package com.parrcel.api.modules.users.enums;

public enum Role {
    ADMIN,
    MERCHANT,
    AGENT;

    public static Role from (String value) {
        return Role.valueOf(value.toUpperCase());
    }
}