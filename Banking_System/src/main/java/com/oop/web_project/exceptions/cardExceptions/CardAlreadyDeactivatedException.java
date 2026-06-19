package com.oop.web_project.exceptions.cardExceptions;

public class CardAlreadyDeactivatedException extends RuntimeException{
    public CardAlreadyDeactivatedException(String message) {
        super(message);
    }
}
