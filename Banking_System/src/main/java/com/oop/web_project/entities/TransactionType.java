package com.oop.web_project.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;


/* This class keeps information about different transaction types. */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "Transaction_types")
public class TransactionType {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "Transaction_type_id")
    private long id;

    @Column(name = "Transaction_type_name", nullable = false, unique = true)
    private String name;

    @OneToMany(mappedBy = "transactionType")
    private List<Transaction> transactions;
}
