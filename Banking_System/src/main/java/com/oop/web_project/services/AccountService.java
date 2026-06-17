package com.oop.web_project.services;

import com.oop.web_project.entities.Account;
import com.oop.web_project.entities.Card;
import com.oop.web_project.entities.Customer;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service interface defining operations for managing accounts,
 * including creation, customer registration, and balance retrieval.
 */
public interface AccountService {

    /**
     * Creates a new account and attaches it to the given customer.
     */
    void createAccount(Account account, Customer customer);

    /**
     * Attempts to register the given customer as an owner of the specified account.
     */
    void registerCustomerToAccount(Account account, Customer customer);

    /**
     * Retrieves the current balance of the given account.
     */
    BigDecimal getAccountBalance(Account account);
}