package com.oop.web_project;

import com.oop.web_project.dto.requests.AccountCreationRequest;
import com.oop.web_project.dto.requests.CardCreationRequest;
import com.oop.web_project.dto.requests.CustomerRegistrationRequest;
import com.oop.web_project.dto.responses.*;
import com.oop.web_project.entities.*;
import com.oop.web_project.mapping.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountApiMapperTest {

    @Mock
    private TransactionApiMapper transactionApiMapper;

    @Mock
    private CardApiMapper cardApiMapper;

    @Mock
    private CustomerSummaryApiMapper customerSummaryApiMapper;

    @InjectMocks
    private AccountApiMapper accountApiMapper;

    private Account createAccount() {
        Transaction transaction = new Transaction();
        transaction.setId(1L);
        Card card = new Card();
        card.setId(1L);
        Customer customer = new Customer();
        customer.setId(1L);
        return new Account(1L, "Main Account", AccountCategory.CHECKING,
                LocalDate.of(2022, 1, 1), true,
                List.of(transaction), List.of(card), List.of(customer));
    }

    private AccountCreationRequest createAccountCreationRequest() {
        return new AccountCreationRequest("Savings Account", AccountCategory.SAVINGS);
    }

    @Test
    void testToProfileResponseSetsName() {
        Account account = createAccount();
        when(transactionApiMapper.toTransactionResponse(any())).thenReturn(new TransactionResponse());
        when(cardApiMapper.toCardResponse(any())).thenReturn(new CardResponse());
        when(customerSummaryApiMapper.toSummaryResponse(any())).thenReturn(new CustomerSummaryResponse());

        AccountProfileResponse response = accountApiMapper.toProfileResponse(account);

        assertEquals("Main Account", response.getName());
    }

    @Test
    void testToProfileResponseSetsCategory() {
        Account account = createAccount();
        when(transactionApiMapper.toTransactionResponse(any())).thenReturn(new TransactionResponse());
        when(cardApiMapper.toCardResponse(any())).thenReturn(new CardResponse());
        when(customerSummaryApiMapper.toSummaryResponse(any())).thenReturn(new CustomerSummaryResponse());

        AccountProfileResponse response = accountApiMapper.toProfileResponse(account);

        assertEquals(AccountCategory.CHECKING, response.getCategory());
    }

    @Test
    void testToProfileResponseSetsDateOpened() {
        Account account = createAccount();
        when(transactionApiMapper.toTransactionResponse(any())).thenReturn(new TransactionResponse());
        when(cardApiMapper.toCardResponse(any())).thenReturn(new CardResponse());
        when(customerSummaryApiMapper.toSummaryResponse(any())).thenReturn(new CustomerSummaryResponse());

        AccountProfileResponse response = accountApiMapper.toProfileResponse(account);

        assertEquals(LocalDate.of(2022, 1, 1), response.getDateOpened());
    }

    @Test
    void testToProfileResponseSetsActive() {
        Account account = createAccount();
        when(transactionApiMapper.toTransactionResponse(any())).thenReturn(new TransactionResponse());
        when(cardApiMapper.toCardResponse(any())).thenReturn(new CardResponse());
        when(customerSummaryApiMapper.toSummaryResponse(any())).thenReturn(new CustomerSummaryResponse());

        AccountProfileResponse response = accountApiMapper.toProfileResponse(account);

        assertTrue(response.isActive());
    }

    @Test
    void testToProfileResponseMapsTransactionsUsingTransactionApiMapper() {
        Account account = createAccount();
        TransactionResponse transactionResponse = new TransactionResponse();
        when(transactionApiMapper.toTransactionResponse(any())).thenReturn(transactionResponse);
        when(cardApiMapper.toCardResponse(any())).thenReturn(new CardResponse());
        when(customerSummaryApiMapper.toSummaryResponse(any())).thenReturn(new CustomerSummaryResponse());

        AccountProfileResponse response = accountApiMapper.toProfileResponse(account);

        assertEquals(1, response.getTransactions().size());
        assertSame(transactionResponse, response.getTransactions().get(0));
    }

    @Test
    void testToProfileResponseMapsCardsUsingCardApiMapper() {
        Account account = createAccount();
        CardResponse cardResponse = new CardResponse();
        when(transactionApiMapper.toTransactionResponse(any())).thenReturn(new TransactionResponse());
        when(cardApiMapper.toCardResponse(any())).thenReturn(cardResponse);
        when(customerSummaryApiMapper.toSummaryResponse(any())).thenReturn(new CustomerSummaryResponse());

        AccountProfileResponse response = accountApiMapper.toProfileResponse(account);

        assertEquals(1, response.getCards().size());
        assertSame(cardResponse, response.getCards().get(0));
    }

    @Test
    void testToProfileResponseMapsCustomersUsingCustomerSummaryApiMapper() {
        Account account = createAccount();
        CustomerSummaryResponse customerSummaryResponse = new CustomerSummaryResponse();
        when(transactionApiMapper.toTransactionResponse(any())).thenReturn(new TransactionResponse());
        when(cardApiMapper.toCardResponse(any())).thenReturn(new CardResponse());
        when(customerSummaryApiMapper.toSummaryResponse(any())).thenReturn(customerSummaryResponse);

        AccountProfileResponse response = accountApiMapper.toProfileResponse(account);

        assertEquals(1, response.getCustomers().size());
        assertSame(customerSummaryResponse, response.getCustomers().get(0));
    }

    @Test
    void testToProfileResponseWithEmptyListsReturnsEmptyLists() {
        Account account = new Account(1L, "Main Account", AccountCategory.CHECKING,
                LocalDate.of(2022, 1, 1), true,
                new ArrayList<>(), new ArrayList<>(), new ArrayList<>());

        AccountProfileResponse response = accountApiMapper.toProfileResponse(account);

        assertTrue(response.getTransactions().isEmpty());
        assertTrue(response.getCards().isEmpty());
        assertTrue(response.getCustomers().isEmpty());
    }

    @Test
    void testToAccountSetsNameFromRequest() {
        AccountCreationRequest request = createAccountCreationRequest();

        Account account = accountApiMapper.toAccount(request);

        assertEquals("Savings Account", account.getName());
    }

    @Test
    void testToAccountSetsCategoryFromRequest() {
        AccountCreationRequest request = createAccountCreationRequest();

        Account account = accountApiMapper.toAccount(request);

        assertEquals(AccountCategory.SAVINGS, account.getCategory());
    }

    @Test
    void testToAccountSetsDateOpenedToToday() {
        AccountCreationRequest request = createAccountCreationRequest();

        Account account = accountApiMapper.toAccount(request);

        assertEquals(LocalDate.now(), account.getDateOpened());
    }

    @Test
    void testToAccountSetsActiveToTrue() {
        AccountCreationRequest request = createAccountCreationRequest();

        Account account = accountApiMapper.toAccount(request);

        assertTrue(account.isActive());
    }

    @Test
    void testToAccountSetsIdToNull() {
        AccountCreationRequest request = createAccountCreationRequest();

        Account account = accountApiMapper.toAccount(request);

        assertNull(account.getId());
    }
}

class AccountSummaryApiMapperTest {

    private final AccountSummaryApiMapper accountSummaryApiMapper = new AccountSummaryApiMapper();

    private Account createAccount() {
        return new Account(1L, "Main Account", AccountCategory.CHECKING,
                LocalDate.of(2022, 1, 1), true, null, null, null);
    }

    @Test
    void testToAccountSummaryResponseSetsName() {
        Account account = createAccount();

        AccountSummaryResponse response = accountSummaryApiMapper.toAccountSummaryResponse(account);

        assertEquals("Main Account", response.getName());
    }

    @Test
    void testToAccountSummaryResponseSetsCategory() {
        Account account = createAccount();

        AccountSummaryResponse response = accountSummaryApiMapper.toAccountSummaryResponse(account);

        assertEquals(AccountCategory.CHECKING, response.getCategory());
    }

    @Test
    void testToAccountSummaryResponseSetsDateOpened() {
        Account account = createAccount();

        AccountSummaryResponse response = accountSummaryApiMapper.toAccountSummaryResponse(account);

        assertEquals(LocalDate.of(2022, 1, 1), response.getDateOpened());
    }

    @Test
    void testToAccountSummaryResponseSetsActive() {
        Account account = createAccount();

        AccountSummaryResponse response = accountSummaryApiMapper.toAccountSummaryResponse(account);

        assertTrue(response.isActive());
    }
}

@ExtendWith(MockitoExtension.class)
class CardApiMapperTest {

    @Mock
    private CardBalanceApiMapper cardBalanceApiMapper;

    @InjectMocks
    private CardApiMapper cardApiMapper;

    private Card createCard() {
        CardBalance balance = new CardBalance();
        balance.setId(1L);
        return new Card(1L, CardType.DEBIT, CardBrand.VISA, null, BigDecimal.valueOf(5000),
                LocalDate.of(2027, 12, 31), "****1234", "token_abc123", true, List.of(balance));
    }

    private CardCreationRequest createCardCreationRequest() {
        return new CardCreationRequest(CardType.CREDIT, CardBrand.MASTERCARD,
                BigDecimal.valueOf(2000), "4111111111111111");
    }

    @Test
    void testToCardOnCardCreationSetsType() {
        CardCreationRequest request = createCardCreationRequest();

        Card card = cardApiMapper.toCardOnCardCreation(request);

        assertEquals(CardType.CREDIT, card.getType());
    }

    @Test
    void testToCardOnCardCreationSetsBrand() {
        CardCreationRequest request = createCardCreationRequest();

        Card card = cardApiMapper.toCardOnCardCreation(request);

        assertEquals(CardBrand.MASTERCARD, card.getBrand());
    }

    @Test
    void testToCardOnCardCreationSetsSpendingLimit() {
        CardCreationRequest request = createCardCreationRequest();

        Card card = cardApiMapper.toCardOnCardCreation(request);

        assertEquals(BigDecimal.valueOf(2000), card.getSpendingLimit());
    }

    @Test
    void testToCardOnCardCreationSetsExpirationDateFiveYearsFromNow() {
        CardCreationRequest request = createCardCreationRequest();

        Card card = cardApiMapper.toCardOnCardCreation(request);

        assertEquals(LocalDate.now().plusYears(5), card.getExpirationDate());
    }

    @Test
    void testToCardOnCardCreationSetsActiveToTrue() {
        CardCreationRequest request = createCardCreationRequest();

        Card card = cardApiMapper.toCardOnCardCreation(request);

        assertTrue(card.isActive());
    }

    @Test
    void testToCardOnCardCreationSetsIdToNull() {
        CardCreationRequest request = createCardCreationRequest();

        Card card = cardApiMapper.toCardOnCardCreation(request);

        assertNull(card.getId());
    }

    @Test
    void testToCardOnCardCreationSetsAccountToNull() {
        CardCreationRequest request = createCardCreationRequest();

        Card card = cardApiMapper.toCardOnCardCreation(request);

        assertNull(card.getAccount());
    }

    @Test
    void testToCardOnCardCreationSetsPanMaskedFromPan() {
        CardCreationRequest request = createCardCreationRequest();

        Card card = cardApiMapper.toCardOnCardCreation(request);

        assertNotNull(card.getPanMasked());
    }

    @Test
    void testToCardOnCardCreationSetsPanTokenFromPan() {
        CardCreationRequest request = createCardCreationRequest();

        Card card = cardApiMapper.toCardOnCardCreation(request);

        assertNotNull(card.getPanToken());
    }

    @Test
    void testToCardResponseSetsType() {
        Card card = createCard();
        when(cardBalanceApiMapper.toCardBalanceResponse(any())).thenReturn(new CardBalanceResponse());

        CardResponse response = cardApiMapper.toCardResponse(card);

        assertEquals(CardType.DEBIT, response.getType());
    }

    @Test
    void testToCardResponseSetsBrand() {
        Card card = createCard();
        when(cardBalanceApiMapper.toCardBalanceResponse(any())).thenReturn(new CardBalanceResponse());

        CardResponse response = cardApiMapper.toCardResponse(card);

        assertEquals(CardBrand.VISA, response.getBrand());
    }

    @Test
    void testToCardResponseSetsSpendingLimit() {
        Card card = createCard();
        when(cardBalanceApiMapper.toCardBalanceResponse(any())).thenReturn(new CardBalanceResponse());

        CardResponse response = cardApiMapper.toCardResponse(card);

        assertEquals(BigDecimal.valueOf(5000), response.getSpendingLimit());
    }

    @Test
    void testToCardResponseSetsExpirationDate() {
        Card card = createCard();
        when(cardBalanceApiMapper.toCardBalanceResponse(any())).thenReturn(new CardBalanceResponse());

        CardResponse response = cardApiMapper.toCardResponse(card);

        assertEquals(LocalDate.of(2027, 12, 31), response.getExpirationDate());
    }

    @Test
    void testToCardResponseSetsPanMasked() {
        Card card = createCard();
        when(cardBalanceApiMapper.toCardBalanceResponse(any())).thenReturn(new CardBalanceResponse());

        CardResponse response = cardApiMapper.toCardResponse(card);

        assertEquals("****1234", response.getPanMasked());
    }

    @Test
    void testToCardResponseSetsPanToken() {
        Card card = createCard();
        when(cardBalanceApiMapper.toCardBalanceResponse(any())).thenReturn(new CardBalanceResponse());

        CardResponse response = cardApiMapper.toCardResponse(card);

        assertEquals("token_abc123", response.getPanToken());
    }

    @Test
    void testToCardResponseSetsActive() {
        Card card = createCard();
        when(cardBalanceApiMapper.toCardBalanceResponse(any())).thenReturn(new CardBalanceResponse());

        CardResponse response = cardApiMapper.toCardResponse(card);

        assertTrue(response.isActive());
    }

    @Test
    void testToCardResponseMapsBalancesUsingCardBalanceApiMapper() {
        Card card = createCard();
        CardBalanceResponse balanceResponse = new CardBalanceResponse();
        when(cardBalanceApiMapper.toCardBalanceResponse(any())).thenReturn(balanceResponse);

        CardResponse response = cardApiMapper.toCardResponse(card);

        assertEquals(1, response.getCardBalances().size());
        assertSame(balanceResponse, response.getCardBalances().get(0));
    }

    @Test
    void testToCardResponseWithEmptyBalancesReturnsEmptyList() {
        Card card = new Card(1L, CardType.DEBIT, CardBrand.VISA, null, BigDecimal.valueOf(5000),
                LocalDate.of(2027, 12, 31), "****1234", "token_abc123", true, new ArrayList<>());

        CardResponse response = cardApiMapper.toCardResponse(card);

        assertTrue(response.getCardBalances().isEmpty());
    }
}

class CardBalanceApiMapperTest {

    private final CardBalanceApiMapper cardBalanceApiMapper = new CardBalanceApiMapper();

    private CardBalance createCardBalance() {
        Currency currency = new Currency(1L, "GEL", "Georgian Lari", null, null, null, null);
        return new CardBalance(1L, new BigDecimal("1500.00"), null, currency);
    }

    @Test
    void testToCardBalanceResponseSetsAmount() {
        CardBalance balance = createCardBalance();

        CardBalanceResponse response = cardBalanceApiMapper.toCardBalanceResponse(balance);

        assertEquals(new BigDecimal("1500.00"), response.getAmount());
    }

    @Test
    void testToCardBalanceResponseSetsCurrencyCodeFromCurrency() {
        CardBalance balance = createCardBalance();

        CardBalanceResponse response = cardBalanceApiMapper.toCardBalanceResponse(balance);

        assertEquals("GEL", response.getCurrencyCode());
    }
}

@ExtendWith(MockitoExtension.class)
class CustomerApiMapperTest {

    @Mock
    private AccountSummaryApiMapper accountSummaryApiMapper;

    @InjectMocks
    private CustomerApiMapper customerApiMapper;

    private Customer createCustomer() {
        Account account = new Account();
        account.setId(1L);
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setFirstName("Giorgi");
        customer.setLastName("Maisuradze");
        customer.setPhoneNumber("555123456");
        customer.setAddress("Tbilisi, Rustaveli 1");
        customer.setDateOfBirth(LocalDate.of(1995, 3, 20));
        customer.setEmail("giorgi@example.com");
        customer.setHashedPassword("hashed_pw_123");
        customer.setActive(true);
        customer.setRole(Role.STANDARD);
        customer.setAccounts(List.of(account));
        return customer;
    }

    private CustomerRegistrationRequest createCustomerRegistrationRequest() {
        return new CustomerRegistrationRequest("Luka", "Beridze", "599000000",
                "Batumi, Aghmashenebeli 5", LocalDate.of(2000, 1, 1),
                "luka@example.com", "plainPassword");
    }

    @Test
    void testToCustomerOnRegistrationSetsFirstName() {
        CustomerRegistrationRequest request = createCustomerRegistrationRequest();

        Customer customer = customerApiMapper.toCustomerOnRegistration(request);

        assertEquals("Luka", customer.getFirstName());
    }

    @Test
    void testToCustomerOnRegistrationSetsLastName() {
        CustomerRegistrationRequest request = createCustomerRegistrationRequest();

        Customer customer = customerApiMapper.toCustomerOnRegistration(request);

        assertEquals("Beridze", customer.getLastName());
    }

    @Test
    void testToCustomerOnRegistrationSetsPhoneNumber() {
        CustomerRegistrationRequest request = createCustomerRegistrationRequest();

        Customer customer = customerApiMapper.toCustomerOnRegistration(request);

        assertEquals("599000000", customer.getPhoneNumber());
    }

    @Test
    void testToCustomerOnRegistrationSetsAddress() {
        CustomerRegistrationRequest request = createCustomerRegistrationRequest();

        Customer customer = customerApiMapper.toCustomerOnRegistration(request);

        assertEquals("Batumi, Aghmashenebeli 5", customer.getAddress());
    }

    @Test
    void testToCustomerOnRegistrationSetsDateOfBirth() {
        CustomerRegistrationRequest request = createCustomerRegistrationRequest();

        Customer customer = customerApiMapper.toCustomerOnRegistration(request);

        assertEquals(LocalDate.of(2000, 1, 1), customer.getDateOfBirth());
    }

    @Test
    void testToCustomerOnRegistrationSetsEmail() {
        CustomerRegistrationRequest request = createCustomerRegistrationRequest();

        Customer customer = customerApiMapper.toCustomerOnRegistration(request);

        assertEquals("luka@example.com", customer.getEmail());
    }

    @Test
    void testToCustomerOnRegistrationSetsAccountsToEmptyList() {
        CustomerRegistrationRequest request = createCustomerRegistrationRequest();

        Customer customer = customerApiMapper.toCustomerOnRegistration(request);

        assertNotNull(customer.getAccounts());
        assertTrue(customer.getAccounts().isEmpty());
    }

    @Test
    void testToCustomerOnRegistrationSetsActiveToTrue() {
        CustomerRegistrationRequest request = createCustomerRegistrationRequest();

        Customer customer = customerApiMapper.toCustomerOnRegistration(request);

        assertTrue(customer.isActive());
    }

    @Test
    void testToCustomerOnRegistrationSetsHashedPasswordFromRequestPassword() {
        CustomerRegistrationRequest request = createCustomerRegistrationRequest();

        Customer customer = customerApiMapper.toCustomerOnRegistration(request);

        assertEquals("plainPassword", customer.getHashedPassword());
    }

    @Test
    void testToCustomerOnRegistrationSetsRoleToStandard() {
        CustomerRegistrationRequest request = createCustomerRegistrationRequest();

        Customer customer = customerApiMapper.toCustomerOnRegistration(request);

        assertEquals(Role.STANDARD, customer.getRole());
    }

    @Test
    void testToProfileResponseSetsFirstName() {
        Customer customer = createCustomer();
        when(accountSummaryApiMapper.toAccountSummaryResponse(any())).thenReturn(new AccountSummaryResponse());

        CustomerProfileResponse response = customerApiMapper.toProfileResponse(customer);

        assertEquals("Giorgi", response.getFirstName());
    }

    @Test
    void testToProfileResponseSetsLastName() {
        Customer customer = createCustomer();
        when(accountSummaryApiMapper.toAccountSummaryResponse(any())).thenReturn(new AccountSummaryResponse());

        CustomerProfileResponse response = customerApiMapper.toProfileResponse(customer);

        assertEquals("Maisuradze", response.getLastName());
    }

    @Test
    void testToProfileResponseSetsDateOfBirth() {
        Customer customer = createCustomer();
        when(accountSummaryApiMapper.toAccountSummaryResponse(any())).thenReturn(new AccountSummaryResponse());

        CustomerProfileResponse response = customerApiMapper.toProfileResponse(customer);

        assertEquals(LocalDate.of(1995, 3, 20), response.getDateOfBirth());
    }

    @Test
    void testToProfileResponseSetsAddress() {
        Customer customer = createCustomer();
        when(accountSummaryApiMapper.toAccountSummaryResponse(any())).thenReturn(new AccountSummaryResponse());

        CustomerProfileResponse response = customerApiMapper.toProfileResponse(customer);

        assertEquals("Tbilisi, Rustaveli 1", response.getAddress());
    }

    @Test
    void testToProfileResponseSetsEmail() {
        Customer customer = createCustomer();
        when(accountSummaryApiMapper.toAccountSummaryResponse(any())).thenReturn(new AccountSummaryResponse());

        CustomerProfileResponse response = customerApiMapper.toProfileResponse(customer);

        assertEquals("giorgi@example.com", response.getEmail());
    }

    @Test
    void testToProfileResponseSetsPhoneNumber() {
        Customer customer = createCustomer();
        when(accountSummaryApiMapper.toAccountSummaryResponse(any())).thenReturn(new AccountSummaryResponse());

        CustomerProfileResponse response = customerApiMapper.toProfileResponse(customer);

        assertEquals("555123456", response.getPhoneNumber());
    }

    @Test
    void testToProfileResponseMapsAccountsUsingAccountSummaryApiMapper() {
        Customer customer = createCustomer();
        AccountSummaryResponse accountSummaryResponse = new AccountSummaryResponse();
        when(accountSummaryApiMapper.toAccountSummaryResponse(any())).thenReturn(accountSummaryResponse);

        CustomerProfileResponse response = customerApiMapper.toProfileResponse(customer);

        assertEquals(1, response.getAccounts().size());
        assertSame(accountSummaryResponse, response.getAccounts().get(0));
    }

    @Test
    void testToProfileResponseWithEmptyAccountsReturnsEmptyList() {
        Customer customer = createCustomer();
        customer.setAccounts(new ArrayList<>());

        CustomerProfileResponse response = customerApiMapper.toProfileResponse(customer);

        assertTrue(response.getAccounts().isEmpty());
    }
}

class CustomerSummaryApiMapperTest {

    private final CustomerSummaryApiMapper customerSummaryApiMapper = new CustomerSummaryApiMapper();

    private Customer createCustomer() {
        Customer customer = new Customer();
        customer.setFirstName("Giorgi");
        customer.setLastName("Maisuradze");
        customer.setEmail("giorgi@example.com");
        return customer;
    }

    @Test
    void testToSummaryResponseSetsFirstName() {
        Customer customer = createCustomer();

        CustomerSummaryResponse response = customerSummaryApiMapper.toSummaryResponse(customer);

        assertEquals("Giorgi", response.getFirstName());
    }

    @Test
    void testToSummaryResponseSetsLastName() {
        Customer customer = createCustomer();

        CustomerSummaryResponse response = customerSummaryApiMapper.toSummaryResponse(customer);

        assertEquals("Maisuradze", response.getLastName());
    }

    @Test
    void testToSummaryResponseSetsEmail() {
        Customer customer = createCustomer();

        CustomerSummaryResponse response = customerSummaryApiMapper.toSummaryResponse(customer);

        assertEquals("giorgi@example.com", response.getEmail());
    }
}

class TransactionApiMapperTest {

    private final TransactionApiMapper transactionApiMapper = new TransactionApiMapper();

    private Transaction createTransaction() {
        Currency gel = new Currency(1L, "GEL", "Georgian Lari", null, null, null, null);
        return new Transaction(1L, TransactionType.DEPOSIT, null,
                LocalDateTime.of(2024, 6, 1, 10, 30, 0),
                new BigDecimal("250.00"), gel, null, null,
                "idem_key_001", "Monthly payment", TransactionStatus.COMPLETE);
    }

    @Test
    void testToTransactionResponseSetsTransactionType() {
        Transaction transaction = createTransaction();

        TransactionResponse response = transactionApiMapper.toTransactionResponse(transaction);

        assertEquals(TransactionType.DEPOSIT, response.getTransactionType());
    }

    @Test
    void testToTransactionResponseSetsTimeStamp() {
        Transaction transaction = createTransaction();

        TransactionResponse response = transactionApiMapper.toTransactionResponse(transaction);

        assertEquals(LocalDateTime.of(2024, 6, 1, 10, 30, 0), response.getTimeStamp());
    }

    @Test
    void testToTransactionResponseSetsAmount() {
        Transaction transaction = createTransaction();

        TransactionResponse response = transactionApiMapper.toTransactionResponse(transaction);

        assertEquals(new BigDecimal("250.00"), response.getAmount());
    }

    @Test
    void testToTransactionResponseSetsDescription() {
        Transaction transaction = createTransaction();

        TransactionResponse response = transactionApiMapper.toTransactionResponse(transaction);

        assertEquals("Monthly payment", response.getDescription());
    }

    @Test
    void testToTransactionResponseSetsStatus() {
        Transaction transaction = createTransaction();

        TransactionResponse response = transactionApiMapper.toTransactionResponse(transaction);

        assertEquals(TransactionStatus.COMPLETE, response.getStatus());
    }

    @Test
    void testToTransactionResponseSetsCurrencyCodeFromCurrency() {
        Transaction transaction = createTransaction();

        TransactionResponse response = transactionApiMapper.toTransactionResponse(transaction);

        assertEquals("GEL", response.getCurrencyCode());
    }
}
