package com.oop.web_project.Entities;

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
public class Transaction {
    private long id;
    private TransactionType transactionType;
    private Account account;
    private LocalDateTime timeStamp;
    private BigDecimal amount;
    private Currency currency;
    private ServiceProvider serviceProvider;
    private Transaction relatedTransaction;
    private String idempotencyKey;
    private String description;
    private TransactionStatus status;
}
