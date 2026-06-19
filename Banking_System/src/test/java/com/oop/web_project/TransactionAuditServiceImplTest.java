package com.oop.web_project;

import com.oop.web_project.entities.*;
import com.oop.web_project.exceptions.cardExceptions.CardNotFoundException;
import com.oop.web_project.exceptions.transactionExceptions.CurrencyNotFoundException;
import com.oop.web_project.persistence.CardRepository;
import com.oop.web_project.persistence.CurrencyRepository;
import com.oop.web_project.persistence.TransactionRepository;
import com.oop.web_project.services.TransactionAuditServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;



@ExtendWith(MockitoExtension.class)
class TransactionAuditServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private CurrencyRepository currencyRepository;

    @InjectMocks
    private TransactionAuditServiceImpl transactionAuditService;

    private Card card;
    private Account account;
    private Currency currency;
    private BigDecimal amount;
    private String description;

    @BeforeEach
    void setUp() {
        account = new Account();
        card = new Card();
        card.setAccount(account);
        currency = new Currency();
        amount = BigDecimal.valueOf(500);
        description = "Deposit of 500 USD";
    }

    @Test
    void testSavePendingReturnsSavedTransaction() {
        Transaction saved = Transaction.builder()
                .transactionType(TransactionType.DEPOSIT)
                .account(account)
                .amount(amount)
                .currency(currency)
                .description(description)
                .status(TransactionStatus.PENDING)
                .build();

        when(cardRepository.findById(3L)).thenReturn(Optional.of(card));
        when(currencyRepository.findCurrencyByCode("USD")).thenReturn(Optional.of(currency));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(saved);

        Transaction result = transactionAuditService.savePending(3L, amount, "USD", description);

        assertNotNull(result);
        assertEquals(TransactionStatus.PENDING, result.getStatus());
        assertEquals(TransactionType.DEPOSIT, result.getTransactionType());
        assertEquals(account, result.getAccount());
        assertEquals(amount, result.getAmount());
        assertEquals(currency, result.getCurrency());
        assertEquals(description, result.getDescription());
    }

    @Test
    void testSavePendingCallsRepositorySave() {
        when(cardRepository.findById(3L)).thenReturn(Optional.of(card));
        when(currencyRepository.findCurrencyByCode("USD")).thenReturn(Optional.of(currency));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(new Transaction());

        transactionAuditService.savePending(3L, amount, "USD", description);

        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void testSavePendingSetsTimestamp() {
        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);

        when(cardRepository.findById(3L)).thenReturn(Optional.of(card));
        when(currencyRepository.findCurrencyByCode("USD")).thenReturn(Optional.of(currency));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(new Transaction());

        transactionAuditService.savePending(3L, amount, "USD", description);

        verify(transactionRepository).save(captor.capture());
        assertNotNull(captor.getValue().getTimeStamp());
    }

    @Test
    void testSavePendingCardNotFoundThrowsException() {
        when(cardRepository.findById(3L)).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class,
                () -> transactionAuditService.savePending(3L, amount, "USD", description));

        verifyNoInteractions(transactionRepository);
    }

    @Test
    void testSavePendingCurrencyNotFoundThrowsException() {
        when(cardRepository.findById(3L)).thenReturn(Optional.of(card));
        when(currencyRepository.findCurrencyByCode("USD")).thenReturn(Optional.empty());

        assertThrows(CurrencyNotFoundException.class,
                () -> transactionAuditService.savePending(3L, amount, "USD", description));

        verifyNoInteractions(transactionRepository);
    }

    @Test
    void testUpdateStatusSetsStatusOnTransaction() {
        Transaction transaction = Transaction.builder()
                .status(TransactionStatus.PENDING)
                .build();

        transactionAuditService.updateStatus(transaction, TransactionStatus.COMPLETE);

        assertEquals(TransactionStatus.COMPLETE, transaction.getStatus());
    }

    @Test
    void testUpdateStatusCallsRepositorySave() {
        Transaction transaction = Transaction.builder()
                .status(TransactionStatus.PENDING)
                .build();

        transactionAuditService.updateStatus(transaction, TransactionStatus.FAILED);

        verify(transactionRepository, times(1)).save(transaction);
    }

    @Test
    void testUpdateStatusSavesCorrectTransaction() {
        Transaction transaction = Transaction.builder()
                .status(TransactionStatus.PENDING)
                .build();

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);

        transactionAuditService.updateStatus(transaction, TransactionStatus.COMPLETE);

        verify(transactionRepository).save(captor.capture());
        assertEquals(TransactionStatus.COMPLETE, captor.getValue().getStatus());
        assertSame(transaction, captor.getValue());
    }
}