package com.oop.web_project.exceptions.accountExceptions;

public class NotAccountOfCustomerException extends RuntimeException {
    public NotAccountOfCustomerException(String message) {
        super(message);
    }
}
