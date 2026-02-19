package com.parrcel.api.modules.payment.utils;

public class PhoneNumberUtils {
    private PhoneNumberUtils() {}

    public static String normalize(String phone) {
        if (phone.startsWith("07") || phone.startsWith("01")) {
            return "254" + phone.substring(1);
        }
        return phone;
    }
}
