package com.oop.web_project.restController;

import com.oop.web_project.dto.requests.CustomerUpdateRequest;
import com.oop.web_project.dto.responses.CustomerProfileResponse;
import com.oop.web_project.entities.Customer;
import com.oop.web_project.mapping.CustomerApiMapper;
import com.oop.web_project.services.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer")
@Tag(name = "Customer", description = "Operations for managing customers")
@Validated
public class CustomerRestController {

    private final CustomerService customerService;
    private final CustomerApiMapper customerApiMapper;

    public CustomerRestController(CustomerService customerService, CustomerApiMapper customerApiMapper) {
        this.customerService = customerService;
        this.customerApiMapper = customerApiMapper;
    }

    @Operation(summary = "Get customer profile", description = "Retrieves the profile of a customer by their ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Customer profile retrieved successfully",
                    content = @Content(schema = @Schema(implementation = CustomerProfileResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid customer ID", content = @Content),
            @ApiResponse(responseCode = "404", description = "Customer not found", content = @Content)
    })
    @PreAuthorize("hasAuthority(\"STANDARD\")")
    @GetMapping("/{customer-id}")
    public ResponseEntity<CustomerProfileResponse> getCustomerProfile(@NotNull @Positive @PathVariable("customer-id") Long customerId){
        Customer customer = customerService.getCustomerById(customerId);
        CustomerProfileResponse response = customerApiMapper.toProfileResponse(customer);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "Update customer profile", description = "Updates the profile details of an existing customer")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Customer updated successfully",
                    content = @Content(schema = @Schema(implementation = CustomerProfileResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request body or customer ID", content = @Content),
            @ApiResponse(responseCode = "404", description = "Customer not found", content = @Content)
    })
    @PreAuthorize("hasAuthority(\"STANDARD\")")
    @PutMapping("/{customer-id}")
    public ResponseEntity<CustomerProfileResponse> updateCustomerProfile(@NotNull @Positive @PathVariable("customer-id") Long customerId, @Valid @RequestBody CustomerUpdateRequest request){
        customerService.updateCustomer(customerId, request.getFirstName(), request.getLastName(), request.getPhoneNumber(), request.getAddress());
        Customer customer = customerService.getCustomerById(customerId);
        CustomerProfileResponse response = customerApiMapper.toProfileResponse(customer);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "Deactivate a customer", description = "Sets the customer's account status to inactive")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Customer deactivated successfully",
                    content = @Content(schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "400", description = "Invalid customer ID", content = @Content),
            @ApiResponse(responseCode = "404", description = "Customer not found", content = @Content),
            @ApiResponse(responseCode = "406", description = "Customer is already inactive", content = @Content)
    })
    @PreAuthorize("hasAuthority(\"STANDARD\")")
    @PatchMapping("/{customer-id}/deactivate")
    public ResponseEntity<String> deactivateCustomer(@NotNull @Positive @PathVariable("customer-id") Long customerId){
        customerService.deactivateCustomer(customerId);
        return ResponseEntity.status(HttpStatus.OK).body("The customer has been deactivated successfully.");
    }

    @Operation(summary = "Activate a customer", description = "Sets the customer's account status to active")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Customer activated successfully",
                    content = @Content(schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "400", description = "Invalid customer ID", content = @Content),
            @ApiResponse(responseCode = "404", description = "Customer not found", content = @Content),
            @ApiResponse(responseCode = "406", description = "Customer is already active", content = @Content)
    })
    @PreAuthorize("hasAuthority(\"STANDARD\")")
    @PatchMapping("/{customer-id}/activate")
    public ResponseEntity<String> activateCustomer(@NotNull @Positive @PathVariable("customer-id") Long customerId){
        customerService.activateCustomer(customerId);
        return ResponseEntity.status(HttpStatus.OK).body("The customer has been activated successfully.");
    }

    @Operation(summary = "Delete a customer", description = "Permanently removes a customer from the system")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Customer deleted successfully",
                    content = @Content(schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "400", description = "Invalid customer ID", content = @Content),
            @ApiResponse(responseCode = "404", description = "Customer not found", content = @Content)
    })
    @PreAuthorize("hasAuthority(\"MANAGER\")")
    @DeleteMapping("/{customer-id}/delete")
    public ResponseEntity<String> deleteCustomer(@NotNull @Positive @PathVariable("customer-id") Long customerId){
        customerService.deleteCustomer(customerId);
        return ResponseEntity.status(HttpStatus.OK).body("The customer has been deleted successfully.");
    }
}