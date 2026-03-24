package com.parrcel.api.modules.users.exception;

public class PhoneNumberAlreadyExistException extends RuntimeException {
    public PhoneNumberAlreadyExistException() {
        super("User with phoneNumber already exist");
    }
}
