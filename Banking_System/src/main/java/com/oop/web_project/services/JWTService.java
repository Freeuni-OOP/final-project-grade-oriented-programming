package com.oop.web_project.services;

import org.springframework.security.core.userdetails.UserDetails;

/**
 * Service interface defining operations necessary to generate, extract and validate JWT Tokens
 */
public interface JWTService {

    /**
     * Method takes in email of the customer and generates appropriate JWT token, which then will serve as a way to
     * authenticate the user
     * @param email email of the customer who is trying to authenticate
     * @return generated JWT Token
     */
    String generateToken(String email);

    /**
     * Method takes in jwt token and extracts email out of the token
     * @param jwtToken JWT token to be used to extract the email
     * @return email extracted from JWT Token
     */
    String extractEmail(String jwtToken);

    /**
     * Method looks at user's details and validates whether passed JWT token matches the details of the user
     * @param jwtToken JWT token to be used to extract the email
     * @param userDetails Details of the user, this instance comes from spring security
     * @return true, if user is validated, false otherwise
     */
    Boolean validateToken(String jwtToken, UserDetails userDetails);
}
