package com.oop.web_project.exceptions.cardExceptions;

public class CardAlreadyActiveException extends RuntimeException{
    public CardAlreadyActiveException(String message) {
        super(message);
    }
}
