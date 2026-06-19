package com.oop.web_project.exceptions.accountExceptions;

public class AccountAlreadyActiveException extends RuntimeException {
    public AccountAlreadyActiveException(String message) {
        super(message);
    }
}
