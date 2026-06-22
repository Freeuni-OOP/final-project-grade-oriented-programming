package com.oop.web_project.exceptions.cardExceptions;

public class NotCardOfCustomerException extends RuntimeException {
    public NotCardOfCustomerException(String message) {
        super(message);
    }
}
