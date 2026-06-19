package com.oop.web_project;

import com.oop.web_project.annotations.Deposit;
import com.oop.web_project.annotations.Exchange;
import com.oop.web_project.annotations.Transfer;
import com.oop.web_project.annotations.Withdraw;
import com.oop.web_project.entities.Transaction;
import com.oop.web_project.entities.TransactionStatus;
import com.oop.web_project.entities.TransactionType;
import com.oop.web_project.logging.TransactionLoggingAspect;
import com.oop.web_project.services.TransactionAuditService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionLoggingAspectTest {

    @Mock
    private TransactionAuditService transactionAuditService;

    @Mock
    private ProceedingJoinPoint pjp;

    @Mock
    private Deposit deposit;

    @Mock
    private Withdraw withdraw;

    @Mock
    private Transfer transfer;

    @Mock
    private Exchange exchange;

    @InjectMocks
    private TransactionLoggingAspect transactionLoggingAspect;

    private Transaction transaction;
    private static final long CARD_ID = 1L;
    private static final long SENDER_CARD_ID = 1L;
    private static final long RECEIVER_CARD_ID = 2L;
    private static final BigDecimal AMOUNT = BigDecimal.valueOf(100);
    private static final String CURRENCY = "USD";
    private static final String FROM_CURRENCY = "GEL";
    private static final String TO_CURRENCY = "USD";

    @BeforeEach
    void setUp() {
        transaction = new Transaction();
    }

    @Test
    void testCreateDepositTransactionSavesPending() throws Throwable {
        when(transactionAuditService.savePending(anyLong(), any(), anyString(), anyString(), any())).thenReturn(transaction);
        when(pjp.proceed()).thenReturn(null);

        transactionLoggingAspect.createDepositTransaction(pjp, CARD_ID, AMOUNT, CURRENCY, deposit);

        verify(transactionAuditService).savePending(
                CARD_ID, AMOUNT, CURRENCY,
                "Deposit of %s %s to card with id %s".formatted(AMOUNT, CURRENCY, CARD_ID),
                TransactionType.DEPOSIT);
    }

    @Test
    void testCreateDepositTransactionSetsComplete() throws Throwable {
        when(transactionAuditService.savePending(anyLong(), any(), anyString(), anyString(), any())).thenReturn(transaction);
        when(pjp.proceed()).thenReturn(null);

        transactionLoggingAspect.createDepositTransaction(pjp, CARD_ID, AMOUNT, CURRENCY, deposit);

        verify(transactionAuditService).updateStatus(transaction, TransactionStatus.COMPLETE);
    }

    @Test
    void testCreateDepositTransactionReturnsResult() throws Throwable {
        Object expected = new Object();
        when(transactionAuditService.savePending(anyLong(), any(), anyString(), anyString(), any())).thenReturn(transaction);
        when(pjp.proceed()).thenReturn(expected);

        Object result = transactionLoggingAspect.createDepositTransaction(pjp, CARD_ID, AMOUNT, CURRENCY, deposit);

        assertSame(expected, result);
    }

    @Test
    void testCreateDepositTransactionSetsFailed() throws Throwable {
        when(transactionAuditService.savePending(anyLong(), any(), anyString(), anyString(), any())).thenReturn(transaction);
        when(pjp.proceed()).thenThrow(new RuntimeException("deposit failed"));

        assertThrows(RuntimeException.class,
                () -> transactionLoggingAspect.createDepositTransaction(pjp, CARD_ID, AMOUNT, CURRENCY, deposit));

        verify(transactionAuditService).updateStatus(transaction, TransactionStatus.FAILED);
    }

    @Test
    void testCreateDepositTransactionRethrowsException() throws Throwable {
        RuntimeException ex = new RuntimeException("deposit failed");
        when(transactionAuditService.savePending(anyLong(), any(), anyString(), anyString(), any())).thenReturn(transaction);
        when(pjp.proceed()).thenThrow(ex);

        RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> transactionLoggingAspect.createDepositTransaction(pjp, CARD_ID, AMOUNT, CURRENCY, deposit));

        assertSame(ex, thrown);
    }

    @Test
    void testCreateWithdrawTransactionSavesPendingWithNegatedAmount() throws Throwable {
        when(transactionAuditService.savePending(anyLong(), any(), anyString(), anyString(), any())).thenReturn(transaction);
        when(pjp.proceed()).thenReturn(null);

        transactionLoggingAspect.createWithdrawTransaction(pjp, CARD_ID, AMOUNT, CURRENCY, withdraw);

        verify(transactionAuditService).savePending(
                CARD_ID, AMOUNT.negate(), CURRENCY,
                "Withdraw of %s %s from card with id %s".formatted(AMOUNT, CURRENCY, CARD_ID),
                TransactionType.WITHDRAWAL);
    }

    @Test
    void testCreateWithdrawTransactionSetsComplete() throws Throwable {
        when(transactionAuditService.savePending(anyLong(), any(), anyString(), anyString(), any())).thenReturn(transaction);
        when(pjp.proceed()).thenReturn(null);

        transactionLoggingAspect.createWithdrawTransaction(pjp, CARD_ID, AMOUNT, CURRENCY, withdraw);

        verify(transactionAuditService).updateStatus(transaction, TransactionStatus.COMPLETE);
    }

    @Test
    void testCreateWithdrawTransactionSetsFailed() throws Throwable {
        when(transactionAuditService.savePending(anyLong(), any(), anyString(), anyString(), any())).thenReturn(transaction);
        when(pjp.proceed()).thenThrow(new RuntimeException("withdraw failed"));

        assertThrows(RuntimeException.class,
                () -> transactionLoggingAspect.createWithdrawTransaction(pjp, CARD_ID, AMOUNT, CURRENCY, withdraw));

        verify(transactionAuditService).updateStatus(transaction, TransactionStatus.FAILED);
    }

    @Test
    void testCreateWithdrawTransactionRethrowsException() throws Throwable {
        RuntimeException ex = new RuntimeException("withdraw failed");
        when(transactionAuditService.savePending(anyLong(), any(), anyString(), anyString(), any())).thenReturn(transaction);
        when(pjp.proceed()).thenThrow(ex);

        RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> transactionLoggingAspect.createWithdrawTransaction(pjp, CARD_ID, AMOUNT, CURRENCY, withdraw));

        assertSame(ex, thrown);
    }

    @Test
    void testCreateTransferTransactionsSavesBothPending() throws Throwable {
        Transaction fromTransaction = new Transaction();
        Transaction toTransaction = new Transaction();

        when(transactionAuditService.savePending(eq(SENDER_CARD_ID), any(), anyString(), anyString(), any())).thenReturn(fromTransaction);
        when(transactionAuditService.savePending(eq(RECEIVER_CARD_ID), any(), anyString(), anyString(), any())).thenReturn(toTransaction);
        when(pjp.proceed()).thenReturn(null);

        transactionLoggingAspect.createTransferTransactions(pjp, SENDER_CARD_ID, RECEIVER_CARD_ID, AMOUNT, CURRENCY, transfer);

        verify(transactionAuditService).savePending(
                SENDER_CARD_ID, AMOUNT.negate(), CURRENCY,
                "transfer of %s %s from card with id %s to card with id %s".formatted(AMOUNT, CURRENCY, SENDER_CARD_ID, RECEIVER_CARD_ID),
                TransactionType.TRANSFER_OUT);
        verify(transactionAuditService).savePending(
                RECEIVER_CARD_ID, AMOUNT, CURRENCY,
                "transfer of %s %s to card with id %s from card with id %s".formatted(AMOUNT, CURRENCY, RECEIVER_CARD_ID, SENDER_CARD_ID),
                TransactionType.TRANSFER_IN);
    }

    @Test
    void testCreateTransferTransactionsSetsRelatedTransactions() throws Throwable {
        Transaction fromTransaction = Transaction.builder().build();
        Transaction toTransaction = Transaction.builder().build();

        when(transactionAuditService.savePending(eq(SENDER_CARD_ID), any(), anyString(), anyString(), any())).thenReturn(fromTransaction);
        when(transactionAuditService.savePending(eq(RECEIVER_CARD_ID), any(), anyString(), anyString(), any())).thenReturn(toTransaction);
        when(pjp.proceed()).thenReturn(null);

        transactionLoggingAspect.createTransferTransactions(pjp, SENDER_CARD_ID, RECEIVER_CARD_ID, AMOUNT, CURRENCY, transfer);

        assertSame(toTransaction, fromTransaction.getRelatedTransaction());
        assertSame(toTransaction, fromTransaction.getReverseTransaction());
        assertSame(fromTransaction, toTransaction.getRelatedTransaction());
        assertSame(fromTransaction, toTransaction.getReverseTransaction());
    }

    @Test
    void testCreateTransferTransactionsSetsCompleteOnBoth() throws Throwable {
        Transaction fromTransaction = new Transaction();
        Transaction toTransaction = new Transaction();

        when(transactionAuditService.savePending(eq(SENDER_CARD_ID), any(), anyString(), anyString(), any())).thenReturn(fromTransaction);
        when(transactionAuditService.savePending(eq(RECEIVER_CARD_ID), any(), anyString(), anyString(), any())).thenReturn(toTransaction);
        when(pjp.proceed()).thenReturn(null);

        transactionLoggingAspect.createTransferTransactions(pjp, SENDER_CARD_ID, RECEIVER_CARD_ID, AMOUNT, CURRENCY, transfer);

        verify(transactionAuditService).updateStatus(fromTransaction, TransactionStatus.COMPLETE);
        verify(transactionAuditService).updateStatus(toTransaction, TransactionStatus.COMPLETE);
    }

    @Test
    void testCreateTransferTransactionsSetsFailedOnBoth() throws Throwable {
        Transaction fromTransaction = new Transaction();
        Transaction toTransaction = new Transaction();

        when(transactionAuditService.savePending(eq(SENDER_CARD_ID), any(), anyString(), anyString(), any())).thenReturn(fromTransaction);
        when(transactionAuditService.savePending(eq(RECEIVER_CARD_ID), any(), anyString(), anyString(), any())).thenReturn(toTransaction);
        when(pjp.proceed()).thenThrow(new RuntimeException("transfer failed"));

        assertThrows(RuntimeException.class,
                () -> transactionLoggingAspect.createTransferTransactions(pjp, SENDER_CARD_ID, RECEIVER_CARD_ID, AMOUNT, CURRENCY, transfer));

        verify(transactionAuditService).updateStatus(fromTransaction, TransactionStatus.FAILED);
        verify(transactionAuditService).updateStatus(toTransaction, TransactionStatus.FAILED);
    }

    @Test
    void testCreateTransferTransactionsRethrowsException() throws Throwable {
        RuntimeException ex = new RuntimeException("transfer failed");
        Transaction fromTransaction = new Transaction();
        Transaction toTransaction = new Transaction();

        when(transactionAuditService.savePending(eq(SENDER_CARD_ID), any(), anyString(), anyString(), any())).thenReturn(fromTransaction);
        when(transactionAuditService.savePending(eq(RECEIVER_CARD_ID), any(), anyString(), anyString(), any())).thenReturn(toTransaction);
        when(pjp.proceed()).thenThrow(ex);

        RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> transactionLoggingAspect.createTransferTransactions(pjp, SENDER_CARD_ID, RECEIVER_CARD_ID, AMOUNT, CURRENCY, transfer));

        assertSame(ex, thrown);
    }

    @Test
    void testCreateCurrencyChangeTransactionsSavesPending() throws Throwable {
        when(transactionAuditService.savePending(anyLong(), any(), anyString(), anyString(), any())).thenReturn(transaction);
        when(pjp.proceed()).thenReturn(null);

        transactionLoggingAspect.createCurrencyChangeTransactions(pjp, CARD_ID, AMOUNT, FROM_CURRENCY, TO_CURRENCY, exchange);

        verify(transactionAuditService).savePending(
                CARD_ID, AMOUNT, FROM_CURRENCY,
                "Exchanging %s from %s to %s".formatted(AMOUNT, FROM_CURRENCY, TO_CURRENCY),
                TransactionType.CURRENCY_EXCHANGE);
    }

    @Test
    void testCreateCurrencyChangeTransactionsSetsComplete() throws Throwable {
        when(transactionAuditService.savePending(anyLong(), any(), anyString(), anyString(), any())).thenReturn(transaction);
        when(pjp.proceed()).thenReturn(null);

        transactionLoggingAspect.createCurrencyChangeTransactions(pjp, CARD_ID, AMOUNT, FROM_CURRENCY, TO_CURRENCY, exchange);

        verify(transactionAuditService).updateStatus(transaction, TransactionStatus.COMPLETE);
    }

    @Test
    void testCreateCurrencyChangeTransactionsSetsFailedOnException() throws Throwable {
        when(transactionAuditService.savePending(anyLong(), any(), anyString(), anyString(), any())).thenReturn(transaction);
        when(pjp.proceed()).thenThrow(new RuntimeException("exchange failed"));

        assertThrows(RuntimeException.class,
                () -> transactionLoggingAspect.createCurrencyChangeTransactions(pjp, CARD_ID, AMOUNT, FROM_CURRENCY, TO_CURRENCY, exchange));

        verify(transactionAuditService).updateStatus(transaction, TransactionStatus.FAILED);
    }

    @Test
    void testCreateCurrencyChangeTransactionsRethrowsException() throws Throwable {
        RuntimeException ex = new RuntimeException("exchange failed");
        when(transactionAuditService.savePending(anyLong(), any(), anyString(), anyString(), any())).thenReturn(transaction);
        when(pjp.proceed()).thenThrow(ex);

        RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> transactionLoggingAspect.createCurrencyChangeTransactions(pjp, CARD_ID, AMOUNT, FROM_CURRENCY, TO_CURRENCY, exchange));

        assertSame(ex, thrown);
    }
}