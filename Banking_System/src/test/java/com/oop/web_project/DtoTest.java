package com.oop.web_project;

import com.oop.web_project.dto.requests.*;
import com.oop.web_project.dto.responses.*;
import com.oop.web_project.entities.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AccountCreationRequestTest {

    private AccountCreationRequest createAccountCreationRequest() {
        return new AccountCreationRequest("Main Account", AccountCategory.CHECKING);
    }

    @Test
    void testGetAccountNameReturnsCorrectValue() {
        AccountCreationRequest request = createAccountCreationRequest();

        assertEquals("Main Account", request.getAccountName());
    }

    @Test
    void testSetAccountNameUpdatesValue() {
        AccountCreationRequest request = createAccountCreationRequest();

        request.setAccountName("Savings Account");

        assertEquals("Savings Account", request.getAccountName());
    }

    @Test
    void testGetCategoryReturnsCorrectValue() {
        AccountCreationRequest request = createAccountCreationRequest();

        assertEquals(AccountCategory.CHECKING, request.getCategory());
    }

    @Test
    void testSetCategoryUpdatesValue() {
        AccountCreationRequest request = createAccountCreationRequest();

        request.setCategory(AccountCategory.SAVINGS);

        assertEquals(AccountCategory.SAVINGS, request.getCategory());
    }
}

class CardCreationRequestTest {

    private CardCreationRequest createCardCreationRequest() {
        return new CardCreationRequest(CardType.DEBIT, CardBrand.VISA,
                BigDecimal.valueOf(5000), "1234567890123456");
    }

    @Test
    void testGetCardTypeReturnsCorrectValue() {
        CardCreationRequest request = createCardCreationRequest();

        assertEquals(CardType.DEBIT, request.getCardType());
    }

    @Test
    void testSetCardTypeUpdatesValue() {
        CardCreationRequest request = createCardCreationRequest();

        request.setCardType(CardType.CREDIT);

        assertEquals(CardType.CREDIT, request.getCardType());
    }

    @Test
    void testGetCardBrandReturnsCorrectValue() {
        CardCreationRequest request = createCardCreationRequest();

        assertEquals(CardBrand.VISA, request.getCardBrand());
    }

    @Test
    void testSetCardBrandUpdatesValue() {
        CardCreationRequest request = createCardCreationRequest();

        request.setCardBrand(CardBrand.MASTERCARD);

        assertEquals(CardBrand.MASTERCARD, request.getCardBrand());
    }

    @Test
    void testGetSpendingLimitReturnsCorrectValue() {
        CardCreationRequest request = createCardCreationRequest();

        assertEquals(BigDecimal.valueOf(5000), request.getSpendingLimit());
    }

    @Test
    void testSetSpendingLimitUpdatesValue() {
        CardCreationRequest request = createCardCreationRequest();

        request.setSpendingLimit(BigDecimal.valueOf(10000));

        assertEquals(BigDecimal.valueOf(10000), request.getSpendingLimit());
    }

    @Test
    void testGetPanReturnsCorrectValue() {
        CardCreationRequest request = createCardCreationRequest();

        assertEquals("1234567890123456", request.getPan());
    }

    @Test
    void testSetPanUpdatesValue() {
        CardCreationRequest request = createCardCreationRequest();

        request.setPan("6543210987654321");

        assertEquals("6543210987654321", request.getPan());
    }
}

class CardDepositRequestTest {

    private CardDepositRequest createCardDepositRequest() {
        return new CardDepositRequest(new BigDecimal("250.00"), "GEL");
    }

    @Test
    void testGetAmountToDepositReturnsCorrectValue() {
        CardDepositRequest request = createCardDepositRequest();

        assertEquals(new BigDecimal("250.00"), request.getAmountToDeposit());
    }

    @Test
    void testSetAmountToDepositUpdatesValue() {
        CardDepositRequest request = createCardDepositRequest();

        request.setAmountToDeposit(new BigDecimal("500.00"));

        assertEquals(new BigDecimal("500.00"), request.getAmountToDeposit());
    }

    @Test
    void testGetCurrencyCodeReturnsCorrectValue() {
        CardDepositRequest request = createCardDepositRequest();

        assertEquals("GEL", request.getCurrencyCode());
    }

    @Test
    void testSetCurrencyCodeUpdatesValue() {
        CardDepositRequest request = createCardDepositRequest();

        request.setCurrencyCode("USD");

        assertEquals("USD", request.getCurrencyCode());
    }
}

class CardTransferRequestTest {

    private CardTransferRequest createCardTransferRequest() {
        return new CardTransferRequest(1L, 2L, new BigDecimal("100.00"), "GEL");
    }

    @Test
    void testGetSenderCardIdReturnsCorrectValue() {
        CardTransferRequest request = createCardTransferRequest();

        assertEquals(1L, request.getSenderCardId());
    }

    @Test
    void testSetSenderCardIdUpdatesValue() {
        CardTransferRequest request = createCardTransferRequest();

        request.setSenderCardId(99L);

        assertEquals(99L, request.getSenderCardId());
    }

    @Test
    void testGetReceiverCardIdReturnsCorrectValue() {
        CardTransferRequest request = createCardTransferRequest();

        assertEquals(2L, request.getReceiverCardId());
    }

    @Test
    void testSetReceiverCardIdUpdatesValue() {
        CardTransferRequest request = createCardTransferRequest();

        request.setReceiverCardId(88L);

        assertEquals(88L, request.getReceiverCardId());
    }

    @Test
    void testGetAmountReturnsCorrectValue() {
        CardTransferRequest request = createCardTransferRequest();

        assertEquals(new BigDecimal("100.00"), request.getAmount());
    }

    @Test
    void testSetAmountUpdatesValue() {
        CardTransferRequest request = createCardTransferRequest();

        request.setAmount(new BigDecimal("200.00"));

        assertEquals(new BigDecimal("200.00"), request.getAmount());
    }

    @Test
    void testGetCurrencyCodeReturnsCorrectValue() {
        CardTransferRequest request = createCardTransferRequest();

        assertEquals("GEL", request.getCurrencyCode());
    }

    @Test
    void testSetCurrencyCodeUpdatesValue() {
        CardTransferRequest request = createCardTransferRequest();

        request.setCurrencyCode("USD");

        assertEquals("USD", request.getCurrencyCode());
    }
}

class CardWithdrawRequestTest {

    private CardWithdrawRequest createCardWithdrawRequest() {
        return new CardWithdrawRequest(new BigDecimal("75.00"), "GEL");
    }

    @Test
    void testGetAmountToWithdrawReturnsCorrectValue() {
        CardWithdrawRequest request = createCardWithdrawRequest();

        assertEquals(new BigDecimal("75.00"), request.getAmountToWithdraw());
    }

    @Test
    void testSetAmountToWithdrawUpdatesValue() {
        CardWithdrawRequest request = createCardWithdrawRequest();

        request.setAmountToWithdraw(new BigDecimal("150.00"));

        assertEquals(new BigDecimal("150.00"), request.getAmountToWithdraw());
    }

    @Test
    void testGetCurrencyCodeReturnsCorrectValue() {
        CardWithdrawRequest request = createCardWithdrawRequest();

        assertEquals("GEL", request.getCurrencyCode());
    }

    @Test
    void testSetCurrencyCodeUpdatesValue() {
        CardWithdrawRequest request = createCardWithdrawRequest();

        request.setCurrencyCode("USD");

        assertEquals("USD", request.getCurrencyCode());
    }
}

class CurrencyExchangeRequestTest {

    private CurrencyExchangeRequest createCurrencyExchangeRequest() {
        return new CurrencyExchangeRequest(new BigDecimal("500.00"), "GEL", "USD");
    }

    @Test
    void testGetAmountReturnsCorrectValue() {
        CurrencyExchangeRequest request = createCurrencyExchangeRequest();

        assertEquals(new BigDecimal("500.00"), request.getAmount());
    }

    @Test
    void testSetAmountUpdatesValue() {
        CurrencyExchangeRequest request = createCurrencyExchangeRequest();

        request.setAmount(new BigDecimal("750.00"));

        assertEquals(new BigDecimal("750.00"), request.getAmount());
    }

    @Test
    void testGetFromCurrencyCodeReturnsCorrectValue() {
        CurrencyExchangeRequest request = createCurrencyExchangeRequest();

        assertEquals("GEL", request.getFromCurrencyCode());
    }

    @Test
    void testSetFromCurrencyCodeUpdatesValue() {
        CurrencyExchangeRequest request = createCurrencyExchangeRequest();

        request.setFromCurrencyCode("EUR");

        assertEquals("EUR", request.getFromCurrencyCode());
    }

    @Test
    void testGetToCurrencyCodeReturnsCorrectValue() {
        CurrencyExchangeRequest request = createCurrencyExchangeRequest();

        assertEquals("USD", request.getToCurrencyCode());
    }

    @Test
    void testSetToCurrencyCodeUpdatesValue() {
        CurrencyExchangeRequest request = createCurrencyExchangeRequest();

        request.setToCurrencyCode("EUR");

        assertEquals("EUR", request.getToCurrencyCode());
    }
}

class CustomerLoginRequestTest {

    private CustomerLoginRequest createCustomerLoginRequest() {
        return new CustomerLoginRequest("giorgi@example.com", "password123");
    }

    @Test
    void testGetEmailReturnsCorrectValue() {
        CustomerLoginRequest request = createCustomerLoginRequest();

        assertEquals("giorgi@example.com", request.getEmail());
    }

    @Test
    void testSetEmailUpdatesValue() {
        CustomerLoginRequest request = createCustomerLoginRequest();

        request.setEmail("luka@example.com");

        assertEquals("luka@example.com", request.getEmail());
    }

    @Test
    void testGetPasswordReturnsCorrectValue() {
        CustomerLoginRequest request = createCustomerLoginRequest();

        assertEquals("password123", request.getPassword());
    }

    @Test
    void testSetPasswordUpdatesValue() {
        CustomerLoginRequest request = createCustomerLoginRequest();

        request.setPassword("newPassword456");

        assertEquals("newPassword456", request.getPassword());
    }
}

class CustomerRegistrationRequestTest {

    private CustomerRegistrationRequest createCustomerRegistrationRequest() {
        return new CustomerRegistrationRequest("Giorgi", "Maisuradze", "555123456",
                "Tbilisi, Rustaveli 1", LocalDate.of(1995, 3, 20),
                "giorgi@example.com", "password123");
    }

    @Test
    void testGetFirstNameReturnsCorrectValue() {
        CustomerRegistrationRequest request = createCustomerRegistrationRequest();

        assertEquals("Giorgi", request.getFirstName());
    }

    @Test
    void testSetFirstNameUpdatesValue() {
        CustomerRegistrationRequest request = createCustomerRegistrationRequest();

        request.setFirstName("Luka");

        assertEquals("Luka", request.getFirstName());
    }

    @Test
    void testGetLastNameReturnsCorrectValue() {
        CustomerRegistrationRequest request = createCustomerRegistrationRequest();

        assertEquals("Maisuradze", request.getLastName());
    }

    @Test
    void testSetLastNameUpdatesValue() {
        CustomerRegistrationRequest request = createCustomerRegistrationRequest();

        request.setLastName("Beridze");

        assertEquals("Beridze", request.getLastName());
    }

    @Test
    void testGetPhoneNumberReturnsCorrectValue() {
        CustomerRegistrationRequest request = createCustomerRegistrationRequest();

        assertEquals("555123456", request.getPhoneNumber());
    }

    @Test
    void testSetPhoneNumberUpdatesValue() {
        CustomerRegistrationRequest request = createCustomerRegistrationRequest();

        request.setPhoneNumber("599000000");

        assertEquals("599000000", request.getPhoneNumber());
    }

    @Test
    void testGetAddressReturnsCorrectValue() {
        CustomerRegistrationRequest request = createCustomerRegistrationRequest();

        assertEquals("Tbilisi, Rustaveli 1", request.getAddress());
    }

    @Test
    void testSetAddressUpdatesValue() {
        CustomerRegistrationRequest request = createCustomerRegistrationRequest();

        request.setAddress("Batumi, Aghmashenebeli 5");

        assertEquals("Batumi, Aghmashenebeli 5", request.getAddress());
    }

    @Test
    void testGetDateOfBirthReturnsCorrectValue() {
        CustomerRegistrationRequest request = createCustomerRegistrationRequest();

        assertEquals(LocalDate.of(1995, 3, 20), request.getDateOfBirth());
    }

    @Test
    void testSetDateOfBirthUpdatesValue() {
        CustomerRegistrationRequest request = createCustomerRegistrationRequest();

        request.setDateOfBirth(LocalDate.of(2000, 1, 1));

        assertEquals(LocalDate.of(2000, 1, 1), request.getDateOfBirth());
    }

    @Test
    void testGetEmailReturnsCorrectValue() {
        CustomerRegistrationRequest request = createCustomerRegistrationRequest();

        assertEquals("giorgi@example.com", request.getEmail());
    }

    @Test
    void testSetEmailUpdatesValue() {
        CustomerRegistrationRequest request = createCustomerRegistrationRequest();

        request.setEmail("luka@example.com");

        assertEquals("luka@example.com", request.getEmail());
    }

    @Test
    void testGetPasswordReturnsCorrectValue() {
        CustomerRegistrationRequest request = createCustomerRegistrationRequest();

        assertEquals("password123", request.getPassword());
    }

    @Test
    void testSetPasswordUpdatesValue() {
        CustomerRegistrationRequest request = createCustomerRegistrationRequest();

        request.setPassword("newPassword456");

        assertEquals("newPassword456", request.getPassword());
    }
}

class CustomerUpdateRequestTest {

    private CustomerUpdateRequest createCustomerUpdateRequest() {
        return new CustomerUpdateRequest("Giorgi", "Maisuradze", "555123456", "Tbilisi, Rustaveli 1");
    }

    @Test
    void testGetFirstNameReturnsCorrectValue() {
        CustomerUpdateRequest request = createCustomerUpdateRequest();

        assertEquals("Giorgi", request.getFirstName());
    }

    @Test
    void testSetFirstNameUpdatesValue() {
        CustomerUpdateRequest request = createCustomerUpdateRequest();

        request.setFirstName("Luka");

        assertEquals("Luka", request.getFirstName());
    }

    @Test
    void testGetLastNameReturnsCorrectValue() {
        CustomerUpdateRequest request = createCustomerUpdateRequest();

        assertEquals("Maisuradze", request.getLastName());
    }

    @Test
    void testSetLastNameUpdatesValue() {
        CustomerUpdateRequest request = createCustomerUpdateRequest();

        request.setLastName("Beridze");

        assertEquals("Beridze", request.getLastName());
    }

    @Test
    void testGetPhoneNumberReturnsCorrectValue() {
        CustomerUpdateRequest request = createCustomerUpdateRequest();

        assertEquals("555123456", request.getPhoneNumber());
    }

    @Test
    void testSetPhoneNumberUpdatesValue() {
        CustomerUpdateRequest request = createCustomerUpdateRequest();

        request.setPhoneNumber("599000000");

        assertEquals("599000000", request.getPhoneNumber());
    }

    @Test
    void testGetAddressReturnsCorrectValue() {
        CustomerUpdateRequest request = createCustomerUpdateRequest();

        assertEquals("Tbilisi, Rustaveli 1", request.getAddress());
    }

    @Test
    void testSetAddressUpdatesValue() {
        CustomerUpdateRequest request = createCustomerUpdateRequest();

        request.setAddress("Batumi, Aghmashenebeli 5");

        assertEquals("Batumi, Aghmashenebeli 5", request.getAddress());
    }
}

class AccountProfileResponseTest {

    private AccountProfileResponse createAccountProfileResponse() {
        return new AccountProfileResponse("Main Account", AccountCategory.CHECKING,
                LocalDate.of(2022, 1, 1), true,
                List.of(new TransactionResponse()),
                List.of(new CardResponse()),
                List.of(new CustomerSummaryResponse()));
    }

    @Test
    void testGetNameReturnsCorrectValue() {
        AccountProfileResponse response = createAccountProfileResponse();

        assertEquals("Main Account", response.getName());
    }

    @Test
    void testSetNameUpdatesValue() {
        AccountProfileResponse response = createAccountProfileResponse();

        response.setName("Savings Account");

        assertEquals("Savings Account", response.getName());
    }

    @Test
    void testGetCategoryReturnsCorrectValue() {
        AccountProfileResponse response = createAccountProfileResponse();

        assertEquals(AccountCategory.CHECKING, response.getCategory());
    }

    @Test
    void testSetCategoryUpdatesValue() {
        AccountProfileResponse response = createAccountProfileResponse();

        response.setCategory(AccountCategory.SAVINGS);

        assertEquals(AccountCategory.SAVINGS, response.getCategory());
    }

    @Test
    void testGetDateOpenedReturnsCorrectValue() {
        AccountProfileResponse response = createAccountProfileResponse();

        assertEquals(LocalDate.of(2022, 1, 1), response.getDateOpened());
    }

    @Test
    void testSetDateOpenedUpdatesValue() {
        AccountProfileResponse response = createAccountProfileResponse();

        response.setDateOpened(LocalDate.of(2023, 6, 15));

        assertEquals(LocalDate.of(2023, 6, 15), response.getDateOpened());
    }

    @Test
    void testIsActiveReturnsTrueByDefault() {
        AccountProfileResponse response = createAccountProfileResponse();

        assertTrue(response.isActive());
    }

    @Test
    void testSetActiveUpdatesValue() {
        AccountProfileResponse response = createAccountProfileResponse();

        response.setActive(false);

        assertFalse(response.isActive());
    }

    @Test
    void testGetTransactionsReturnsCorrectValue() {
        AccountProfileResponse response = createAccountProfileResponse();

        assertEquals(1, response.getTransactions().size());
    }

    @Test
    void testSetTransactionsUpdatesValue() {
        AccountProfileResponse response = createAccountProfileResponse();
        List<TransactionResponse> newTransactions = List.of();

        response.setTransactions(newTransactions);

        assertEquals(0, response.getTransactions().size());
    }

    @Test
    void testGetCardsReturnsCorrectValue() {
        AccountProfileResponse response = createAccountProfileResponse();

        assertEquals(1, response.getCards().size());
    }

    @Test
    void testSetCardsUpdatesValue() {
        AccountProfileResponse response = createAccountProfileResponse();
        List<CardResponse> newCards = List.of();

        response.setCards(newCards);

        assertEquals(0, response.getCards().size());
    }

    @Test
    void testGetCustomersReturnsCorrectValue() {
        AccountProfileResponse response = createAccountProfileResponse();

        assertEquals(1, response.getCustomers().size());
    }

    @Test
    void testSetCustomersUpdatesValue() {
        AccountProfileResponse response = createAccountProfileResponse();
        List<CustomerSummaryResponse> newCustomers = List.of();

        response.setCustomers(newCustomers);

        assertEquals(0, response.getCustomers().size());
    }
}

class AccountSummaryResponseTest {

    private AccountSummaryResponse createAccountSummaryResponse() {
        return new AccountSummaryResponse("Main Account", AccountCategory.CHECKING,
                LocalDate.of(2022, 1, 1), true);
    }

    @Test
    void testGetNameReturnsCorrectValue() {
        AccountSummaryResponse response = createAccountSummaryResponse();

        assertEquals("Main Account", response.getName());
    }

    @Test
    void testSetNameUpdatesValue() {
        AccountSummaryResponse response = createAccountSummaryResponse();

        response.setName("Savings Account");

        assertEquals("Savings Account", response.getName());
    }

    @Test
    void testGetCategoryReturnsCorrectValue() {
        AccountSummaryResponse response = createAccountSummaryResponse();

        assertEquals(AccountCategory.CHECKING, response.getCategory());
    }

    @Test
    void testSetCategoryUpdatesValue() {
        AccountSummaryResponse response = createAccountSummaryResponse();

        response.setCategory(AccountCategory.SAVINGS);

        assertEquals(AccountCategory.SAVINGS, response.getCategory());
    }

    @Test
    void testGetDateOpenedReturnsCorrectValue() {
        AccountSummaryResponse response = createAccountSummaryResponse();

        assertEquals(LocalDate.of(2022, 1, 1), response.getDateOpened());
    }

    @Test
    void testSetDateOpenedUpdatesValue() {
        AccountSummaryResponse response = createAccountSummaryResponse();

        response.setDateOpened(LocalDate.of(2023, 6, 15));

        assertEquals(LocalDate.of(2023, 6, 15), response.getDateOpened());
    }

    @Test
    void testIsActiveReturnsTrueByDefault() {
        AccountSummaryResponse response = createAccountSummaryResponse();

        assertTrue(response.isActive());
    }

    @Test
    void testSetActiveUpdatesValue() {
        AccountSummaryResponse response = createAccountSummaryResponse();

        response.setActive(false);

        assertFalse(response.isActive());
    }
}

class CardBalanceResponseTest {

    private CardBalanceResponse createCardBalanceResponse() {
        return new CardBalanceResponse(new BigDecimal("1500.00"), "GEL");
    }

    @Test
    void testGetAmountReturnsCorrectValue() {
        CardBalanceResponse response = createCardBalanceResponse();

        assertEquals(new BigDecimal("1500.00"), response.getAmount());
    }

    @Test
    void testSetAmountUpdatesValue() {
        CardBalanceResponse response = createCardBalanceResponse();

        response.setAmount(new BigDecimal("2000.00"));

        assertEquals(new BigDecimal("2000.00"), response.getAmount());
    }

    @Test
    void testGetCurrencyCodeReturnsCorrectValue() {
        CardBalanceResponse response = createCardBalanceResponse();

        assertEquals("GEL", response.getCurrencyCode());
    }

    @Test
    void testSetCurrencyCodeUpdatesValue() {
        CardBalanceResponse response = createCardBalanceResponse();

        response.setCurrencyCode("USD");

        assertEquals("USD", response.getCurrencyCode());
    }
}

class CardResponseTest {

    private CardResponse createCardResponse() {
        return new CardResponse(CardType.DEBIT, CardBrand.VISA, BigDecimal.valueOf(5000),
                LocalDate.of(2027, 12, 31), "****1234", "token_abc123", true,
                List.of(new CardBalanceResponse()));
    }

    @Test
    void testGetTypeReturnsCorrectValue() {
        CardResponse response = createCardResponse();

        assertEquals(CardType.DEBIT, response.getType());
    }

    @Test
    void testSetTypeUpdatesValue() {
        CardResponse response = createCardResponse();

        response.setType(CardType.CREDIT);

        assertEquals(CardType.CREDIT, response.getType());
    }

    @Test
    void testGetBrandReturnsCorrectValue() {
        CardResponse response = createCardResponse();

        assertEquals(CardBrand.VISA, response.getBrand());
    }

    @Test
    void testSetBrandUpdatesValue() {
        CardResponse response = createCardResponse();

        response.setBrand(CardBrand.MASTERCARD);

        assertEquals(CardBrand.MASTERCARD, response.getBrand());
    }

    @Test
    void testGetSpendingLimitReturnsCorrectValue() {
        CardResponse response = createCardResponse();

        assertEquals(BigDecimal.valueOf(5000), response.getSpendingLimit());
    }

    @Test
    void testSetSpendingLimitUpdatesValue() {
        CardResponse response = createCardResponse();

        response.setSpendingLimit(BigDecimal.valueOf(10000));

        assertEquals(BigDecimal.valueOf(10000), response.getSpendingLimit());
    }

    @Test
    void testGetExpirationDateReturnsCorrectValue() {
        CardResponse response = createCardResponse();

        assertEquals(LocalDate.of(2027, 12, 31), response.getExpirationDate());
    }

    @Test
    void testSetExpirationDateUpdatesValue() {
        CardResponse response = createCardResponse();

        response.setExpirationDate(LocalDate.of(2028, 6, 30));

        assertEquals(LocalDate.of(2028, 6, 30), response.getExpirationDate());
    }

    @Test
    void testGetPanMaskedReturnsCorrectValue() {
        CardResponse response = createCardResponse();

        assertEquals("****1234", response.getPanMasked());
    }

    @Test
    void testSetPanMaskedUpdatesValue() {
        CardResponse response = createCardResponse();

        response.setPanMasked("****5678");

        assertEquals("****5678", response.getPanMasked());
    }

    @Test
    void testGetPanTokenReturnsCorrectValue() {
        CardResponse response = createCardResponse();

        assertEquals("token_abc123", response.getPanToken());
    }

    @Test
    void testSetPanTokenUpdatesValue() {
        CardResponse response = createCardResponse();

        response.setPanToken("token_xyz999");

        assertEquals("token_xyz999", response.getPanToken());
    }

    @Test
    void testIsActiveReturnsTrueByDefault() {
        CardResponse response = createCardResponse();

        assertTrue(response.isActive());
    }

    @Test
    void testSetActiveUpdatesValue() {
        CardResponse response = createCardResponse();

        response.setActive(false);

        assertFalse(response.isActive());
    }

    @Test
    void testGetCardBalancesReturnsCorrectValue() {
        CardResponse response = createCardResponse();

        assertEquals(1, response.getCardBalances().size());
    }

    @Test
    void testSetCardBalancesUpdatesValue() {
        CardResponse response = createCardResponse();
        List<CardBalanceResponse> newCardBalances = List.of();

        response.setCardBalances(newCardBalances);

        assertEquals(0, response.getCardBalances().size());
    }
}

class CustomerProfileResponseTest {

    private CustomerProfileResponse createCustomerProfileResponse() {
        return new CustomerProfileResponse("Giorgi", "Maisuradze", LocalDate.of(1995, 3, 20),
                "Tbilisi, Rustaveli 1", "giorgi@example.com", "555123456",
                List.of(new AccountSummaryResponse()));
    }

    @Test
    void testGetFirstNameReturnsCorrectValue() {
        CustomerProfileResponse response = createCustomerProfileResponse();

        assertEquals("Giorgi", response.getFirstName());
    }

    @Test
    void testSetFirstNameUpdatesValue() {
        CustomerProfileResponse response = createCustomerProfileResponse();

        response.setFirstName("Luka");

        assertEquals("Luka", response.getFirstName());
    }

    @Test
    void testGetLastNameReturnsCorrectValue() {
        CustomerProfileResponse response = createCustomerProfileResponse();

        assertEquals("Maisuradze", response.getLastName());
    }

    @Test
    void testSetLastNameUpdatesValue() {
        CustomerProfileResponse response = createCustomerProfileResponse();

        response.setLastName("Beridze");

        assertEquals("Beridze", response.getLastName());
    }

    @Test
    void testGetDateOfBirthReturnsCorrectValue() {
        CustomerProfileResponse response = createCustomerProfileResponse();

        assertEquals(LocalDate.of(1995, 3, 20), response.getDateOfBirth());
    }

    @Test
    void testSetDateOfBirthUpdatesValue() {
        CustomerProfileResponse response = createCustomerProfileResponse();

        response.setDateOfBirth(LocalDate.of(2000, 1, 1));

        assertEquals(LocalDate.of(2000, 1, 1), response.getDateOfBirth());
    }

    @Test
    void testGetAddressReturnsCorrectValue() {
        CustomerProfileResponse response = createCustomerProfileResponse();

        assertEquals("Tbilisi, Rustaveli 1", response.getAddress());
    }

    @Test
    void testSetAddressUpdatesValue() {
        CustomerProfileResponse response = createCustomerProfileResponse();

        response.setAddress("Batumi, Aghmashenebeli 5");

        assertEquals("Batumi, Aghmashenebeli 5", response.getAddress());
    }

    @Test
    void testGetEmailReturnsCorrectValue() {
        CustomerProfileResponse response = createCustomerProfileResponse();

        assertEquals("giorgi@example.com", response.getEmail());
    }

    @Test
    void testSetEmailUpdatesValue() {
        CustomerProfileResponse response = createCustomerProfileResponse();

        response.setEmail("luka@example.com");

        assertEquals("luka@example.com", response.getEmail());
    }

    @Test
    void testGetPhoneNumberReturnsCorrectValue() {
        CustomerProfileResponse response = createCustomerProfileResponse();

        assertEquals("555123456", response.getPhoneNumber());
    }

    @Test
    void testSetPhoneNumberUpdatesValue() {
        CustomerProfileResponse response = createCustomerProfileResponse();

        response.setPhoneNumber("599000000");

        assertEquals("599000000", response.getPhoneNumber());
    }

    @Test
    void testGetAccountsReturnsCorrectValue() {
        CustomerProfileResponse response = createCustomerProfileResponse();

        assertEquals(1, response.getAccounts().size());
    }

    @Test
    void testSetAccountsUpdatesValue() {
        CustomerProfileResponse response = createCustomerProfileResponse();
        List<AccountSummaryResponse> newAccounts = List.of();

        response.setAccounts(newAccounts);

        assertEquals(0, response.getAccounts().size());
    }
}

class CustomerSummaryResponseTest {

    private CustomerSummaryResponse createCustomerSummaryResponse() {
        return new CustomerSummaryResponse("Giorgi", "Maisuradze", "giorgi@example.com");
    }

    @Test
    void testGetFirstNameReturnsCorrectValue() {
        CustomerSummaryResponse response = createCustomerSummaryResponse();

        assertEquals("Giorgi", response.getFirstName());
    }

    @Test
    void testSetFirstNameUpdatesValue() {
        CustomerSummaryResponse response = createCustomerSummaryResponse();

        response.setFirstName("Luka");

        assertEquals("Luka", response.getFirstName());
    }

    @Test
    void testGetLastNameReturnsCorrectValue() {
        CustomerSummaryResponse response = createCustomerSummaryResponse();

        assertEquals("Maisuradze", response.getLastName());
    }

    @Test
    void testSetLastNameUpdatesValue() {
        CustomerSummaryResponse response = createCustomerSummaryResponse();

        response.setLastName("Beridze");

        assertEquals("Beridze", response.getLastName());
    }

    @Test
    void testGetEmailReturnsCorrectValue() {
        CustomerSummaryResponse response = createCustomerSummaryResponse();

        assertEquals("giorgi@example.com", response.getEmail());
    }

    @Test
    void testSetEmailUpdatesValue() {
        CustomerSummaryResponse response = createCustomerSummaryResponse();

        response.setEmail("luka@example.com");

        assertEquals("luka@example.com", response.getEmail());
    }
}

class TransactionResponseTest {

    private TransactionResponse createTransactionResponse() {
        return new TransactionResponse(TransactionType.DEPOSIT,
                LocalDateTime.of(2024, 6, 1, 10, 30, 0), new BigDecimal("250.00"),
                "Monthly payment", TransactionStatus.COMPLETE, "GEL");
    }

    @Test
    void testGetTransactionTypeReturnsCorrectValue() {
        TransactionResponse response = createTransactionResponse();

        assertEquals(TransactionType.DEPOSIT, response.getTransactionType());
    }

    @Test
    void testGetTimeStampReturnsCorrectValue() {
        TransactionResponse response = createTransactionResponse();

        assertEquals(LocalDateTime.of(2024, 6, 1, 10, 30, 0), response.getTimeStamp());
    }

    @Test
    void testSetTimeStampUpdatesValue() {
        TransactionResponse response = createTransactionResponse();
        LocalDateTime newTime = LocalDateTime.of(2025, 1, 1, 0, 0, 0);

        response.setTimeStamp(newTime);

        assertEquals(newTime, response.getTimeStamp());
    }

    @Test
    void testGetAmountReturnsCorrectValue() {
        TransactionResponse response = createTransactionResponse();

        assertEquals(new BigDecimal("250.00"), response.getAmount());
    }

    @Test
    void testSetAmountUpdatesValue() {
        TransactionResponse response = createTransactionResponse();

        response.setAmount(new BigDecimal("500.00"));

        assertEquals(new BigDecimal("500.00"), response.getAmount());
    }

    @Test
    void testGetDescriptionReturnsCorrectValue() {
        TransactionResponse response = createTransactionResponse();

        assertEquals("Monthly payment", response.getDescription());
    }

    @Test
    void testSetDescriptionUpdatesValue() {
        TransactionResponse response = createTransactionResponse();

        response.setDescription("Annual fee");

        assertEquals("Annual fee", response.getDescription());
    }

    @Test
    void testGetStatusReturnsCorrectValue() {
        TransactionResponse response = createTransactionResponse();

        assertEquals(TransactionStatus.COMPLETE, response.getStatus());
    }

    @Test
    void testSetStatusUpdatesValue() {
        TransactionResponse response = createTransactionResponse();

        response.setStatus(TransactionStatus.PENDING);

        assertEquals(TransactionStatus.PENDING, response.getStatus());
    }

    @Test
    void testGetCurrencyCodeReturnsCorrectValue() {
        TransactionResponse response = createTransactionResponse();

        assertEquals("GEL", response.getCurrencyCode());
    }

    @Test
    void testSetCurrencyCodeUpdatesValue() {
        TransactionResponse response = createTransactionResponse();

        response.setCurrencyCode("USD");

        assertEquals("USD", response.getCurrencyCode());
    }
}
