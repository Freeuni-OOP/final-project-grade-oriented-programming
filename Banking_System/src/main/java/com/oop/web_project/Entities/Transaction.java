package com.oop.web_project.Entities;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
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
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Transient
    private TransactionType transactionType;
    @ManyToOne
    private Account account;
    private LocalDateTime timeStamp;
    private BigDecimal amount;
    @Transient
    private Currency currency;
    @Transient
    private ServiceProvider serviceProvider;
    @ManyToOne
    @JoinColumn(name = "related_transaction_id")
    private Transaction relatedTransaction;
    private String idempotencyKey;
    private String description;
    @Enumerated(EnumType.STRING)
    private TransactionStatus status;
}
