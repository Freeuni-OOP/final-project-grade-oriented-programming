package com.oop.web_project.restController;

import com.oop.web_project.dto.requests.CustomerRegistrationRequest;
import com.oop.web_project.entities.Customer;
import com.oop.web_project.mapping.CustomerApiMapper;
import com.oop.web_project.services.CustomerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("api/customer")
public class CustomerRestController {

    private final CustomerService customerService;
    private final CustomerApiMapper customerApiMapper;

    public CustomerRestController(CustomerService customerService, CustomerApiMapper customerApiMapper){
        this.customerService = customerService;
        this.customerApiMapper = customerApiMapper;
    }

    @PostMapping("/register")
    public ResponseEntity<Void> registerCustomer(@RequestBody @Valid CustomerRegistrationRequest request){
        Customer customer = customerApiMapper.toCustomer(request);
        customerService.registerCustomer(customer);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }




}
