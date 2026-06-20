package com.oop.web_project.restController;

import com.oop.web_project.dto.requests.CustomerRegistrationRequest;
import com.oop.web_project.dto.requests.CustomerUpdateRequest;
import com.oop.web_project.dto.responses.CustomerProfileResponse;
import com.oop.web_project.entities.Customer;
import com.oop.web_project.mapping.CustomerApiMapper;
import com.oop.web_project.services.CustomerService;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/customer")
@Validated
public class CustomerRestController {

    private final CustomerService customerService;
    private final CustomerApiMapper customerApiMapper;

    public CustomerRestController(CustomerService customerService, CustomerApiMapper customerApiMapper){
        this.customerService = customerService;
        this.customerApiMapper = customerApiMapper;
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerCustomer(@Valid @RequestBody CustomerRegistrationRequest request){
        Customer customer = customerApiMapper.toCustomerOnRegistration(request);
        customerService.registerCustomer(customer);
        return ResponseEntity.status(HttpStatus.CREATED).body("The Customer has been registered successfully.");
    }

    @GetMapping("/{customer-id}")
    public ResponseEntity<CustomerProfileResponse> getCustomerProfile(@NotNull @Positive @PathVariable("customer-id") Long customerId){
        Customer customer = customerService.getCustomerById(customerId);
        CustomerProfileResponse response = customerApiMapper.toProfileResponse(customer);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/{customer-id}")
    public ResponseEntity<CustomerProfileResponse> updateCustomerProfile(@NotNull @Positive @PathVariable("customer-id") Long customerId, @Valid @RequestBody CustomerUpdateRequest request){
        customerService.updateCustomer(customerId, request.getFirstName(), request.getLastName(), request.getPhoneNumber(), request.getAddress());
        Customer customer = customerService.getCustomerById(customerId);
        CustomerProfileResponse response = customerApiMapper.toProfileResponse(customer);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PatchMapping("/{customer-id}/deactivate")
    public ResponseEntity<String> deactivateCustomer(@NotNull @Positive @PathVariable("customer-id") Long customerId){
        customerService.deactivateCustomer(customerId);
        return ResponseEntity.status(HttpStatus.OK).body("The customer has been deactivated successfully.");
    }

    @PatchMapping("/{customer-id}/activate")
    public ResponseEntity<String> activateCustomer(@NotNull @Positive @PathVariable("customer-id") Long customerId){
        customerService.activateCustomer(customerId);
        return ResponseEntity.status(HttpStatus.OK).body("The customer has been activated successfully.");
    }

    @DeleteMapping("/{customer-id}/delete")
    public ResponseEntity<String> deleteCustomer(@NotNull @Positive @PathVariable("customer-id") Long customerId){
        customerService.deleteCustomer(customerId);
        return ResponseEntity.status(HttpStatus.OK).body("The customer has been deleted successfully.");
    }

}
