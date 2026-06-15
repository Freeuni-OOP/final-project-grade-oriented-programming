package com.oop.web_project.services;

import com.oop.web_project.Entities.Customer;

public interface CustomerService {
    /*
     * These methods are self-explanatory.
     */
    public Customer getCustomerByEmail(String email);

    public Customer getCustomerByID(String id);

    public void activateCustomer(Customer customer);

    public void deactivateCustomer(Customer customer);
}
