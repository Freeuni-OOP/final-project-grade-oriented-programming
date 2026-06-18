package com.oop.web_project.services;

import com.oop.web_project.entities.Account;
import com.oop.web_project.entities.Card;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service interface defining operations for managing cards,
 * including activation, deposits, withdrawals, and transfers.
 */
public interface CardService {

    /**
     * Activates the given card.
     */
    void activateCard(long cardId);

    /**
     * Deactivates the given card.
     */
    void deactivateCard(long cardId);

    /**
     * Creates new card
     */
    void createCard(Card card);

    /**
     * Looks for card with cardId in database and deletes it
     */
    void deleteCard(long cardId);

    /**
     * Deposits the specified amount into the account linked to the given card.
     */
    void depositMoney(long cardId, BigDecimal amountToAdd, String currencyCode);

    /**
     * Withdraws the specified amount from the account linked to the given card.
     */
    void withdrawMoney(long cardId, BigDecimal amountToWithdraw, String currencyCode);

    /**
     * Transfers the specified amount from the sender's card to the receiver's card.
     */
    void transferMoney(long senderCardId, long receiverCardId, BigDecimal amount, String currencyCode);

    /**
     * Converts some amount of money on card from one currency to the other
     */
    void changeCurrency(long cardId, BigDecimal amount, String fromCurrencyCode, String toCurrencyCode);

    /**
     * Retrieves all cards associated with the given account.
     */
    List<Card> getAllCardsForAccount(long accountId);

}