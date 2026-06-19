package com.oop.web_project.services;

import com.oop.web_project.entities.*;
import com.oop.web_project.exceptions.cardExceptions.CardNotFoundException;
import com.oop.web_project.exceptions.transactionExceptions.CurrencyNotFoundException;
import com.oop.web_project.persistence.CardRepository;
import com.oop.web_project.persistence.CurrencyRepository;
import com.oop.web_project.persistence.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class TransactionAuditServiceImpl implements TransactionAuditService{
    private final TransactionRepository transactionRepository;
    private final CardRepository cardRepository;
    private final CurrencyRepository currencyRepository;

    public TransactionAuditServiceImpl(TransactionRepository transactionRepository,
                                       CardRepository cardRepository,
                                       CurrencyRepository currencyRepository) {
        this.transactionRepository = transactionRepository;
        this.cardRepository = cardRepository;
        this.currencyRepository = currencyRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public Transaction savePending(long cardId, BigDecimal amount, String currencyCode, String description,
                                   TransactionType transactionType) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(
                        () -> new CardNotFoundException("Card could not be found!")
                );

        Account account = card.getAccount();

        Currency currency = currencyRepository.findCurrencyByCode(currencyCode)
                .orElseThrow(
                        () -> new CurrencyNotFoundException("Currency could not be found")
                );

        Transaction transaction = Transaction.builder()
                .transactionType(transactionType)
                .account(account)
                .amount(amount)
                .currency(currency)
                .description(description)
                .timeStamp(LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS))
                .status(TransactionStatus.PENDING)
                .build();
        return transactionRepository.save(transaction);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void updateStatus(Transaction transaction, TransactionStatus status) {
        transaction.setStatus(status);
        transactionRepository.save(transaction);
    }
}
