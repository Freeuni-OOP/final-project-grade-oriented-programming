package com.oop.web_project.exceptions.cardExceptions;

public class DuplicateCurrencyException extends RuntimeException {
    public DuplicateCurrencyException(String message) {
        super(message);
    }
}
