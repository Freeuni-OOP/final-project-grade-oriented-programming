package com.oop.web_project.services;


import com.oop.web_project.entities.Account;
import com.oop.web_project.entities.Card;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CardServiceImpl implements CardService {


    @Override
    public void activateCard(Card card) {

    }

    @Override
    public void deactivateCard(Card card) {

    }

    @Override
    public void deleteCard(long cardId) {

    }

    @Override
    public void depositMoney(Card card, int amountToAdd) {

    }

    @Override
    public void withdrawMoney(Card card, int amountToWithdraw) {

    }

    @Override
    public void transferMoney(Card senderCard, Card receiverCard, int amount) {

    }

    @Override
    public void changeCurrency(Card card, String desiredCurrency) {

    }

    @Override
    public List<Card> getAllCardsForAccount(long accountId) {
        return List.of();
    }
}
