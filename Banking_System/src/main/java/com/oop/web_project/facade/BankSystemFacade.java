package com.oop.web_project.facade;


import com.oop.web_project.entities.Account;
import com.oop.web_project.entities.AccountCategory;
import com.oop.web_project.entities.Customer;
import com.oop.web_project.services.AccountService;
import com.oop.web_project.services.CardService;
import com.oop.web_project.services.CustomerService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

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
    }

    private void testCardService() {

    }

    @Override
    public void run(String... args) throws Exception {
        testAccountService();
        testCustomerService();
        testCardService();
    }
}
