package com.oop.web_project.exceptions.customerExceptions;

public class CustomerCannotBeAuthenticatedException extends RuntimeException {
    public CustomerCannotBeAuthenticatedException(String message) {
        super(message);
    }
}
