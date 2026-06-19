package com.oop.web_project.exceptions.accountExceptions;

public class CustomerAlreadyLinkedToThisAccountException extends RuntimeException{
    public CustomerAlreadyLinkedToThisAccountException(String message) {
        super(message);
    }
}
