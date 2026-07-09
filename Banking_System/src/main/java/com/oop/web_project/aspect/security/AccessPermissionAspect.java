package com.oop.web_project.aspect.security;

import com.oop.web_project.annotations.AccountAccessPermissionRequired;
import com.oop.web_project.annotations.CardAccessPermissionRequired;
import com.oop.web_project.annotations.CustomerAccessPermissionRequired;
import com.oop.web_project.entities.CheckActivityTarget;
import com.oop.web_project.entities.Role;
import com.oop.web_project.exceptions.accountExceptions.NotAccountOfCustomerException;
import com.oop.web_project.exceptions.cardExceptions.NotCardOfCustomerException;
import com.oop.web_project.exceptions.customerExceptions.CustomerAccessDeniedException;
import com.oop.web_project.exceptions.customerExceptions.CustomerDetailsNotFoundException;
import com.oop.web_project.exceptions.customerExceptions.CustomerIsNotAuthenticatedException;
import com.oop.web_project.exceptions.otherExceptions.CouldNotExtractIdException;
import com.oop.web_project.persistence.CustomerRepository;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.jspecify.annotations.NonNull;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.util.Arrays;


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

        String email = getEmail();

        boolean isManager = customerRepository.existsByEmailAndRole(email, Role.MANAGER);

        if(isManager){
            return;
        }

        Long cardId = extractId(jp, cardAccessPermissionRequired.idArgName());

        if (!customerRepository.customerWithEmailOwnsCard(email, cardId)) {
            throw new NotCardOfCustomerException("Current authenticated customer does not own the card!");
        }
    }

    @Before(value = "@annotation(accountAccessPermissionRequired)")
    public void checkAccountAccessPermission(JoinPoint jp,
                                          AccountAccessPermissionRequired accountAccessPermissionRequired) {

        String email = getEmail();

        boolean isManager = customerRepository.existsByEmailAndRole(email, Role.MANAGER);

        if(isManager){
            return;
        }

        Long accountId = extractId(jp, accountAccessPermissionRequired.idArgName());

        boolean accountHasCustomers = customerRepository.existsCustomerByAccounts_Id(accountId);
        boolean isAccountOwner = customerRepository.existsByEmailAndAccountsId(email, accountId);

        if (accountHasCustomers && !isAccountOwner) {
            throw new NotAccountOfCustomerException("Current authenticated customer does not own the account!");
        }
    }

    @Before(value = "@annotation(customerAccessPermissionRequired)")
    public void checkCustomerAccessPermission(JoinPoint jp,
                                          CustomerAccessPermissionRequired customerAccessPermissionRequired) {

        String email = getEmail();

        boolean isManager = customerRepository.existsByEmailAndRole(email, Role.MANAGER);

        if(isManager){
            return;
        }

        Object obj = jp.getArgs()[0];

        if (obj instanceof String customerEmail) {
            if (!email.equals(customerEmail)) {
                throw new CustomerAccessDeniedException(
                        "Current authenticated customer attempted to access other customer's information!"
                );
            }
            return;
        }

        Long customerId = extractId(jp, customerAccessPermissionRequired.idArgName());
        if (!customerRepository.existsByEmailAndId(email, customerId)) {
            throw new CustomerAccessDeniedException
                    ("Current authenticated customer attempted to access other customer's information!");
        }
    }

    private Long extractId(JoinPoint jp,
                           String paramName) {
        Object[] args = jp.getArgs();
        String[] argNames = ((MethodSignature)jp.getSignature()).getParameterNames();

        for(int i = 0; i < argNames.length; i++) {
            if(argNames[i].equals(paramName)) {
                Object arg = args[i];
                if(!(arg instanceof Long)) {
                    throw new IllegalArgumentException("Id has to be a Long type!..");
                }
                return (Long)arg;
            }
        }
        throw new CouldNotExtractIdException("Could not extract id parameter " +
                "from the method that requires id extraction!");
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
