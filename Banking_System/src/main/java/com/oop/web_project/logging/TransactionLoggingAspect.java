package com.oop.web_project.logging;

import com.oop.web_project.annotations.Deposit;
import com.oop.web_project.annotations.Transfer;
import com.oop.web_project.annotations.Withdraw;
import com.oop.web_project.persistence.TransactionRepository;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.math.BigDecimal;


/**
 * Class utilizes Aspect oriented programming paradigm to create appropriate transactions
 * and save them in database. Approach is to intercept appropriate annotations.
 */
@Aspect
@Component
public class TransactionLoggingAspect {

    private final TransactionRepository transactionRepository;

    public TransactionLoggingAspect(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Around(value = "@annotation(deposit) && args(cardId, amountToAdd, currencyCode)",
            argNames = "pjp,cardId,amountToAdd,currencyCode,deposit")
    public Object createDepositTransaction(ProceedingJoinPoint pjp, long cardId, BigDecimal amountToAdd,
                                           String currencyCode, Deposit deposit)
            throws Throwable {
        System.out.println("amount to deposit start");
        System.out.println(cardId);
        System.out.println(amountToAdd);
        System.out.println(currencyCode);
        System.out.println("amount to deposit end");
        Object obj = pjp.proceed();

        return obj;
    }

    @Around(value="@annotation(withdraw) && args(cardId, amountToWithdraw, currencyCode)",
            argNames = "pjp,cardId,amountToWithdraw,currencyCode,withdraw")
    public Object createWithdrawTransaction(ProceedingJoinPoint pjp,
                                            long cardId, BigDecimal amountToWithdraw, String currencyCode,
                                            Withdraw withdraw)
            throws Throwable {
        System.out.println("amount to withdraw start");
        System.out.println(cardId);
        System.out.println(amountToWithdraw);
        System.out.println(currencyCode);
        System.out.println("amount to withdraw end");
        return pjp.proceed();
    }

    @Around(value="@annotation(transfer) && args(senderCardId, receiverCardId, amount, currencyCode)",
            argNames = "pjp,senderCardId,receiverCardId,amount,currencyCode,transfer")
    public Object createTransferTransactions(ProceedingJoinPoint pjp,
                                             long senderCardId, long receiverCardId,
                                             BigDecimal amount, String currencyCode,
                                               Transfer transfer)
            throws Throwable {

        System.out.println("amount to transfer start");
        System.out.println(senderCardId);
        System.out.println(receiverCardId);
        System.out.println(amount);
        System.out.println(currencyCode);
        System.out.println("amount to transfer end");

        return pjp.proceed();
    }
}
