package com.oop.web_project.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CustomerUpdateRequest {

    @NotBlank(message =  "First name should not be blank.")
    private String firstName;

    @NotBlank(message =  "Last name should not be blank.")
    private String lastName;

    @Pattern(regexp = "\\d+", message = "Phone number must contain only digits.")
    private String phoneNumber;

    private String address;
}
