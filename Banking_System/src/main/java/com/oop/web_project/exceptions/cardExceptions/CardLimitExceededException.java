package com.oop.web_project.exceptions.cardExceptions;

public class CardLimitExceededException extends RuntimeException{
    public CardLimitExceededException(String message) {
        super(message);
    }
}
