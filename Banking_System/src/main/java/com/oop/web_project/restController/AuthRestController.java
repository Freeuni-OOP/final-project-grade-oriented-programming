package com.oop.web_project.restController;

import com.oop.web_project.dto.requests.CustomerLoginRequest;
import com.oop.web_project.services.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Validated
public class AuthRestController {

    private final AuthService authService;

    public AuthRestController(AuthService authService) {
        this.authService = authService;
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
