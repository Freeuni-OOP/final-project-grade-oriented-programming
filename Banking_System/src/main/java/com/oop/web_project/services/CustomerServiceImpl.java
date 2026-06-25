package com.oop.web_project.services;

import com.oop.web_project.annotations.ActivityCheckRequired;
import com.oop.web_project.annotations.CustomerAccessPermissionRequired;
import com.oop.web_project.entities.Account;
import com.oop.web_project.entities.CheckActivityTarget;
import com.oop.web_project.entities.Customer;
import com.oop.web_project.exceptions.accountExceptions.AccountNotFoundException;
import com.oop.web_project.exceptions.customerExceptions.*;
import com.oop.web_project.persistence.*;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerServiceImpl implements CustomerService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomerServiceImpl(AccountRepository accountRepository,
                               CustomerRepository customerRepository,
                               PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void registerCustomer(Customer customer) {
        if(customer == null) {
            throw new IllegalArgumentException("customer cannot be null!");
        }

        if(customerRepository.existsByEmail(customer.getEmail())) {
            throw new CustomerAlreadyRegisteredException("Customer with this email already exists!");
        }

        customer.setHashedPassword(passwordEncoder.encode(customer.getHashedPassword()));
        customerRepository.save(customer);
    }

    @Override
    @CustomerAccessPermissionRequired
    public Customer getCustomerByEmail(String email) {
        return customerRepository.getCustomerByEmail(email)
                .orElseThrow(
                        () -> new CustomerNotFoundException("could not find customer with email!")
                );
    }

    @Override
    @CustomerAccessPermissionRequired
    public Customer getCustomerById(long customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(
                        () -> new CustomerNotFoundException("could not find customer with id!")
                );
    }

    @Override
    @CustomerAccessPermissionRequired
    @Transactional
    public void activateCustomer(long customerId) {
        Customer customer = customerRepository.findWithLockById(customerId).orElseThrow(
                () -> new CustomerNotFoundException("customer cannot be found!"));
        if(customer.isActive()) {
            throw new CustomerAlreadyActiveException("customer is already active!");
        }
        customer.setActive(true);
    }

    @Override
    @CustomerAccessPermissionRequired
    @Transactional
    public void deactivateCustomer(long customerId) {
        Customer customer = customerRepository.findWithLockById(customerId).orElseThrow(
                () -> new CustomerNotFoundException("customer cannot be found!"));
        if(!customer.isActive()) {
            throw new CustomerAlreadyDeactivatedException("customer is already inactive!");
        }
        customer.setActive(false);
    }

    @Override
    @Transactional
    public void deleteCustomer(long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(
                        () -> new CustomerNotFoundException("customer cannot be found!")
                );

        for (Account account : customer.getAccounts()) {
            account.getCustomers().remove(customer);
            accountRepository.save(account);
        }

        customerRepository.delete(customer);
    }

    @Override
    @CustomerAccessPermissionRequired
    @ActivityCheckRequired(checkActivityTarget = CheckActivityTarget.CUSTOMER)
    @Transactional
    public void updateCustomer(long customerId, String firstName, String lastName, String phoneNumber, String address) {
        Customer existingCustomer =  customerRepository.findWithLockById(customerId)
                .orElseThrow(
                        () -> new CustomerNotFoundException("customer cannot be found")
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
            throw new AccountNotFoundException("account cannot be found");
        }

        return customers;
    }
}
