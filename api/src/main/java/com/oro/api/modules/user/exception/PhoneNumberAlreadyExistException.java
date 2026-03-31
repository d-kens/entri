package com.oro.api.modules.user.exception;

public class PhoneNumberAlreadyExistException extends RuntimeException {
    public PhoneNumberAlreadyExistException() {
        super("User with phoneNumber already exist");
    }
}
