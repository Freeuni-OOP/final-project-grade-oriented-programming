package com.oop.web_project.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;
import java.util.List;

/* This class keeps information about accounts. Each customer may have several accounts. Several customers can also have a shared account. */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "Accounts")
public class Account {

    @Column(name = "Account_id")
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "Account_name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "Account_category", nullable = false)
    private AccountCategory category;

    @Column(name = "Date_opened", nullable = false)
    private LocalDate dateOpened;

    @Column(name = "Is_active")
    private boolean isActive;

    @OneToMany(mappedBy = "account")
    private List<Transaction> transactions;

    @OneToMany(mappedBy = "account")
    private List<Card> cards;

    @ManyToMany
    @JoinTable(name = "Account_customer", joinColumns = @JoinColumn(name = "Account_id"), inverseJoinColumns = @JoinColumn(name = "Customer_id"))
    private List<Customer> customers;
}
