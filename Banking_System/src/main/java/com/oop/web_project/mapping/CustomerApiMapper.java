package com.oop.web_project.mapping;
import com.oop.web_project.dto.requests.CustomerRegistrationRequest;
import com.oop.web_project.dto.responses.AccountSummaryResponse;
import com.oop.web_project.dto.responses.CustomerProfileResponse;
import com.oop.web_project.entities.Account;
import com.oop.web_project.entities.Customer;
import com.oop.web_project.entities.Role;
import com.oop.web_project.utils.PasswordHasher;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CustomerApiMapper {

    private final AccountSummaryApiMapper accountSummaryApiMapper;

    public CustomerApiMapper(AccountSummaryApiMapper accountSummaryApiMapper) {
        this.accountSummaryApiMapper = accountSummaryApiMapper;
    }


    public Customer toCustomerOnRegistration(CustomerRegistrationRequest request){
        Customer customer = new Customer();
        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setPhoneNumber(request.getPhoneNumber());
        customer.setAddress(request.getAddress());
        customer.setDateOfBirth(request.getDateOfBirth());
        customer.setEmail(request.getEmail());
        customer.setAccounts(new ArrayList<>());
        customer.setActive(true);
        customer.setHashedPassword(PasswordHasher.hash(request.getPassword()));
        customer.setRole(Role.STANDARD);
        return customer;
    }

    public CustomerProfileResponse toProfileResponse(Customer customer){
        CustomerProfileResponse response = new CustomerProfileResponse();
        response.setFirstName(customer.getFirstName());
        response.setLastName(customer.getLastName());
        response.setDateOfBirth(customer.getDateOfBirth());
        response.setAddress(customer.getAddress());
        response.setEmail(customer.getEmail());
        response.setPhoneNumber(customer.getPhoneNumber());
        List<AccountSummaryResponse> summaryAccounts = new ArrayList<>();
        List<Account> accounts = customer.getAccounts();
        for(Account account : accounts){
            summaryAccounts.add(accountSummaryApiMapper.toAccountSummaryResponse(account));
        }
        response.setAccounts(summaryAccounts);
        return response;
    }

}
