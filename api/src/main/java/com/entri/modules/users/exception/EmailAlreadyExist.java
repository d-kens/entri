package com.entri.modules.users.exception;

public class EmailAlreadyExist extends RuntimeException {
    public EmailAlreadyExist() {
        super("Email already exist");
    }
}
