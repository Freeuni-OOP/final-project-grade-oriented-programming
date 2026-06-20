package com.oop.web_project.mapping;

import com.oop.web_project.dto.responses.CardBalanceResponse;
import com.oop.web_project.entities.CardBalance;
import org.springframework.stereotype.Component;

@Component
public class CardBalanceApiMapper {

    public CardBalanceResponse toCardBalanceResponse(CardBalance cardBalance){
        CardBalanceResponse response = new CardBalanceResponse();
        response.setAmount(cardBalance.getAmount());
        response.setCurrencyCode(cardBalance.getCurrency().getCode());
        return response;
    }
}
