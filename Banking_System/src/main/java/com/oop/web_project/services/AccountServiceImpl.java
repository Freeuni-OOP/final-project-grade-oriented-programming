package com.oop.web_project.services;
import com.oop.web_project.entities.Account;
import com.oop.web_project.entities.Customer;
import com.oop.web_project.persistence.AccountRepository;
import com.oop.web_project.persistence.CardRepository;
import com.oop.web_project.persistence.CustomerRepository;
import com.oop.web_project.persistence.TransactionRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final CardRepository cardRepository;
    private final TransactionRepository transactionRepository;

    public AccountServiceImpl(AccountRepository accountRepository,
                              CustomerRepository customerRepository,
                              CardRepository cardRepository,
                              TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
        this.cardRepository = cardRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    @Transactional
    public void createAccount(Account account) {
        accountRepository.save(account);
    }

    @Override
    @Transactional
    public void activateAccount(long accountId) {
        Account account = accountRepository.findById(accountId).orElseThrow(
                () -> new IllegalArgumentException("account cannot be null!"));
        if(account.isActive()) {
            throw new IllegalArgumentException("account is already active!");
        }
        account.setActive(true);
    }

    @Override
    @Transactional
    public void deactivateAccount(long accountId) {
        Account account = accountRepository.findById(accountId).orElseThrow(
                () -> new IllegalArgumentException("account cannot be null!"));
        if(!account.isActive()) {
            throw new IllegalArgumentException("account is already inactive!");
        }
        account.setActive(false);
    }

    @Override
    @Transactional
    public void deleteAccount(long accountId) {
        Account account = accountRepository.findById(accountId).orElseThrow(
                () -> new IllegalArgumentException("such account does not exist!")
        );
        cardRepository.deleteAll(account.getCards());
        transactionRepository.deleteAll(account.getTransactions());
        accountRepository.delete(account);
    }

    @Override
    public Account selectAccountById(long accountId) {
        return accountRepository.findById(accountId).orElseThrow(
                () -> new IllegalArgumentException("Could not find account!")
        );
    }

    @Override
    public List<Account> selectAccountsByCustomerEmail(String customerEmail) {
        return accountRepository.findAllByCustomersEmail(customerEmail);
    }

    @Override
    public List<Account> selectAccountsByCustomerId(long customerId) {
        return accountRepository.findAllByCustomersId(customerId);
    }

    @Override
    @Transactional
    public void updateAccount(long accountId, String accountName) {
        Account existingAccount = accountRepository.findById(accountId).orElseThrow(
                () -> new IllegalArgumentException("such account does not exist!")
        );
        if(accountName != null)existingAccount.setName(accountName);
    }

    @Override
    @Transactional
    public void registerCustomerToAccount(long accountId, long customerId) {
        Account account = accountRepository.findById(accountId).orElseThrow(
                () -> new IllegalArgumentException("such account does not exist!")
        );
        Customer customer = customerRepository.findById(customerId).orElseThrow(
                () -> new IllegalArgumentException("such customer does not exist!")
        );
        account.getCustomers().add(customer);
        customer.getAccounts().add(account);
    }

    @Override
    @Transactional
    public BigDecimal getAccountBalanceByCurrency(long accountId, String currencyCode) {
        return cardRepository.getBalanceForAccount(accountId, currencyCode).orElseThrow(
                () -> new IllegalArgumentException("Could not determine balance of the account!")
        );
    }

}
