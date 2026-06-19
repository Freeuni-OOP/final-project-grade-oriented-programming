package com.oop.web_project.exceptions.cardExceptions;

public class InsufficientMoneyOnCardException extends RuntimeException{
    public InsufficientMoneyOnCardException(String message) {
        super(message);
    }
}
