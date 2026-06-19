package com.oop.web_project.logging;

import com.oop.web_project.annotations.Deposit;
import com.oop.web_project.annotations.Exchange;
import com.oop.web_project.annotations.Transfer;
import com.oop.web_project.annotations.Withdraw;
import com.oop.web_project.entities.*;
import com.oop.web_project.services.TransactionAuditService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;


/**
 * Class utilizes Aspect oriented programming paradigm to create appropriate transactions
 * and save them in database. Approach is to intercept appropriate annotations.
 */
@Aspect
@Component
public class TransactionLoggingAspect {

    private final TransactionAuditService transactionAuditService;

    public TransactionLoggingAspect(TransactionAuditService transactionAuditService) {
        this.transactionAuditService = transactionAuditService;
    }

    @Around(value = "@annotation(deposit) && args(cardId, amountToAdd, currencyCode)",
            argNames = "pjp,cardId,amountToAdd,currencyCode,deposit")
    public Object createDepositTransaction(ProceedingJoinPoint pjp, long cardId, BigDecimal amountToAdd,
                                           String currencyCode, Deposit deposit)
            throws Throwable {

        Transaction transaction =
                transactionAuditService.savePending(cardId, amountToAdd, currencyCode,
                        "Deposit of %s %s to card with id %s".formatted(amountToAdd, currencyCode, cardId)
                        ,TransactionType.DEPOSIT);

        Object obj;
        try {
            obj = pjp.proceed();
        } catch (Throwable t) {
            transactionAuditService.updateStatus(transaction, TransactionStatus.FAILED);
            throw t;
        }

        transactionAuditService.updateStatus(transaction, TransactionStatus.COMPLETE);
        return obj;
    }

    @Around(value="@annotation(withdraw) && args(cardId, amountToWithdraw, currencyCode)",
            argNames = "pjp,cardId,amountToWithdraw,currencyCode,withdraw")
    public Object createWithdrawTransaction(ProceedingJoinPoint pjp,
                                            long cardId, BigDecimal amountToWithdraw, String currencyCode,
                                            Withdraw withdraw)
            throws Throwable {

        Transaction transaction =
                transactionAuditService.savePending(cardId, amountToWithdraw.negate(), currencyCode,
                        "Withdraw of %s %s from card with id %s".formatted(amountToWithdraw, currencyCode, cardId),
                        TransactionType.WITHDRAWAL);

        Object obj;

        try {
            obj = pjp.proceed();
        } catch (Throwable t) {
            transactionAuditService.updateStatus(transaction, TransactionStatus.FAILED);
            throw t;
        }

        transactionAuditService.updateStatus(transaction, TransactionStatus.COMPLETE);
        return obj;
    }

    @Around(value="@annotation(transfer) && args(senderCardId, receiverCardId, amount, currencyCode)",
            argNames = "pjp,senderCardId,receiverCardId,amount,currencyCode,transfer")
    public Object createTransferTransactions(ProceedingJoinPoint pjp,
                                             long senderCardId, long receiverCardId,
                                             BigDecimal amount, String currencyCode,
                                               Transfer transfer)
            throws Throwable {

        Transaction fromTransaction = transactionAuditService.savePending(senderCardId, amount.negate(), currencyCode,
                "transfer of %s %s from card with id %s to card with id %s"
                        .formatted(amount, currencyCode, senderCardId, receiverCardId),
                TransactionType.TRANSFER_OUT);

        Transaction toTransaction = transactionAuditService.savePending(receiverCardId, amount, currencyCode,
                "transfer of %s %s to card with id %s from card with id %s".
                        formatted(amount, currencyCode, receiverCardId, senderCardId),
                TransactionType.TRANSFER_IN);

        fromTransaction.setReverseTransaction(toTransaction);
        fromTransaction.setRelatedTransaction(toTransaction);

        toTransaction.setRelatedTransaction(fromTransaction);
        toTransaction.setReverseTransaction(fromTransaction);

        Object obj;

        try {
            obj = pjp.proceed();
        } catch (Throwable t) {
            transactionAuditService.updateStatus(fromTransaction, TransactionStatus.FAILED);
            transactionAuditService.updateStatus(toTransaction, TransactionStatus.FAILED);
            throw t;
        }

        transactionAuditService.updateStatus(fromTransaction, TransactionStatus.COMPLETE);
        transactionAuditService.updateStatus(toTransaction, TransactionStatus.COMPLETE);
        return obj;
    }

    @Around(value="@annotation(exchange) && args(cardId, amount, fromCurrencyCode, toCurrencyCode)",
            argNames = "pjp,cardId,amount,fromCurrencyCode,toCurrencyCode,exchange")
    public Object createCurrencyChangeTransactions(ProceedingJoinPoint pjp, long cardId,
                                                   BigDecimal amount,
                                                   String fromCurrencyCode, String toCurrencyCode,
                                                   Exchange exchange)
    throws Throwable {

        Transaction transaction = transactionAuditService.savePending(
                cardId,
                amount,
                fromCurrencyCode,
                "Exchanging %s from %s to %s".formatted(amount,fromCurrencyCode, toCurrencyCode),
                TransactionType.CURRENCY_EXCHANGE
        );

        Object obj;

        try {
            obj = pjp.proceed();
        } catch (Throwable t) {
            transactionAuditService.updateStatus(transaction, TransactionStatus.FAILED);
            throw t;
        }

        transactionAuditService.updateStatus(transaction, TransactionStatus.COMPLETE);
        return obj;
    }
}
