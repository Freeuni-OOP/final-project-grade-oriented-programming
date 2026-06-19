package com.oop.web_project.exceptions.customerExceptions;

public class CustomerAlreadyRegisteredException extends RuntimeException{
    public CustomerAlreadyRegisteredException(String message) {
        super(message);
    }
}
