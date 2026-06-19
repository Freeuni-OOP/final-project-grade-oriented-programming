package com.oop.web_project.logging;

import com.oop.web_project.annotations.Deposit;
import com.oop.web_project.annotations.Transfer;
import com.oop.web_project.annotations.Withdraw;
import com.oop.web_project.entities.*;
import com.oop.web_project.exceptions.cardExceptions.CardNotFoundException;
import com.oop.web_project.exceptions.transactionExceptions.CurrencyExchangeException;
import com.oop.web_project.persistence.CardRepository;
import com.oop.web_project.persistence.CurrencyRepository;
import com.oop.web_project.persistence.TransactionRepository;
import com.oop.web_project.services.TransactionAuditService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;


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

        Object obj = pjp.proceed();

        return obj;
    }

    @Around(value="@annotation(withdraw) && args(cardId, amountToWithdraw, currencyCode)",
            argNames = "pjp,cardId,amountToWithdraw,currencyCode,withdraw")
    public Object createWithdrawTransaction(ProceedingJoinPoint pjp,
                                            long cardId, BigDecimal amountToWithdraw, String currencyCode,
                                            Withdraw withdraw)
            throws Throwable {

        Object obj = pjp.proceed();

        return obj;
    }

    @Around(value="@annotation(transfer) && args(senderCardId, receiverCardId, amount, currencyCode)",
            argNames = "pjp,senderCardId,receiverCardId,amount,currencyCode,transfer")
    public Object createTransferTransactions(ProceedingJoinPoint pjp,
                                             long senderCardId, long receiverCardId,
                                             BigDecimal amount, String currencyCode,
                                               Transfer transfer)
            throws Throwable {

        Object obj = pjp.proceed();


        return obj;
    }
}
