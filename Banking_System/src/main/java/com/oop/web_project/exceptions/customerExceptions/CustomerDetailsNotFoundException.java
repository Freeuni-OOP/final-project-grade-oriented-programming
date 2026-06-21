package com.oop.web_project.exceptions.customerExceptions;

public class CustomerDetailsNotFoundException extends RuntimeException {
  public CustomerDetailsNotFoundException(String message) {
    super(message);
  }
}
