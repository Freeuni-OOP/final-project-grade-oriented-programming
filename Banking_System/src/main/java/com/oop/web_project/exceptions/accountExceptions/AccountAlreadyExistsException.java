package com.oop.web_project.exceptions.accountExceptions;

public class AccountAlreadyExistsException extends RuntimeException {
    public AccountAlreadyExistsException (String message) {
        super(message);
    }
}