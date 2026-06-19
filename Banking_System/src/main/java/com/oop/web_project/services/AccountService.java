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
     * Creates a new account
     */
    void createAccount(Account account);

    /**
     * activates the account
     * @param accountId id of the account we need to activate
     */
    void activateAccount(long accountId);

    /**
     * deactivates the account
     * @param accountId id of the account we need to deactivate
     */
    void deactivateAccount(long accountId);

    /**
     * deletes account from database
     * @param accountId id of the account we need to delete
     */
    void deleteAccount(long accountId);

    /**
     *
     * @param accountId for account in the database by id
     * @return returns appropriate account
     */
    Account selectAccountById(long accountId);
    /**
     * looks for accounts associated to the email of the particular customer
     * @param customerEmail email of the customer whose accounts we are looking for
     * @return list of accounts customer has
     */
    List<Account> selectAccountsByCustomerEmail(String customerEmail);

    /**
     * looks for accounts associated to the customer with id
     * @param customerId id of the customer whose accounts we are looking for
     * @return list of accounts customer has
     */
    List<Account> selectAccountsByCustomerId(long customerId);

    /**
     * finds the account in the database and updates its credentials
     * @param accountId account's id we are looking to update
     * @param accountName account's name
     */
    void updateAccount(long accountId, String accountName);
    /**
     * Attempts to register the given customer as an owner of the specified account.
     */
    void registerCustomerToAccount(long accountId, long customerId);

    /**
     * Retrieves the total balance of the given account, which is sum of money in each card's
     * particular currency
     */
    BigDecimal getAccountBalanceByCurrency(long accountId, String currencyName);
}