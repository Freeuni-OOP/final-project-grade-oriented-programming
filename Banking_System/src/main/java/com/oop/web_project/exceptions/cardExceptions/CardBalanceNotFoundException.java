package com.oop.web_project.exceptions.cardExceptions;

public class CardBalanceNotFoundException extends RuntimeException{
    public CardBalanceNotFoundException(String message) {
        super(message);
    }
}
