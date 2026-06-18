package com.oop.web_project.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/* This class keeps information about all the transactions. */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "Transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "Transaction_id")
    private long id;

    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    @ManyToOne
    @JoinColumn(name = "Account_id", nullable = false)
    private Account account;

    @Column(name = "Transaction_time_stamp", nullable = false)
    private LocalDateTime timeStamp;

    @Column(name = "Transaction_amount", nullable = false)
    private BigDecimal amount;

    @ManyToOne
    @JoinColumn(name = "Currency_id", nullable = false)
    private Currency currency;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "Service_provider_id")
    private ServiceProvider serviceProvider;

    @OneToOne
    @JoinColumn(name = "Related_Transaction_id", unique = true)
    private Transaction relatedTransaction;

    @OneToOne(mappedBy = "relatedTransaction")
    private Transaction reverseTransaction;

    @Column(name = "Transaction_idempotency_key", unique = true)
    private String idempotencyKey;

    @Column(name = "Transaction_description")
    private String description;

    @Column(name = "Transaction_status")
    @Enumerated(EnumType.STRING)
    private TransactionStatus status;
}
