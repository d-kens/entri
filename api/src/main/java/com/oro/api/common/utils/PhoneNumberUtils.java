package com.oro.api.common.utils;

public class PhoneNumberUtils {
    private PhoneNumberUtils() {}

    public static String normalize(String phoneNumber) {
        if (phoneNumber.startsWith("07") || phoneNumber.startsWith("01")) {
            return "254" + phoneNumber.substring(1);
        }
        if (phoneNumber.startsWith("+254")) {
            return phoneNumber.substring(1);
        }
        return phoneNumber;
    }
}
