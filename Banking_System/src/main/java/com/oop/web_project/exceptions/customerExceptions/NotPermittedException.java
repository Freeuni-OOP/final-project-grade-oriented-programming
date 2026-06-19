package com.oop.web_project.exceptions.customerExceptions;

public class NotPermittedException extends RuntimeException {
    public NotPermittedException(String message) {
        super(message);
    }
}
