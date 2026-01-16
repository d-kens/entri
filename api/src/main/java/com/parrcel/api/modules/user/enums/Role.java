package com.parrcel.api.modules.user.enums;

public enum Role {
    ADMIN,
    MERCHANT,
    AGENT;

    public static Role from (String value) {
        return Role.valueOf(value.toUpperCase());
    }
}