package com.parrcel.api.common.exception;

public class EmailAlreadyExistException extends RuntimeException {
    public EmailAlreadyExistException() {
        super("User with email already exist");
    }
}
