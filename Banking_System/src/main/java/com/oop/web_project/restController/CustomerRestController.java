package com.oop.web_project.restController;

import com.oop.web_project.dto.requests.CustomerRegistrationRequest;
import com.oop.web_project.dto.requests.CustomerUpdateRequest;
import com.oop.web_project.dto.responses.CustomerProfileResponse;
import com.oop.web_project.entities.Customer;
import com.oop.web_project.mapping.CustomerApiMapper;
import com.oop.web_project.services.CustomerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/customer")
public class CustomerRestController {

    private final CustomerService customerService;
    private final CustomerApiMapper customerApiMapper;

    public CustomerRestController(CustomerService customerService, CustomerApiMapper customerApiMapper){
        this.customerService = customerService;
        this.customerApiMapper = customerApiMapper;
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerCustomer(@RequestBody @Valid CustomerRegistrationRequest request){
        Customer customer = customerApiMapper.toCustomerOnRegistration(request);
        customerService.registerCustomer(customer);
        return ResponseEntity.status(HttpStatus.CREATED).body("The Customer has been registered successfully.");
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerProfileResponse> getCustomerProfile(@PathVariable Long id){
        Customer customer = customerService.getCustomerById(id);
        CustomerProfileResponse response = customerApiMapper.toProfileResponse(customer);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerProfileResponse> updateCustomerProfile(@PathVariable Long id, @RequestBody @Valid CustomerUpdateRequest request){
        customerService.updateCustomer(id, request.getFirstName(), request.getLastName(), request.getPhoneNumber(), request.getAddress());
        Customer customer = customerService.getCustomerById(id);
        CustomerProfileResponse response = customerApiMapper.toProfileResponse(customer);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<String> deactivateCustomer(@PathVariable Long id){
        customerService.deactivateCustomer(id);
        return ResponseEntity.status(HttpStatus.OK).body("The customer has been deactivated successfully.");
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<String> activateCustomer(@PathVariable Long id){
        customerService.activateCustomer(id);
        return ResponseEntity.status(HttpStatus.OK).body("The customer has been activated successfully.");
    }

    @DeleteMapping("/{id}/delete")
    public ResponseEntity<String> deleteCustomer(@PathVariable Long id){
        customerService.deleteCustomer(id);
        return ResponseEntity.status(HttpStatus.OK).body("The customer has been deleted successfully.");
    }



}
