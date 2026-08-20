package com.entri.shared.exception;

public class EmailAlreadyExist extends RuntimeException {
    public EmailAlreadyExist() {
        super("Email already exist");
    }
}
