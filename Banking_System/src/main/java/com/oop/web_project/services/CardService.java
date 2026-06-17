package com.oop.web_project.services;

import com.oop.web_project.entities.Account;
import com.oop.web_project.entities.Card;

import java.util.List;

/**
 * Service interface defining operations for managing cards,
 * including activation, deposits, withdrawals, and transfers.
 */
public interface CardService {

    /**
     * Activates the given card.
     */
    void activateCard(Card card);

    /**
     * Deactivates the given card.
     */
    void deactivateCard(Card card);

    /**
     * Deposits the specified amount into the account linked to the given card.
     */
    void depositMoney(Card card, int amountToAdd);

    /**
     * Withdraws the specified amount from the account linked to the given card.
     */
    void withdrawMoney(Card card, int amountToWithdraw);

    /**
     * Transfers the specified amount from the sender's card to the receiver's card.
     */
    void transferMoney(Card senderCard, Card receiverCard, int amount);

    /**
     * Converts the card's current currency into the desired currency.
     */
    void changeCurrency(Card card, String desiredCurrency);

    /**
     * Retrieves all cards associated with the given account.
     */
    List<Card> getAllCardsForAccount(Account account);

}