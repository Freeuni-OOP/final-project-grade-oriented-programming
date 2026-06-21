package com.oop.web_project.services;

import com.oop.web_project.exceptions.customerExceptions.CustomerCannotBeAuthenticatedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {
    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;

    public AuthServiceImpl(AuthenticationManager authenticationManager,
                           JWTService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Override
    public String authenticateCustomer(String email, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        email, password
                )
        );

        if(!authentication.isAuthenticated()) {
            throw new CustomerCannotBeAuthenticatedException("Customer cannot be authenticated!");
        }

        return jwtService.generateToken(email);
    }
}
