package com.oop.web_project.restController;

import com.oop.web_project.dto.requests.CustomerLoginRequest;
import com.oop.web_project.dto.requests.CustomerRegistrationRequest;
import com.oop.web_project.entities.Customer;
import com.oop.web_project.mapping.CustomerApiMapper;
import com.oop.web_project.services.AuthService;
import com.oop.web_project.services.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Operations for authenticating customers")
@Validated
public class AuthRestController {

    private final AuthService authService;
    private final CustomerService customerService;
    private final CustomerApiMapper customerApiMapper;

    public AuthRestController(AuthService authService, CustomerService customerService,
                              CustomerApiMapper customerApiMapper) {
        this.authService = authService;
        this.customerService = customerService;
        this.customerApiMapper = customerApiMapper;
    }

    @Operation(summary = "Register a new customer", description = "Creates a new customer account")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Customer registered successfully",
                    content = @Content(schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "400", description = "Invalid request body", content = @Content),
            @ApiResponse(responseCode = "409", description = "Customer already exists", content = @Content)
    })
    @PostMapping("/register")
    public ResponseEntity<String> registerCustomer(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Customer registration details", required = true,
                    content = @Content(schema = @Schema(implementation = CustomerRegistrationRequest.class)))
            @Valid @RequestBody CustomerRegistrationRequest request) {
        Customer customer = customerApiMapper.toCustomerOnRegistration(request);
        customerService.registerCustomer(customer);
        return ResponseEntity.status(HttpStatus.CREATED).body("The Customer has been registered successfully.");
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> loginCustomer
            (@Valid @RequestBody CustomerLoginRequest customerLoginRequest) {
        String jwtToken =
                authService.authenticateCustomer(customerLoginRequest.getEmail(), customerLoginRequest.getPassword());
        return ResponseEntity.ok(
                Map.of(
                        "Status message", "Authentication was successful!",
                        "Generated JWT token",  jwtToken
                )
        );
    }

    @GetMapping("/validate")
    public ResponseEntity<String> validateCustomer() {
        return ResponseEntity.ok("User is authenticated!");
    }
}
