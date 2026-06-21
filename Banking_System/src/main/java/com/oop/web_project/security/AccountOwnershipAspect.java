package com.oop.web_project.security;

import com.oop.web_project.annotations.AccountOwnershipRequired;
import com.oop.web_project.entities.Account;
import com.oop.web_project.entities.Customer;
import com.oop.web_project.exceptions.accountExceptions.NotAccountOfCustomerException;
import com.oop.web_project.exceptions.customerExceptions.CustomerDetailsNotFoundException;
import com.oop.web_project.exceptions.customerExceptions.CustomerIsNotAuthenticatedException;
import com.oop.web_project.services.AccountService;
import com.oop.web_project.services.CustomerService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.jspecify.annotations.NonNull;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;


@Aspect
@Component
@Order(1)
public class AccountOwnershipAspect {

    private final CustomerService customerService;


    public AccountOwnershipAspect(CustomerService customerService) {
        this.customerService = customerService;
    }


    @Before(value = "@annotation(accountOwnershipRequired)")
    public void checkAccountOwnership(JoinPoint jp,
                                        AccountOwnershipRequired accountOwnershipRequired) {

        Object obj = jp.getArgs()[0];

        if(!(obj instanceof Long cardId)) {
            throw new IllegalArgumentException("First argument must be type of long!");
        }

        String email = getEmail();

        if(!customerService.customerOwnsAccountWithCard(email, cardId)) {
            throw new NotAccountOfCustomerException("Current authenticated customer does not own the account!");
        }
    }

    private static @NonNull String getEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if(auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            throw new CustomerIsNotAuthenticatedException("Customer is not authenticated!");
        }

        UserDetails userDetails = (UserDetails)auth.getPrincipal();

        if(userDetails == null) {
            throw new CustomerDetailsNotFoundException("Could not find user details!");
        }

        return userDetails.getUsername();
    }

}
