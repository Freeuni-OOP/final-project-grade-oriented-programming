package com.oop.web_project.services;

import com.oop.web_project.Entities.Card;

public interface CardService {
    /*
     * Activates card.
     */
    public void activateCard(Card card);

    /*
     * Deactivates card.
     */
    public void deactivateCard(Card card);

    /*
     * This method adds money to ones account. if the addition is
     * correct, then the deposit is successful, so the method returns true.
     * if it wasn't successful, false is returned.
     */
    public void depositMoney(Card card, int amountToAdd);

    /*
     * This method withdraws money from ones account. if the withdrawal is
     * completed, then the action was successful, so the method returns true.
     * if it wasn't successful, false is returned.
     */
    public void withdrawMoney(Card card, int amountToWithdraw);

    /*
     * This method transfers money from one user to another user. If the
     * Transaction was successful, then the program returns true, else false.
     */
    public void transferMoney(Card senderCard, Card receiverCard, int amount);

    /*
     * This method takes the card, and exchanges the current currency
     * into the desired one.
     */
    public void changeCurrency(Card card, String desiredCurrency);
}
