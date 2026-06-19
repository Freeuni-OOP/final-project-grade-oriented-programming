package com.oop.web_project.mapping;

import com.oop.web_project.dto.responses.CardBalanceResponse;
import com.oop.web_project.dto.responses.CardResponse;
import com.oop.web_project.entities.Card;
import com.oop.web_project.entities.CardBalance;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CardApiMapper {

    private final CardBalanceApiMapper cardBalanceApiMapper;

    public CardApiMapper(CardBalanceApiMapper cardBalanceApiMapper){
        this.cardBalanceApiMapper = cardBalanceApiMapper;
    }

    public CardResponse toCardResponse(Card card){
        CardResponse response = new CardResponse();
        response.setType(card.getType());
        response.setBrand(card.getBrand());
        response.setSpendingLimit(card.getSpendingLimit());
        response.setExpirationDate(card.getExpirationDate());
        response.setPanMasked(card.getPanMasked());
        response.setPanToken(card.getPanToken());
        response.setActive(card.isActive());
        List<CardBalance> cardBalances = card.getBalances();
        List<CardBalanceResponse> cardBalanceResponses = new ArrayList<>();
        for(CardBalance balance : cardBalances){
            cardBalanceResponses.add(cardBalanceApiMapper.toCardBalanceResponse(balance));
        }
        return response;
    }
}
