package com.oop.web_project.exceptions.cardExceptions;

public class CardAlreadyExistsException extends RuntimeException{
    public CardAlreadyExistsException(String message) {
        super(message);
    }
}
