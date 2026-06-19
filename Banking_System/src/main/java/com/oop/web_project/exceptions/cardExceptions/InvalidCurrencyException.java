package com.oop.web_project.exceptions.cardExceptions;

public class InvalidCurrencyException extends RuntimeException{
    public InvalidCurrencyException(String message) {
        super(message);
    }
}
