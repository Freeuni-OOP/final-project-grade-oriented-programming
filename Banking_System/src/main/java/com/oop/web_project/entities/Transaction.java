package com.oop.web_project.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/* This class keeps information about all the transactions. */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Builder
@Table(name = "Transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "Transaction_id")
    private Long id;

    @Column(name = "Transaction_type")
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

    @OneToOne(cascade=CascadeType.ALL)
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
