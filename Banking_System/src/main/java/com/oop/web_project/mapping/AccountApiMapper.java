package com.oop.web_project.mapping;

import com.oop.web_project.dto.responses.*;
import com.oop.web_project.entities.Account;
import com.oop.web_project.entities.Card;
import com.oop.web_project.entities.Customer;
import com.oop.web_project.entities.Transaction;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AccountApiMapper {

    private final TransactionApiMapper transactionApiMapper;
    private final CardApiMapper cardApiMapper;
    private final CustomerApiMapper customerApiMapper;

    public AccountApiMapper(TransactionApiMapper transactionApiMapper, CardApiMapper cardApiMapper, CustomerApiMapper customerApiMapper){
        this.transactionApiMapper = transactionApiMapper;
        this.cardApiMapper = cardApiMapper;
        this.customerApiMapper = customerApiMapper;
    }

    public AccountProfileResponse toProfileResponse(Account account){
        AccountProfileResponse response = new AccountProfileResponse();
        response.setName(account.getName());
        response.setCategory(account.getCategory());
        response.setDateOpened(account.getDateOpened());
        response.setActive(account.isActive());
        List<Transaction> transactions = account.getTransactions();
        List<TransactionResponse> transactionResponses = new ArrayList<>();
        for(Transaction transaction : transactions){
            transactionResponses.add(transactionApiMapper.toTransactionResponse(transaction));
        }
        response.setTransactions(transactionResponses);
        List<Card> cards = account.getCards();
        List<CardResponse> cardResponses = new ArrayList<>();
        for(Card card : cards){
            cardResponses.add(cardApiMapper.toCardResponse(card));
        }
        response.setCards(cardResponses);
        List<Customer> customers = account.getCustomers();
        List<CustomerSummaryResponse> customerResponses = new ArrayList<>();
        for(Customer customer : customers){
            customerResponses.add(customerApiMapper.toSummaryResponse(customer));
        }
        response.setCustomers(customerResponses);
        return response;
    }

    public AccountSummaryResponse toAccountSummaryResponse(Account account){
        AccountSummaryResponse response = new AccountSummaryResponse();
        response.setName(account.getName());
        response.setCategory(account.getCategory());
        response.setDateOpened(account.getDateOpened());
        response.setActive(account.isActive());
        return response;
    }

}
