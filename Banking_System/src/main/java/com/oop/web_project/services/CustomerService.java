package com.oop.web_project.services;

import com.oop.web_project.entities.Account;
import com.oop.web_project.entities.Customer;

import java.util.List;

/**
 * Service interface defining operations for managing customers,
 * including lookup, activation, and account ownership queries.
 */
public interface CustomerService {

    /**
     * Retrieves a customer by their email address.
     */
    Customer getCustomerByEmail(String email);

    /**
     * Retrieves a customer by their unique ID.
     */
    Customer getCustomerByID(String id);

    /**
     * Activates the given customer.
     */
    void activateCustomer(Customer customer);

    /**
     * Deactivates the given customer.
     */
    void deactivateCustomer(Customer customer);

    /**
     * Returns all customers that jointly own the specified account.
     */
    List<Customer> getCustomersByAccount(Account account);
}