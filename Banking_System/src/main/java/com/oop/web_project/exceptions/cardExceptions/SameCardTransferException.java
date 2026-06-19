package com.oop.web_project.exceptions.cardExceptions;

public class SameCardTransferException extends RuntimeException{
    public SameCardTransferException(String message) {
        super(message);
    }
}