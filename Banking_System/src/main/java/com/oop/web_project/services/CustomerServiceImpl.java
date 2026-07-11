package com.oop.web_project.services;

import com.oop.web_project.annotations.ActivityCheckRequired;
import com.oop.web_project.annotations.CustomerAccessPermissionRequired;
import com.oop.web_project.dto.requests.CustomerFilterRequest;
import com.oop.web_project.dto.requests.PageRequest;
import com.oop.web_project.entities.Account;
import com.oop.web_project.entities.CheckActivityTarget;
import com.oop.web_project.entities.Customer;
import com.oop.web_project.exceptions.accountExceptions.AccountNotFoundException;
import com.oop.web_project.exceptions.customerExceptions.*;
import com.oop.web_project.persistence.*;
import com.oop.web_project.utils.PageUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public long registerCustomer(Customer customer) {
        if(customer == null) {
            throw new IllegalArgumentException("customer cannot be null!");
        }

        if(customerRepository.existsByEmail(customer.getEmail())) {
            throw new CustomerAlreadyRegisteredException("Customer with this email already exists!");
        }

        customer.setHashedPassword(passwordEncoder.encode(customer.getHashedPassword()));
        Customer createdCustomer = customerRepository.save(customer);
        return createdCustomer.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public Customer getCustomerByEmail(String email) {
        return customerRepository.getCustomerByEmail(email)
                .orElseThrow(
                        () -> new CustomerNotFoundException("could not find customer with email!")
                );
    }

    @Override
    @Transactional(readOnly = true)
    @CustomerAccessPermissionRequired(idArgName = "customerId")
    public Customer getCustomerById(long customerId) {
        return customerRepository.findByIdWithDetails(customerId)
                .orElseThrow(
                        () -> new CustomerNotFoundException("could not find customer with id!")
                );
    }

    @Override
    @CustomerAccessPermissionRequired(idArgName = "customerId")
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
    @CustomerAccessPermissionRequired(idArgName = "customerId")
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
    @CustomerAccessPermissionRequired(idArgName = "customerId")
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
    @Transactional(readOnly = true)
    public List<Customer> getCustomersByAccount(long accountId) {
        List<Customer> customers =  customerRepository.getCustomersByAccounts_Id(accountId);

        if (customers.isEmpty() && !accountRepository.existsById(accountId)) {
            throw new AccountNotFoundException("account cannot be found");
        }

        return customers;
    }

    @Override
    @Transactional(readOnly = true)
    @CustomerAccessPermissionRequired
    public Page<Customer> filterCustomers(CustomerFilterRequest customerFilterRequest, PageRequest pageRequest) {

        Specification<Customer> specification = Specification.unrestricted();

        if (customerFilterRequest.getFirstName() != null && !customerFilterRequest.getFirstName().isEmpty()) {
            specification = specification.and((root, query, cb) ->
                    cb.equal(root.get("firstName"), customerFilterRequest.getFirstName()));
        }
        if (customerFilterRequest.getLastName() != null && !customerFilterRequest.getLastName().isEmpty()) {
            specification = specification.and((root, query, cb) ->
                    cb.equal(root.get("lastName"), customerFilterRequest.getLastName()));
        }
        if (customerFilterRequest.getEmail() != null && !customerFilterRequest.getEmail().isEmpty()) {
            specification = specification.and((root, query, cb) ->
                    cb.equal(root.get("email"), customerFilterRequest.getEmail()));
        }

        return customerRepository.findAll(specification, PageUtils.buildPageable(pageRequest));
    }
}
