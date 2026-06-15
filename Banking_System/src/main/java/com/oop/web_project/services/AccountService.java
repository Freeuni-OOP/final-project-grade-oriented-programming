package com.oop.web_project.services;

import com.oop.web_project.Entities.Account;
import com.oop.web_project.Entities.Card;
import com.oop.web_project.Entities.Customer;

import java.math.BigDecimal;
import java.util.List;

public interface AccountService {
    /*
     * This creates an account, which will be attached to a customer.
     */
    public void createAccount(Account account, Customer customer);

    /*
     * If user calls this method, he will be passed all his cards, on certain account.
     */
    public List<Card> getAllCardsForAccount(Account account);

    /*
     * This Method returns all the usernames of the customers that
     * jointly own some account.
     */
    public List<String> getAccountCustomers(Account account);

    /*
     * Attempts to add a user to specified account.
     */
    public void registerCustomerToAccount(Account account, Customer customer);

    /*
     * self-explanatory
     */
    public BigDecimal getAccountBalance(Account account);
}
