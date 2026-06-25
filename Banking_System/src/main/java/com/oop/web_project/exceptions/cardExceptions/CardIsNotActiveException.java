package com.oop.web_project.exceptions.cardExceptions;

public class CardIsNotActiveException extends RuntimeException {
    public CardIsNotActiveException(String message) {
        super(message);
    }
}
