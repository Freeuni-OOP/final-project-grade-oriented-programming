package com.oop.web_project.services;

import com.oop.web_project.entities.Transaction;
import com.oop.web_project.entities.TransactionStatus;
import com.oop.web_project.entities.TransactionType;

import java.math.BigDecimal;

public interface TransactionAuditService {
    /**
     * Saves the pending transaction into the database
     * @param cardId id of the card that takes part in transaction
     * @param amount amount that is being deposited, withdrew or transferred
     * @param currencyCode currency code of currency in which transaction is happening
     * @param description description of the transaction
     * @return instance of the transaction we have saved
     */
    Transaction savePending(long cardId, BigDecimal amount, String currencyCode, String description,
                            TransactionType transactionType);

    /**
     * Updates the status of the transaction
     * @param transaction transaction whose status we are updating
     * @param status the status transaction's new status should become
     */
    void updateStatus(Transaction transaction, TransactionStatus status);
}
