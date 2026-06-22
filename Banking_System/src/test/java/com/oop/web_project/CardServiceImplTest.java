package com.oop.web_project;

import com.oop.web_project.entities.*;
import com.oop.web_project.exceptions.accountExceptions.AccountNotFoundException;
import com.oop.web_project.exceptions.cardExceptions.*;
import com.oop.web_project.exceptions.transactionExceptions.CurrencyExchangeException;
import com.oop.web_project.persistence.*;
import com.oop.web_project.services.CardServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceImplTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private CurrencyRepository currencyRepository;

    @Mock
    private CardBalanceRepository cardBalanceRepository;

    @Mock
    private CurrencyExchangeRepository currencyExchangeRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private CardServiceImpl cardService;

    private Card card;
    private Card receiverCard;

    @BeforeEach
    void setUp() {
        card = new Card();
        card.setId(1L);
        card.setActive(false);
        card.setSpendingLimit(new BigDecimal("10000.00"));

        receiverCard = new Card();
        receiverCard.setId(2L);
        receiverCard.setActive(true);
        receiverCard.setSpendingLimit(new BigDecimal("10000.00"));
    }

    @Test
    void testSelectCardByIdNotFoundThrowsException() {
        when(cardRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(CardNotFoundException.class, () -> cardService.selectCardById(1L));
    }

    @Test
    void testSelectCardByIdFoundReturnsCard() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        Card result = cardService.selectCardById(1L);
        assertEquals(card, result);
    }

    @Test
    void testAddCurrencyToCardCardNotFoundThrowsException() {
        when(cardRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(CardNotFoundException.class, () -> cardService.addCurrencyToCard(1L, "USD"));
    }

    @Test
    void testAddCurrencyToCardCurrencyNotFoundThrowsException() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(currencyRepository.findCurrencyByCode("USD")).thenReturn(Optional.empty());
        assertThrows(InvalidCurrencyException.class, () -> cardService.addCurrencyToCard(1L, "USD"));
    }

    @Test
    void testAddCurrencyToCardAlreadyExistsThrowsException() {
        Currency currency = new Currency();
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(currencyRepository.findCurrencyByCode("USD")).thenReturn(Optional.of(currency));
        when(cardBalanceRepository.existsByCardIdAndCurrencyCode(1L, "USD")).thenReturn(true);
        assertThrows(DuplicateCurrencyException.class, () -> cardService.addCurrencyToCard(1L, "USD"));
    }

    @Test
    void testAddCurrencyToCardDataIntegrityViolationThrowsDuplicateCurrencyException() {
        Currency currency = new Currency();
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(currencyRepository.findCurrencyByCode("USD")).thenReturn(Optional.of(currency));
        when(cardBalanceRepository.existsByCardIdAndCurrencyCode(1L, "USD")).thenReturn(false);
        when(cardBalanceRepository.saveAndFlush(any(CardBalance.class))).thenThrow(new DataIntegrityViolationException("Duplicate currency balance"));
        assertThrows(DuplicateCurrencyException.class, () -> cardService.addCurrencyToCard(1L, "USD"));
    }

    @Test
    void testAddCurrencyToCardValidSavesCardBalance() {
        Currency currency = new Currency();
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(currencyRepository.findCurrencyByCode("USD")).thenReturn(Optional.of(currency));
        when(cardBalanceRepository.existsByCardIdAndCurrencyCode(1L, "USD")).thenReturn(false);
        cardService.addCurrencyToCard(1L, "USD");
        verify(cardBalanceRepository, times(1)).saveAndFlush(any(CardBalance.class));
    }

    @Test
    void testSelectCardBalancesReturnsBalances() {
        List<CardBalance> balances = List.of(new CardBalance());
        when(cardBalanceRepository.findAllByCardId(1L)).thenReturn(balances);
        List<CardBalance> result = cardService.selectCardBalances(1L);
        assertEquals(balances, result);
    }

    @Test
    void testActivateCardNotFoundThrowsException() {
        when(cardRepository.findWithLockById(1L)).thenReturn(Optional.empty());
        assertThrows(CardNotFoundException.class, () -> cardService.activateCard(1L));
    }

    @Test
    void testActivateCardAlreadyActiveThrowsException() {
        card.setActive(true);
        when(cardRepository.findWithLockById(1L)).thenReturn(Optional.of(card));
        assertThrows(CardAlreadyActiveException.class, () -> cardService.activateCard(1L));
    }

    @Test
    void testActivateCardInactiveActivatesCard() {
        when(cardRepository.findWithLockById(1L)).thenReturn(Optional.of(card));
        cardService.activateCard(1L);
        assertTrue(card.isActive());
    }

    @Test
    void testDeactivateCardNotFoundThrowsException() {
        when(cardRepository.findWithLockById(1L)).thenReturn(Optional.empty());
        assertThrows(CardNotFoundException.class, () -> cardService.deactivateCard(1L));
    }

    @Test
    void testDeactivateCardAlreadyInactiveThrowsException() {
        when(cardRepository.findWithLockById(1L)).thenReturn(Optional.of(card));
        assertThrows(CardAlreadyDeactivatedException.class, () -> cardService.deactivateCard(1L));
    }

    @Test
    void testDeactivateCardActiveDeactivatesCard() {
        card.setActive(true);
        when(cardRepository.findWithLockById(1L)).thenReturn(Optional.of(card));
        cardService.deactivateCard(1L);
        assertFalse(card.isActive());
    }

    @Test
    void testCreateCardSavesCard() {

        Account account = new Account();
        account.setId(1L);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        cardService.createCard(1L, card);
        verify(cardRepository, times(1)).save(card);
    }

    @Test
    void testCreateCardAccountNotFoundThrowsException() {
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(AccountNotFoundException.class, () -> cardService.createCard(1L, card));
    }

    @Test
    void testCreateCardDuplicatePanTokenThrowsException() {
        Account account = new Account();
        account.setId(1L);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(cardRepository.existsByPanToken(card.getPanToken())).thenReturn(true);
        assertThrows(CardAlreadyExistsException.class, () -> cardService.createCard(1L, card));
    }

    @Test
    void testCreateCardDuplicatePanMaskedThrowsException() {
        Account account = new Account();
        account.setId(1L);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(cardRepository.existsByPanToken(card.getPanToken())).thenReturn(false);
        when(cardRepository.existsByPanMasked(card.getPanMasked())).thenReturn(true);
        assertThrows(CardAlreadyExistsException.class, () -> cardService.createCard(1L, card));
    }

    @Test
    void testDeleteCardDeletesById() {
        cardService.deleteCard(1L);
        verify(cardRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDepositMoneyCardNotFoundThrowsException() {
        when(cardRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(CardNotFoundException.class, () -> cardService.depositMoney(1L, BigDecimal.TEN, "USD"));
    }

    @Test
    void testDepositMoneyBalanceNotFoundThrowsException() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(cardBalanceRepository.findByCardIdAndCurrencyCode(1L, "USD")).thenReturn(Optional.empty());
        assertThrows(CardBalanceNotFoundException.class, () -> cardService.depositMoney(1L, BigDecimal.TEN, "USD"));
    }

    @Test
    void testDepositMoneySpendingLimitExceededThrowsException() {
        card.setSpendingLimit(new BigDecimal("100.00"));
        CardBalance balance = new CardBalance();
        balance.setAmount(new BigDecimal("90.00"));
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(cardBalanceRepository.findByCardIdAndCurrencyCode(1L, "USD")).thenReturn(Optional.of(balance));
        assertThrows(InsufficientMoneyOnCardException.class, () -> cardService.depositMoney(1L, new BigDecimal("20.00"), "USD"));
    }

    @Test
    void testDepositMoneyValidAddsAmount() {
        CardBalance balance = new CardBalance();
        balance.setAmount(new BigDecimal("100.00"));
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(cardBalanceRepository.findByCardIdAndCurrencyCode(1L, "USD")).thenReturn(Optional.of(balance));
        cardService.depositMoney(1L, new BigDecimal("50.00"), "USD");
        assertEquals(new BigDecimal("150.00"), balance.getAmount());
    }

    @Test
    void testWithdrawMoneyBalanceNotFoundThrowsException() {
        when(cardBalanceRepository.findByCardIdAndCurrencyCode(1L, "USD")).thenReturn(Optional.empty());
        assertThrows(CardBalanceNotFoundException.class, () -> cardService.withdrawMoney(1L, BigDecimal.TEN, "USD"));
    }

    @Test
    void testWithdrawMoneyInsufficientFundsThrowsException() {
        CardBalance balance = new CardBalance();
        balance.setAmount(new BigDecimal("10.00"));
        when(cardBalanceRepository.findByCardIdAndCurrencyCode(1L, "USD")).thenReturn(Optional.of(balance));
        assertThrows(InsufficientMoneyOnCardException.class, () -> cardService.withdrawMoney(1L, new BigDecimal("20.00"), "USD"));
    }

    @Test
    void testWithdrawMoneySufficientFundsWithdraws() {
        CardBalance balance = new CardBalance();
        balance.setAmount(new BigDecimal("100.00"));
        when(cardBalanceRepository.findByCardIdAndCurrencyCode(1L, "USD")).thenReturn(Optional.of(balance));
        cardService.withdrawMoney(1L, new BigDecimal("40.00"), "USD");
        assertEquals(new BigDecimal("60.00"), balance.getAmount());
    }

    @Test
    void testTransferMoneyReceiverCardNotFoundThrowsException() {
        when(cardRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(CardNotFoundException.class, () -> cardService.transferMoney(1L, 2L, BigDecimal.TEN, "USD"));
    }

    @Test
    void testTransferMoneySenderLessThanReceiverSourceBalanceNotFoundThrowsException() {
        when(cardRepository.findById(2L)).thenReturn(Optional.of(receiverCard));
        when(cardBalanceRepository.findByCardIdAndCurrencyCode(1L, "USD")).thenReturn(Optional.empty());
        assertThrows(CardBalanceNotFoundException.class, () -> cardService.transferMoney(1L, 2L, BigDecimal.TEN, "USD"));
    }

    @Test
    void testTransferMoneySenderLessThanReceiverTargetBalanceNotFoundThrowsException() {
        CardBalance from = new CardBalance();
        from.setAmount(new BigDecimal("100.00"));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(receiverCard));
        when(cardBalanceRepository.findByCardIdAndCurrencyCode(1L, "USD")).thenReturn(Optional.of(from));
        when(cardBalanceRepository.findByCardIdAndCurrencyCode(2L, "USD")).thenReturn(Optional.empty());
        assertThrows(CardBalanceNotFoundException.class, () -> cardService.transferMoney(1L, 2L, BigDecimal.TEN, "USD"));
    }

    @Test
    void testTransferMoneySenderGreaterThanReceiverTargetBalanceNotFoundThrowsException() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(cardBalanceRepository.findByCardIdAndCurrencyCode(1L, "USD")).thenReturn(Optional.empty());
        assertThrows(CardBalanceNotFoundException.class, () -> cardService.transferMoney(2L, 1L, BigDecimal.TEN, "USD"));
    }

    @Test
    void testTransferMoneySenderGreaterThanReceiverSourceBalanceNotFoundThrowsException() {
        CardBalance to = new CardBalance();
        to.setAmount(new BigDecimal("100.00"));
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(cardBalanceRepository.findByCardIdAndCurrencyCode(1L, "USD")).thenReturn(Optional.of(to));
        when(cardBalanceRepository.findByCardIdAndCurrencyCode(2L, "USD")).thenReturn(Optional.empty());
        assertThrows(CardBalanceNotFoundException.class, () -> cardService.transferMoney(2L, 1L, BigDecimal.TEN, "USD"));
    }

    @Test
    void testTransferMoneyInsufficientFundsThrowsException() {
        CardBalance from = new CardBalance();
        from.setAmount(new BigDecimal("5.00"));
        CardBalance to = new CardBalance();
        to.setAmount(new BigDecimal("0.00"));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(receiverCard));
        when(cardBalanceRepository.findByCardIdAndCurrencyCode(1L, "USD")).thenReturn(Optional.of(from));
        when(cardBalanceRepository.findByCardIdAndCurrencyCode(2L, "USD")).thenReturn(Optional.of(to));
        assertThrows(InsufficientMoneyOnCardException.class, () -> cardService.transferMoney(1L, 2L, new BigDecimal("10.00"), "USD"));
    }

    @Test
    void testTransferMoneyReceiverSpendingLimitExceededThrowsException() {
        receiverCard.setSpendingLimit(new BigDecimal("40.00"));
        CardBalance from = new CardBalance();
        from.setAmount(new BigDecimal("100.00"));
        CardBalance to = new CardBalance();
        to.setAmount(new BigDecimal("20.00"));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(receiverCard));
        when(cardBalanceRepository.findByCardIdAndCurrencyCode(1L, "USD")).thenReturn(Optional.of(from));
        when(cardBalanceRepository.findByCardIdAndCurrencyCode(2L, "USD")).thenReturn(Optional.of(to));
        assertThrows(InsufficientMoneyOnCardException.class, () -> cardService.transferMoney(1L, 2L, new BigDecimal("30.00"), "USD"));
    }

    @Test
    void testTransferMoneySufficientFundsTransfersAmount() {
        CardBalance from = new CardBalance();
        from.setAmount(new BigDecimal("100.00"));
        CardBalance to = new CardBalance();
        to.setAmount(new BigDecimal("20.00"));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(receiverCard));
        when(cardBalanceRepository.findByCardIdAndCurrencyCode(1L, "USD")).thenReturn(Optional.of(from));
        when(cardBalanceRepository.findByCardIdAndCurrencyCode(2L, "USD")).thenReturn(Optional.of(to));
        cardService.transferMoney(1L, 2L, new BigDecimal("30.00"), "USD");
        assertEquals(new BigDecimal("70.00"), from.getAmount());
        assertEquals(new BigDecimal("50.00"), to.getAmount());
    }

    @Test
    void testTransferMoneySameCardThrowsException() {
        assertThrows(SameCardTransferException.class, () -> cardService.transferMoney(1L, 1L, BigDecimal.TEN, "USD"));
    }

    @Test
    void testChangeCurrencyFromCurrencyLessThanToCurrencySourceBalanceNotFoundThrowsException() {
        when(cardBalanceRepository.findByCardIdAndCurrencyCode(1L, "EUR")).thenReturn(Optional.empty());
        assertThrows(CardBalanceNotFoundException.class, () -> cardService.changeCurrency(1L, BigDecimal.TEN, "EUR", "USD"));
    }

    @Test
    void testChangeCurrencyFromCurrencyGreaterThanToCurrencyTargetBalanceNotFoundThrowsException() {
        when(cardBalanceRepository.findByCardIdAndCurrencyCode(1L, "EUR")).thenReturn(Optional.empty());
        assertThrows(CardBalanceNotFoundException.class, () -> cardService.changeCurrency(1L, BigDecimal.TEN, "USD", "EUR"));
    }

    @Test
    void testChangeCurrencyInsufficientFundsThrowsException() {
        CardBalance from = new CardBalance();
        from.setAmount(new BigDecimal("5.00"));
        CardBalance to = new CardBalance();
        to.setAmount(new BigDecimal("0.00"));
        when(cardBalanceRepository.findByCardIdAndCurrencyCode(1L, "EUR")).thenReturn(Optional.of(from));
        when(cardBalanceRepository.findByCardIdAndCurrencyCode(1L, "USD")).thenReturn(Optional.of(to));
        assertThrows(InsufficientMoneyOnCardException.class, () -> cardService.changeCurrency(1L, new BigDecimal("10.00"), "EUR", "USD"));
    }

    @Test
    void testChangeCurrencyExchangeRateNotFoundThrowsException() {
        CardBalance from = new CardBalance();
        from.setAmount(new BigDecimal("100.00"));
        CardBalance to = new CardBalance();
        to.setAmount(new BigDecimal("0.00"));
        when(cardBalanceRepository.findByCardIdAndCurrencyCode(1L, "EUR")).thenReturn(Optional.of(from));
        when(cardBalanceRepository.findByCardIdAndCurrencyCode(1L, "USD")).thenReturn(Optional.of(to));
        when(currencyExchangeRepository.findByCurrencyCodes("EUR", "USD")).thenReturn(Optional.empty());
        assertThrows(CurrencyExchangeException.class, () -> cardService.changeCurrency(1L, new BigDecimal("10.00"), "EUR", "USD"));
    }

    @Test
    void testChangeCurrencySuccessfulChangesCurrency() {
        CardBalance from = new CardBalance();
        from.setAmount(new BigDecimal("100.00"));
        CardBalance to = new CardBalance();
        to.setAmount(new BigDecimal("0.00"));
        CurrencyExchange exchange = new CurrencyExchange();
        exchange.setRate(new BigDecimal("1.15"));
        when(cardBalanceRepository.findByCardIdAndCurrencyCode(1L, "EUR")).thenReturn(Optional.of(from));
        when(cardBalanceRepository.findByCardIdAndCurrencyCode(1L, "USD")).thenReturn(Optional.of(to));
        when(currencyExchangeRepository.findByCurrencyCodes("EUR", "USD")).thenReturn(Optional.of(exchange));
        cardService.changeCurrency(1L, new BigDecimal("10.00"), "EUR", "USD");
        assertEquals(new BigDecimal("90.00"), from.getAmount());
        assertEquals(new BigDecimal("11.50"), to.getAmount());
    }

    @Test
    void testGetAllCardsForAccountReturnsCards() {
        List<Card> cards = List.of(card);
        when(cardRepository.getAllByAccountId(1L)).thenReturn(cards);
        List<Card> result = cardService.getAllCardsForAccount(1L);
        assertEquals(cards, result);
    }

    @Test
    void testCheckCardExpirationCardNotFoundThrowsException() {
        when(cardRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(CardNotFoundException.class, () -> cardService.checkCardExpiration(1L));
    }

    @Test
    void testCheckCardExpirationExpiredReturnsTrue() {
        card.setExpirationDate(LocalDate.now().minusDays(1));
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        assertTrue(cardService.checkCardExpiration(1L));
    }

    @Test
    void testCheckCardExpirationNotExpiredReturnsFalse() {
        card.setExpirationDate(LocalDate.now().plusDays(1));
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        assertFalse(cardService.checkCardExpiration(1L));
    }
}