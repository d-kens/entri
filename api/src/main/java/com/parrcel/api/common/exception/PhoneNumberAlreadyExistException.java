package com.parrcel.api.common.exception;

public class PhoneNumberAlreadyExistException extends RuntimeException {
    public PhoneNumberAlreadyExistException() {
        super("User with phoneNumber already exist");
    }
}
