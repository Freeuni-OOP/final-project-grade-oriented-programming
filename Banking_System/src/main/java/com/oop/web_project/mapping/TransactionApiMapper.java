package com.oop.web_project.mapping;

import com.oop.web_project.dto.responses.TransactionResponse;
import com.oop.web_project.entities.Transaction;
import org.springframework.stereotype.Component;

@Component
public class TransactionApiMapper {

    public TransactionResponse toTransactionResponse(Transaction transaction){
        TransactionResponse response = new TransactionResponse();
        response.setTransactionType(transaction.getTransactionType());
        response.setTimeStamp(transaction.getTimeStamp());
        response.setAmount(transaction.getAmount());
        response.setDescription(transaction.getDescription());
        response.setStatus(transaction.getStatus());
        response.setCurrencyCode(transaction.getCurrency().getCode());
        return response;
    }
}
