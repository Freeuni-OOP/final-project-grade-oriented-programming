package com.oop.web_project.dto.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CustomerLoginRequest {

    @NotBlank(message = "Email should not be blank.")
    private String email;

    @NotBlank(message = "Password should not be blank.")
    private String password;
}
