package com.entri.common.exception;

public class EmailAlreadyExist extends RuntimeException {
    public EmailAlreadyExist() {
        super("Email already exist");
    }
}
