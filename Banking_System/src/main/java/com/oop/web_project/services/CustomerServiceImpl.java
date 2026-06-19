package com.oop.web_project.services;

import com.oop.web_project.entities.Account;
import com.oop.web_project.entities.Customer;
import com.oop.web_project.persistence.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerServiceImpl implements CustomerService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;

    public CustomerServiceImpl(AccountRepository accountRepository,
                               CustomerRepository customerRepository) {
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
    }

    @Override
    @Transactional
    public void registerCustomer(Customer customer) {
        if(customer == null) {
            throw new IllegalArgumentException("customer cannot be null!");
        }
        customerRepository.save(customer);
    }

    @Override
    public Customer getCustomerByEmail(String email) {
        return customerRepository.getCustomerByEmail(email)
                .orElseThrow(
                        () -> new IllegalArgumentException("could not find customer with email!")
                );
    }

    @Override
    public Customer getCustomerById(long id) {
        return customerRepository.findById(id)
                .orElseThrow(
                        () -> new IllegalArgumentException("could not find customer with id!")
                );
    }

    @Override
    @Transactional
    public void activateCustomer(long customerId) {
        Customer customer = customerRepository.findById(customerId).orElseThrow(
                () -> new IllegalArgumentException("customer cannot be found!"));
        if(customer.isActive()) {
            throw new IllegalArgumentException("customer is already active!");
        }
        customer.setActive(true);
    }

    @Override
    @Transactional
    public void deactivateCustomer(long customerId) {
        Customer customer = customerRepository.findById(customerId).orElseThrow(
                () -> new IllegalArgumentException("customer cannot be found!"));
        if(!customer.isActive()) {
            throw new IllegalArgumentException("customer is already inactive!");
        }
        customer.setActive(false);
    }

    @Override
    @Transactional
    public void deleteCustomer(long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(
                        () -> new IllegalArgumentException("customer cannot be found!")
                );

        for (Account account : customer.getAccounts()) {
            account.getCustomers().remove(customer);
            accountRepository.save(account);
        }

        customerRepository.delete(customer);
    }

    @Override
    @Transactional
    public void updateCustomer(long customerId, String firstName, String lastName, String phoneNumber, String address) {
        Customer existingCustomer =  customerRepository.findById(customerId)
                .orElseThrow(
                        () -> new IllegalArgumentException("customer cannot be found")
                );
        if(firstName != null)existingCustomer.setFirstName(firstName);
        if(lastName != null)existingCustomer.setLastName(lastName);
        if(phoneNumber != null)existingCustomer.setPhoneNumber(phoneNumber);
        if(address != null)existingCustomer.setAddress(address);
    }

    @Override
    public List<Customer> getCustomersByAccount(long accountId) {
        List<Customer> customers =  customerRepository.getCustomersByAccounts_Id(accountId);

        if (customers.isEmpty() && !accountRepository.existsById(accountId)) {
            throw new IllegalArgumentException("account cannot be found");
        }

        return customers;
    }
}
