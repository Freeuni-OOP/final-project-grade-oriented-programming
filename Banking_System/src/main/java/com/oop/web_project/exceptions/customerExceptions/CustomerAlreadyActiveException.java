package com.oop.web_project.exceptions.customerExceptions;

public class CustomerAlreadyActiveException extends RuntimeException{
    public CustomerAlreadyActiveException(String message) {
        super(message);
    }
}
