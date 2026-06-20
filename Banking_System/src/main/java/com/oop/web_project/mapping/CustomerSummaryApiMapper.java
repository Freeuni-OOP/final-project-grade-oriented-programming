package com.oop.web_project.mapping;

import com.oop.web_project.dto.responses.CustomerSummaryResponse;
import com.oop.web_project.entities.Customer;
import org.springframework.stereotype.Component;

@Component
public class CustomerSummaryApiMapper {
    public CustomerSummaryResponse toSummaryResponse(Customer customer){
        CustomerSummaryResponse response = new CustomerSummaryResponse();
        response.setEmail(customer.getEmail());
        response.setFirstName(customer.getFirstName());
        response.setLastName(customer.getLastName());
        return response;
    }
}