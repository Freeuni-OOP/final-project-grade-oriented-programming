package com.oop.web_project.mapping;

import com.oop.web_project.dto.requests.AccountCreationRequest;
import com.oop.web_project.dto.responses.*;
import com.oop.web_project.entities.Account;
import com.oop.web_project.entities.Card;
import com.oop.web_project.entities.Customer;
import com.oop.web_project.entities.Transaction;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class AccountApiMapper {

    private final TransactionApiMapper transactionApiMapper;
    private final CardApiMapper cardApiMapper;
    private final CustomerSummaryApiMapper customerSummaryApiMapper;

    public AccountApiMapper(TransactionApiMapper transactionApiMapper, CardApiMapper cardApiMapper,
                            CustomerSummaryApiMapper customerSummaryApiMapper){
        this.transactionApiMapper = transactionApiMapper;
        this.cardApiMapper = cardApiMapper;
        this.customerSummaryApiMapper = customerSummaryApiMapper;
    }

    public AccountProfileResponse toProfileResponse(Account account){
        AccountProfileResponse response = new AccountProfileResponse();
        response.setName(account.getName());
        response.setCategory(account.getCategory());
        response.setDateOpened(account.getDateOpened());
        response.setActive(account.isActive());
        Set<Transaction> transactions = account.getTransactions();
        List<TransactionResponse> transactionResponses = new ArrayList<>();
        for(Transaction transaction : transactions){
            transactionResponses.add(transactionApiMapper.toTransactionResponse(transaction));
        }
        response.setTransactions(transactionResponses);
        Set<Card> cards = account.getCards();
        List<CardSummaryResponse> cardSummaryResponses = new ArrayList<>();
        for(Card card : cards){
            cardSummaryResponses.add(cardApiMapper.toCardSummaryResponse(card));
        }
        response.setCards(cardSummaryResponses);
        Set<Customer> customers = account.getCustomers();
        List<CustomerSummaryResponse> customerResponses = new ArrayList<>();
        for(Customer customer : customers){
            customerResponses.add(customerSummaryApiMapper.toSummaryResponse(customer));
        }
        response.setCustomers(customerResponses);
        return response;
    }



    public Account toAccount(AccountCreationRequest request) {
        return new Account(
                null,
                request.getAccountName(),
                request.getCategory(),
                LocalDate.now(),
                true,
                null,
                null,
                null
        );
    }
}
