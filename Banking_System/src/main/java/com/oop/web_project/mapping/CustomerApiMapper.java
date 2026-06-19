package com.oop.web_project.mapping;

import com.oop.web_project.dto.requests.CustomerRegistrationRequest;
import com.oop.web_project.entities.Customer;
import com.oop.web_project.entities.Role;
import com.oop.web_project.utils.PasswordHasher;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class CustomerApiMapper {


    public Customer toCustomer(CustomerRegistrationRequest request){
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
}
