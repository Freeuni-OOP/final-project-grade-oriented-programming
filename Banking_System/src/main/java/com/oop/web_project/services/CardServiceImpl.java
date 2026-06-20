package com.oop.web_project.services;
import com.oop.web_project.annotations.Deposit;
import com.oop.web_project.annotations.Exchange;
import com.oop.web_project.annotations.Transfer;
import com.oop.web_project.annotations.Withdraw;
import com.oop.web_project.entities.*;
import com.oop.web_project.exceptions.cardExceptions.*;
import com.oop.web_project.exceptions.transactionExceptions.CurrencyExchangeException;
import com.oop.web_project.persistence.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
public class CardServiceImpl implements CardService {


    public record FetchedBalances(CardBalance from, CardBalance to) {}

    private final CardRepository cardRepository;
    private final CardBalanceRepository cardBalanceRepository;
    private final CurrencyExchangeRepository currencyExchangeRepository;
    private final CurrencyRepository currencyRepository;

    public CardServiceImpl(CardRepository cardRepository,
                           CardBalanceRepository cardBalanceRepository,
                           CurrencyExchangeRepository currencyExchangeRepository,
                           CurrencyRepository currencyRepository) {
        this.cardRepository = cardRepository;
        this.cardBalanceRepository = cardBalanceRepository;
        this.currencyExchangeRepository = currencyExchangeRepository;
        this.currencyRepository = currencyRepository;
    }

    @Override
    @Transactional
    public void activateCard(long cardId) {
        Card card = cardRepository.findWithLockById(cardId).orElseThrow(
                () -> new CardNotFoundException("card cannot be found!"));
        if(card.isActive()) {
            throw new CardAlreadyActiveException("card is already active!");
        }
        card.setActive(true);
    }

    @Override
    @Transactional
    public void deactivateCard(long cardId) {
        Card card = cardRepository.findWithLockById(cardId).orElseThrow(
                () -> new CardNotFoundException("card cannot be found!"));
        if(!card.isActive()) {
            throw new CardAlreadyDeactivatedException("card is already inactive!");
        }
        card.setActive(false);
    }

    @Override
    @Transactional
    public void createCard(Card card) {
        cardRepository.save(card);
    }

    @Override
    public Card selectCardById(long cardId) {
        return cardRepository.findById(cardId)
                .orElseThrow(
                        () -> new CardNotFoundException("Could could not be found!")
                );
    }

    @Override
    @Transactional
    public void addCurrencyToCard(long cardId, String currencyCode) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(
                        () -> new CardNotFoundException("card could not be found!")
                );

        Currency currency = currencyRepository.findCurrencyByCode(currencyCode)
                .orElseThrow(
                        () -> new InvalidCurrencyException("Such currency does not exist!")
                );

        if (cardBalanceRepository.existsByCardIdAndCurrencyCode(cardId, currencyCode)) {
            throw new DuplicateCurrencyException("Card already has a balance for this currency!");
        }

        CardBalance cardBalance = new CardBalance(
                null,
                BigDecimal.ZERO,
                card,
                currency
        );
        try {
            cardBalanceRepository.saveAndFlush(cardBalance);
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateCurrencyException("Card already has a balance for this currency!");
        }
    }

    @Override
    public List<CardBalance> selectCardBalances(long cardId) {
        return cardBalanceRepository.findAllByCardId(cardId);
    }

    @Override
    @Transactional
    public void deleteCard(long cardId) {
        cardRepository.deleteById(cardId);
    }

    @Override
    @Deposit
    @Transactional
    public void depositMoney(long cardId, BigDecimal amountToAdd, String currencyCode) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(
                        () -> new CardNotFoundException("card could not be found!")
                );

        CardBalance balance = cardBalanceRepository.findByCardIdAndCurrencyCode(cardId, currencyCode)
                .orElseThrow(
                        () -> new CardBalanceNotFoundException("Balance could not be found!")
                );

        BigDecimal totalBalance = balance.getAmount().add(amountToAdd);

        if(totalBalance.compareTo(card.getSpendingLimit()) > 0) {
            throw new InsufficientMoneyOnCardException("spending limit exceeded!");
        }

        balance.setAmount(totalBalance);
    }

    @Override
    @Withdraw
    @Transactional
    public void withdrawMoney(long cardId, BigDecimal amountToWithdraw, String currencyCode) {
        CardBalance balance = cardBalanceRepository.findByCardIdAndCurrencyCode(cardId, currencyCode)
                .orElseThrow(
                        () -> new CardBalanceNotFoundException("Balance could not be found!")
                );

        BigDecimal finalBalance = balance.getAmount().subtract(amountToWithdraw);

        if(finalBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new InsufficientMoneyOnCardException("Insufficient funds!");
        }

        balance.setAmount(finalBalance);
    }

    @Override
    @Transfer
    @Transactional
    public void transferMoney(long senderCardId, long receiverCardId, BigDecimal amount, String currencyCode) {
        if(senderCardId == receiverCardId){
            throw new SameCardTransferException("Tried to transfer to the same card!");
        }
        Card receiverCard = cardRepository.findById(receiverCardId)
                .orElseThrow(
                        () -> new CardNotFoundException("Card could not be found!")
                );

        FetchedBalances fetchedBalances = safeFetchBalances(senderCardId, receiverCardId,
                currencyCode, currencyCode);

        BigDecimal finalBalanceFrom = fetchedBalances.from().getAmount().subtract(amount);

        if(finalBalanceFrom.compareTo(BigDecimal.ZERO) < 0) {
            throw new InsufficientMoneyOnCardException("Insufficient funds!");
        }

        BigDecimal finalBalanceTo = fetchedBalances.to().getAmount().add(amount);

        if(finalBalanceTo.compareTo(receiverCard.getSpendingLimit()) > 0) {
            throw new InsufficientMoneyOnCardException("spending limit exceeded!");
        }

        fetchedBalances.from().setAmount(finalBalanceFrom);
        fetchedBalances.to().setAmount(finalBalanceTo);
    }

    @Override
    @Exchange
    @Transactional
    public void changeCurrency(long cardId, BigDecimal amount, String fromCurrencyCode, String toCurrencyCode) {
        if(fromCurrencyCode.equals(toCurrencyCode)){
            throw new DuplicateCurrencyException("Tried to exchange the same currency");
        }
        FetchedBalances fetchedBalances =
                safeFetchBalances(cardId, cardId, fromCurrencyCode, toCurrencyCode);

        BigDecimal finalBalanceFrom = fetchedBalances.from().getAmount().subtract(amount);

        if(finalBalanceFrom.compareTo(BigDecimal.ZERO) < 0) {
            throw new InsufficientMoneyOnCardException("Insufficient funds!");
        }

        CurrencyExchange currencyRate = currencyExchangeRepository.
                findByCurrencyCodes(fromCurrencyCode, toCurrencyCode)
                .orElseThrow(
                        () -> new CurrencyExchangeException("Could not find currency exchange!")
                );

        BigDecimal finalBalanceTo = fetchedBalances.to().getAmount().add(exchangeCurrency(amount, currencyRate.getRate()));

        fetchedBalances.from().setAmount(finalBalanceFrom);
        fetchedBalances.to().setAmount(finalBalanceTo);
    }

    @Override
    public List<Card> getAllCardsForAccount(long accountId) {
        return cardRepository.getAllByAccountId(accountId);
    }

    @Override
    public boolean checkCardExpiration(long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(
                        () -> new CardNotFoundException("card could not be found!")
                );
        return LocalDate.now().isAfter(card.getExpirationDate());
    }

    /**
     * Method exchanges given money into new currency
     * @param amount amount of money in first currency
     * @param rate how much one unit of money of first currency is worth in other currency
     * @return exchanged amount
     */
    private BigDecimal exchangeCurrency(BigDecimal amount, BigDecimal rate) {
        BigDecimal result =  amount.multiply(rate);
        return result.setScale(2, RoundingMode.HALF_EVEN);
    }

    /**
     * Method resolves dining philosopher's problem and fetches balances of cards in particular currencies
     * @param fromCardId id of the card from which money will be transferred
     * @param toCardId id of the card to which money will be transferred
     * @param fromCurrencyCode code of the currency for the card we are transferring money from
     * @param toCurrencyCode code of the currency for the card we are transferring money to
     * @return record of fetched balances(balance from first card, balance from second card)
     */
    private FetchedBalances safeFetchBalances(long fromCardId, long toCardId, String fromCurrencyCode,
                                                        String toCurrencyCode) {
        CardBalance balanceFrom;
        CardBalance balanceTo;

        boolean lockFromFirst;

        if (fromCardId != toCardId) {
            lockFromFirst = fromCardId < toCardId;
        } else {
            lockFromFirst = fromCurrencyCode.compareTo(toCurrencyCode) < 0;
        }

        if (lockFromFirst) {
            balanceFrom = cardBalanceRepository.findByCardIdAndCurrencyCode(fromCardId, fromCurrencyCode)
                    .orElseThrow(() -> new CardBalanceNotFoundException("Source card balance could not be found!"));
            balanceTo = cardBalanceRepository.findByCardIdAndCurrencyCode(toCardId, toCurrencyCode)
                    .orElseThrow(() -> new CardBalanceNotFoundException("Target card balance could not be found!"));
        } else {
            balanceTo = cardBalanceRepository.findByCardIdAndCurrencyCode(toCardId, toCurrencyCode)
                    .orElseThrow(() -> new CardBalanceNotFoundException("Target card balance could not be found!"));
            balanceFrom = cardBalanceRepository.findByCardIdAndCurrencyCode(fromCardId, fromCurrencyCode)
                    .orElseThrow(() -> new CardBalanceNotFoundException("Source card balance could not be found!"));
        }
        return new FetchedBalances(balanceFrom, balanceTo);
    }

}
