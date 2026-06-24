package com.oop.web_project.exceptions.customerExceptions;

public class CustomerIsNotActiveException extends RuntimeException {
    public CustomerIsNotActiveException(String message) {
        super(message);
    }
}
