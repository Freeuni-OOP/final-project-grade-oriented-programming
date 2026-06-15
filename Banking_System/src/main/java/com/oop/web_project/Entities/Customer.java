package com.oop.web_project.Entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;


/* This class keeps information about customers. */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Customer {
    private long id;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String address;
    private LocalDate dateOfBirth;
    private String email;
    private String hashedPassword;
    private Role role;
}
