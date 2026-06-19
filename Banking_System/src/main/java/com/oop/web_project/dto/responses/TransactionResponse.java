package com.oop.web_project.dto.responses;

import com.oop.web_project.entities.TransactionStatus;
import com.oop.web_project.entities.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransactionResponse {

    private TransactionType transactionType;
    private LocalDateTime timeStamp;
    private BigDecimal amount;
    private String description;
    private TransactionStatus status;
    private String currencyCode;
}
