package com.oop.web_project;

import com.oop.web_project.entities.Account;
import com.oop.web_project.entities.Customer;
import com.oop.web_project.exceptions.accountExceptions.AccountAlreadyActiveException;
import com.oop.web_project.exceptions.accountExceptions.AccountAlreadyDeactivatedException;
import com.oop.web_project.exceptions.accountExceptions.AccountNotFoundException;
import com.oop.web_project.exceptions.cardExceptions.CardBalanceNotFoundException;
import com.oop.web_project.exceptions.customerExceptions.CustomerNotFoundException;
import com.oop.web_project.persistence.AccountRepository;
import com.oop.web_project.persistence.CardRepository;
import com.oop.web_project.persistence.CustomerRepository;
import com.oop.web_project.persistence.TransactionRepository;
import com.oop.web_project.services.AccountServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CardRepository cardRepository;

    @InjectMocks
    private AccountServiceImpl accountService;

    private Account account;

    @BeforeEach
    void setUp() {
        account = new Account();
        account.setId(1L);
        account.setName("Main Account");
        account.setActive(false);
        account.setCustomers(new ArrayList<>());
    }

    @Test
    void testCreateAccountSavesAccount() {
        accountService.createAccount(account);
        verify(accountRepository, times(1)).save(account);
    }

    @Test
    void testActivateAccountNotFoundThrowsException() {
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(AccountNotFoundException.class, () -> accountService.activateAccount(1L));
    }

    @Test
    void testActivateAccountAlreadyActiveThrowsException() {
        account.setActive(true);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        assertThrows(AccountAlreadyActiveException.class, () -> accountService.activateAccount(1L));
    }

    @Test
    void testActivateAccountInactiveActivatesAccount() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        accountService.activateAccount(1L);
        assertTrue(account.isActive());
    }

    @Test
    void testDeactivateAccountNotFoundThrowsException() {
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(AccountNotFoundException.class, () -> accountService.deactivateAccount(1L));
    }

    @Test
    void testDeactivateAccountAlreadyInactiveThrowsException() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        assertThrows(AccountAlreadyDeactivatedException.class, () -> accountService.deactivateAccount(1L));
    }

    @Test
    void testDeactivateAccountActiveDeactivatesAccount() {
        account.setActive(true);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        accountService.deactivateAccount(1L);
        assertFalse(account.isActive());
    }

    @Test
    void testDeleteAccountNotFoundThrowsException() {
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(AccountNotFoundException.class, () -> accountService.deleteAccount(1L));
    }

    @Test
    void testDeleteAccountFoundDeletesAccount() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        account.setCards(new ArrayList<>());
        account.setTransactions(new ArrayList<>());

        accountService.deleteAccount(1L);
        verify(accountRepository, times(1)).delete(account);
    }

    @Test
    void testSelectAccountByIdFound() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        Account result = accountService.selectAccountById(1L);
        assertEquals(account, result);
    }

    @Test
    void testSelectAccountByIdNotFoundThrowsException() {
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(AccountNotFoundException.class, () -> accountService.selectAccountById(1L));
    }

    @Test
    void testSelectAccountsByCustomerEmailReturnsAccounts() {
        List<Account> accounts = List.of(account);
        when(accountRepository.findAllByCustomersEmail("john@example.com")).thenReturn(accounts);
        List<Account> result = accountService.selectAccountsByCustomerEmail("john@example.com");
        assertEquals(accounts, result);
    }

    @Test
    void testSelectAccountsByCustomerIdReturnsAccounts() {
        List<Account> accounts = List.of(account);
        when(accountRepository.findAllByCustomersId(1L)).thenReturn(accounts);
        List<Account> result = accountService.selectAccountsByCustomerId(1L);
        assertEquals(accounts, result);
    }

    @Test
    void testUpdateAccountNotFoundThrowsException() {
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(AccountNotFoundException.class, () -> accountService.updateAccount(1L, "account name"));
    }

    @Test
    void testUpdateAccountValidUpdatesAccount() {
        Account existingAccount = new Account();
        existingAccount.setId(1L);
        existingAccount.setName("Old Name");
        when(accountRepository.findById(1L)).thenReturn(Optional.of(existingAccount));
        accountService.updateAccount(1L, "New Name");
        assertEquals("New Name", existingAccount.getName());
        assertEquals(1L, existingAccount.getId());
    }

    @Test
    void testRegisterCustomerToAccountAccountNotFoundThrowsException() {
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(AccountNotFoundException.class, () -> accountService.registerCustomerToAccount(1L, 1L));
    }

    @Test
    void testRegisterCustomerToAccountCustomerNotFoundThrowsException() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(CustomerNotFoundException.class, () -> accountService.registerCustomerToAccount(1L, 1L));
    }

    @Test
    void testRegisterCustomerToAccountValidRegistersCustomer() {
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setAccounts(new ArrayList<>());
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        accountService.registerCustomerToAccount(1L, 1L);
        assertTrue(account.getCustomers().contains(customer));
        assertTrue(customer.getAccounts().contains(account));
    }

    @Test
    void testGetAccountBalanceByCurrencyFound() {
        when(cardRepository.getBalanceForAccount(1L, "USD")).thenReturn(Optional.of(new BigDecimal("100.00")));
        BigDecimal result = accountService.getAccountBalanceByCurrency(1L, "USD");
        assertEquals(new BigDecimal("100.00"), result);
    }

    @Test
    void testGetAccountBalanceByCurrencyNotFoundThrowsException() {
        when(cardRepository.getBalanceForAccount(1L, "USD")).thenReturn(Optional.empty());
        assertThrows(CardBalanceNotFoundException.class, () -> accountService.getAccountBalanceByCurrency(1L, "USD"));
    }
}