package com.oop.web_project.logging;

import com.oop.web_project.annotations.Deposit;
import com.oop.web_project.annotations.Transfer;
import com.oop.web_project.annotations.Withdraw;
import com.oop.web_project.persistence.TransactionRepository;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;


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

    @Around("@annotation(deposit)")
    public Object createDepositTransaction(ProceedingJoinPoint pjp, Deposit deposit)
            throws Throwable {
        return pjp.proceed();
    }

    @Around("@annotation(withdraw)")
    public Object createWithdrawTransaction(ProceedingJoinPoint pjp, Withdraw withdraw)
            throws Throwable {
        return pjp.proceed();
    }

    @Around("@annotation(transfer)")
    public Object createTransferTransactions(ProceedingJoinPoint pjp,
                                               Transfer transfer)
            throws Throwable {
        return pjp.proceed();
    }
}
