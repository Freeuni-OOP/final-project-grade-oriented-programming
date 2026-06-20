package com.oop.web_project.dto.requests;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CustomerRegistrationRequest {

    @NotBlank(message =  "First name should not be blank.")
    private String firstName;

    @NotBlank(message =  "Last name should not be blank.")
    private String lastName;

    @Pattern(regexp = "\\d+", message = "Phone number must contain only digits.")
    private String phoneNumber;

    private String address;

    @NotNull(message = "Date of birth should not be missing.")
    private LocalDate dateOfBirth;

    @NotBlank(message = "Email should not be blank.")
    @Email(message = "Email must be valid")
    private String email;


    @NotBlank(message = "Password should not be blank.")
    @Size(min = 6, max = 20, message = "Password should be between 6 and 20 characters.")
    private String password;

}
