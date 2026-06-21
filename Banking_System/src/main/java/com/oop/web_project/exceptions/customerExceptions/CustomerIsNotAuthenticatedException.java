package com.oop.web_project.exceptions.customerExceptions;

public class CustomerIsNotAuthenticatedException extends RuntimeException {
  public CustomerIsNotAuthenticatedException(String message) {
    super(message);
  }
}
