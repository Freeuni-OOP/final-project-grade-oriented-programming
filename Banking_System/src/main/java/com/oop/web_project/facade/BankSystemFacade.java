package com.oop.web_project.facade;


import com.oop.web_project.entities.*;
import com.oop.web_project.exceptions.cardExceptions.CardNotFoundException;
import com.oop.web_project.exceptions.cardExceptions.InsufficientMoneyOnCardException;
import com.oop.web_project.services.AccountService;
import com.oop.web_project.services.CardService;
import com.oop.web_project.services.CustomerService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
public class BankSystemFacade implements CommandLineRunner {

    private final AccountService accountService;
    private final CardService cardService;
    private final CustomerService customerService;

    public BankSystemFacade(AccountService accountService, CardService cardService, CustomerService customerService) {
        this.accountService = accountService;
        this.cardService = cardService;
        this.customerService = customerService;
    }

    private void testCustomerService() {
        Customer customer = customerService.getCustomerByEmail("alice.johnson@example.com");

        System.out.println("ooo id: " + customer.getId());


        customerService.deactivateCustomer(customer.getId());

        customerService.deleteCustomer(customer.getId());


        Customer newCustomer = new Customer(null, "giga", "beradze", "577665544", "kitskhi",
                LocalDate.now(),"gggg3333@gmail.com", "mdsnsadanad",
                true, null, null);


        customerService.registerCustomer(
                newCustomer
        );

        List<Customer> customers = customerService.getCustomersByAccount(9L);

        customers.forEach(c -> System.out.println("customer id: " + c.getId()));
    }


    private void testAccountService() {
        Account account = new Account();
        account.setName("new account");
        account.setCategory(AccountCategory.CREDIT);
        account.setDateOpened(LocalDate.now());
        account.setActive(true);
        accountService.createAccount(account);

        Account account1 = accountService.selectAccountById(1L);

        accountService.deactivateAccount(account1.getId());

        accountService.registerCustomerToAccount(3L, 4L);

        accountService.updateAccount(3L, "new great account name");

        List<Account> accounts = accountService.selectAccountsByCustomerId(4L);

        accounts.forEach(a -> System.out.println("account name: " + a.getName()));

        accountService.deleteAccount(3L);

        BigDecimal val = accountService.getAccountBalanceByCurrency(1, "GEL");

        System.out.println("total is: " + val);
    }

    private void testCardService() {
        cardService.deactivateCard(1L);

        CardBrand cardBrand = new CardBrand(null, "MASTERCARD", null);

        Account account = accountService.selectAccountById(1L);


        Card card = new Card(null, CardType.DEBIT, cardBrand, account, BigDecimal.valueOf(30000),
                LocalDate.now(), "sdfdsfs", "dsfnsdjdsnfds", true, null);

        cardService.createCard(card);

        List<Card> cardList = cardService.getAllCardsForAccount(1L);

        cardList.forEach(c -> System.out.println("card: " + c.getBrand().getName()));

        cardService.depositMoney(1, BigDecimal.valueOf(300), "GEL");

        cardService.changeCurrency(1L, BigDecimal.valueOf(400), "GEL", "USD");

        cardService.withdrawMoney(1, BigDecimal.valueOf(500), "GEL");

        cardService.transferMoney(6, 11, BigDecimal.valueOf(400), "GEL");

//        cardService.deleteCard(1L)

        try {
            cardService.depositMoney(1L, BigDecimal.valueOf(1000000000), "GEL");
        }catch(InsufficientMoneyOnCardException e) {
            e.printStackTrace();
        }
        System.out.println(cardService.checkCardExpiration(2L));
    }

    @Override
    public void run(String... args) throws Exception {
        testAccountService();
        testCustomerService();
        testCardService();
    }
}
