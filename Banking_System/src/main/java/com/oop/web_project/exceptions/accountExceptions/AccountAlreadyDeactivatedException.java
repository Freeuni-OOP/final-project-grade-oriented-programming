package com.oop.web_project.exceptions.accountExceptions;

public class AccountAlreadyDeactivatedException extends RuntimeException{
    public AccountAlreadyDeactivatedException(String message) {
        super(message);
    }
}
