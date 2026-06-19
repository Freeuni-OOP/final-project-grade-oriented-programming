package com.oop.web_project.exceptions.customerExceptions;

public class InvalidCustomerEmailException extends RuntimeException {
    public InvalidCustomerEmailException(String message) {
        super(message);
    }
}
