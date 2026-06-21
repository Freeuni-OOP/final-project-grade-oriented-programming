package com.oop.web_project.security;

import com.oop.web_project.annotations.AccountAccessPermissionRequired;
import com.oop.web_project.annotations.CardAccessPermissionRequired;
import com.oop.web_project.annotations.CustomerAccessPermissionRequired;
import com.oop.web_project.exceptions.accountExceptions.NotAccountOfCustomerException;
import com.oop.web_project.exceptions.cardExceptions.NotCardOfCustomerException;
import com.oop.web_project.exceptions.customerExceptions.CustomerAccessDeniedException;
import com.oop.web_project.exceptions.customerExceptions.CustomerDetailsNotFoundException;
import com.oop.web_project.exceptions.customerExceptions.CustomerIsNotAuthenticatedException;
import com.oop.web_project.persistence.CustomerRepository;
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
public class AccessPermissionAspect {

    private final CustomerRepository customerRepository;


    public AccessPermissionAspect(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }


    @Before(value = "@annotation(cardAccessPermissionRequired)")
    public void checkCardAccessPermission(JoinPoint jp,
                                        CardAccessPermissionRequired cardAccessPermissionRequired) {

        Long cardId = extractArg(jp);

        String email = getEmail();

        if(!customerRepository.customerWithEmailOwnsCard(email, cardId)) {
            throw new NotCardOfCustomerException("Current authenticated customer does not own the card!");
        }
    }

    @Before(value = "@annotation(accountAccessPermissionRequired)")
    public void checkAccountAccessPermission(JoinPoint jp,
                                          AccountAccessPermissionRequired accountAccessPermissionRequired) {

        Long accountId = extractArg(jp);

        String email = getEmail();

        if(!customerRepository.existsByEmailAndAccountsId(email, accountId)) {
            throw new NotAccountOfCustomerException("Current authenticated customer does not own the account!");
        }
    }

    @Before(value = "@annotation(customerAccessPermissionRequired)")
    public void checkCustomerAccessPermission(JoinPoint jp,
                                          CustomerAccessPermissionRequired customerAccessPermissionRequired) {
        Object obj = jp.getArgs()[0];

        String email = getEmail();

        if (obj instanceof String customerEmail) {
            if (!email.equals(customerEmail)) {
                throw new CustomerAccessDeniedException("Customer could not be authenticated");
            }
            return;
        }

        Long customerId = extractArg(jp);

        if(!customerRepository.existsByEmailAndId(email, customerId)) {
            throw new CustomerAccessDeniedException
                    ("Current authenticated customer attempted to access other customer's information!");
        }

    }

    private Long extractArg(JoinPoint jp) {
        Object obj = jp.getArgs()[0];

        if(!(obj instanceof Long id)) {
            throw new IllegalArgumentException("First argument must be type of long!");
        }
        return id;
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
