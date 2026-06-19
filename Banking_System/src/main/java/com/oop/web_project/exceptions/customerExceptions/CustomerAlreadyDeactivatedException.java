package com.oop.web_project.exceptions.customerExceptions;

public class CustomerAlreadyDeactivatedException extends RuntimeException{
    public CustomerAlreadyDeactivatedException(String message) {
        super(message);
    }
}
