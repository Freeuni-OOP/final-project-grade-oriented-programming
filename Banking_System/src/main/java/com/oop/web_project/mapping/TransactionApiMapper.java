package com.oop.web_project.mapping;

import com.oop.web_project.dto.responses.TransactionResponse;
import com.oop.web_project.entities.Transaction;
import org.springframework.stereotype.Component;

@Component
public class TransactionApiMapper {

    public TransactionResponse toTransactionResponse(Transaction transaction){
        return null;
    }
}
