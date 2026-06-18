package com.oop.web_project.services;

import com.oop.web_project.entities.Account;
import com.oop.web_project.entities.Customer;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerServiceImpl implements CustomerService {

    @Override
    public void registerCustomer(Customer customer) {

    }

    @Override
    public Customer getCustomerByEmail(String email) {
        return null;
    }

    @Override
    public Customer getCustomerByID(String id) {
        return null;
    }

    @Override
    public void activateCustomer(Customer customer) {

    }

    @Override
    public void deactivateCustomer(Customer customer) {

    }

    @Override
    public void deleteCustomer(long customerId) {

    }

    @Override
    public void updateCustomer(long customerId, Customer customer) {

    }

    @Override
    public List<Customer> getCustomersByAccount(Account account) {
        return List.of();
    }
}
