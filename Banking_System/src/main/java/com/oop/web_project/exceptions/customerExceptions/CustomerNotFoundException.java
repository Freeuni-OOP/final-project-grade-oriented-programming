package com.oop.web_project.exceptions.customerExceptions;

import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class CustomerNotFoundException extends UsernameNotFoundException {
    public CustomerNotFoundException(String message) {
        super(message);
    }
}
