package com.oop.web_project.exceptions.cardExceptions;

public class CardNotFoundException extends RuntimeException{
    public CardNotFoundException(String message) {
        super(message);
    }
}
