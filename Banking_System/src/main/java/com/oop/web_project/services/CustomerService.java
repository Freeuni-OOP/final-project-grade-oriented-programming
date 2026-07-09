package com.oop.web_project.services;

import com.oop.web_project.dto.requests.CustomerFilterRequest;
import com.oop.web_project.dto.requests.PageRequest;
import com.oop.web_project.entities.Account;
import com.oop.web_project.entities.Customer;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Service interface defining operations for managing customers,
 * including lookup, activation, and account ownership queries.
 */
public interface CustomerService {


    /**
     * registers new customer and
     * returns id of the newly registered customer
     */
    long registerCustomer(Customer customer);

    /**
     * Retrieves a customer by their email address.
     */
    Customer getCustomerByEmail(String email);

    /**
     * Retrieves a customer by their unique ID.
     */
    Customer getCustomerById(long id);

    /**
     * Activates the given customer.
     */
    void activateCustomer(long customerId);

    /**
     * Deactivates the given customer.
     */
    void deactivateCustomer(long customerId);

    /**
     * finds customer and deletes him/her from database.
     */
    void deleteCustomer(long customerId);

    /**
     * finds customer and updates his/her credentials
     */
    void updateCustomer(long customerId, String firstName, String lastName, String phoneNumber, String address);

    /**
     * Returns all customers that jointly own the specified account.
     */
    List<Customer> getCustomersByAccount(long accountId);

    /**
     * Retrieves customers based on given parameters, applies paging and sorting
     */
    Page<Customer> filterCustomers(CustomerFilterRequest customerFilterRequest, PageRequest pageRequest);
}