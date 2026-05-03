package com.puuul.api.modules.users.exception;

public class EmailAlreadyExist extends RuntimeException {
    public EmailAlreadyExist() {
        super("Email already exist");
    }
}
