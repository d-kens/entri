package com.api.common.utils;

import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    private SecurityUtils() {}

    public static String getCurrentUserExternalKey() {
        return (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
