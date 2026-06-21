package com.oop.web_project.exceptions.customerExceptions;

public class CustomerAccessDeniedException extends RuntimeException {
    public CustomerAccessDeniedException(String message) {
        super(message);
    }
}
