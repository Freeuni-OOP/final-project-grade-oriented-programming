package com.oop.web_project.services;

public interface AuthService {

    /**
     * Method looks at email and password of the customer, validates it on backend and authenticates customer
     * @param email customer's email
     * @param password customer's password
     * @return JWT Token, if authentication is successful, 'Failure' otherwise
     */
    String authenticateCustomer(String email, String password);

}
