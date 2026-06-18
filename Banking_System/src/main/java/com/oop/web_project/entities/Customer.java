package com.oop.web_project.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;


/* This class keeps information about customers. */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "Customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "Customer_id")
    private long id;

    @Column(name = "First_name", nullable = false)
    private String firstName;

    @Column(name = "Last_name", nullable = false)
    private String lastName;

    @Column(name = "Phone_number", unique = true)
    private String phoneNumber;

    @Column(name = "Address")
    private String address;

    @Column(name = "Date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Column(name = "Email", nullable = false, unique = true)
    private String email;

    @Column(name = "Hashed_password", nullable = false, unique = true)
    private String hashedPassword;

    @Column(name = "Is_active", nullable = false)
    private boolean isActive;

    @ManyToOne()
    @JoinColumn(name = "Role_id")
    private Role role;

    @ManyToMany(mappedBy = "customers")
    private List<Account> accounts;
}
