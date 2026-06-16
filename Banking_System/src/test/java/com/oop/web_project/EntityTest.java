package com.oop.web_project;



import com.oop.web_project.entities.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AccountTest {

    private Account createAccount() {
        return new Account(1L, "Main Account", AccountCategory.CHECKING,
                LocalDate.of(2022, 1, 1), true, null, null, null);
    }

    @Test
    void testGetIdReturnsCorrectValue() {
        Account account = createAccount();

        assertEquals(1L, account.getId());
    }

    @Test
    void testSetIdUpdatesValue() {
        Account account = createAccount();

        account.setId(99L);

        assertEquals(99L, account.getId());
    }

    @Test
    void testGetNameReturnsCorrectValue() {
        Account account = createAccount();

        assertEquals("Main Account", account.getName());
    }

    @Test
    void testSetNameUpdatesValue() {
        Account account = createAccount();

        account.setName("Savings Account");

        assertEquals("Savings Account", account.getName());
    }

    @Test
    void testGetCategoryReturnsCorrectValue() {
        Account account = createAccount();

        assertEquals(AccountCategory.CHECKING, account.getCategory());
    }

    @Test
    void testSetCategoryUpdatesValue() {
        Account account = createAccount();

        account.setCategory(AccountCategory.SAVINGS);

        assertEquals(AccountCategory.SAVINGS, account.getCategory());
    }

    @Test
    void testGetDateOpenedReturnsCorrectValue() {
        Account account = createAccount();

        assertEquals(LocalDate.of(2022, 1, 1), account.getDateOpened());
    }

    @Test
    void testSetDateOpenedUpdatesValue() {
        Account account = createAccount();

        account.setDateOpened(LocalDate.of(2023, 6, 15));

        assertEquals(LocalDate.of(2023, 6, 15), account.getDateOpened());
    }

    @Test
    void testIsActiveReturnsTrueByDefault() {
        Account account = createAccount();

        assertTrue(account.isActive());
    }

    @Test
    void testSetActiveUpdatesValue() {
        Account account = createAccount();

        account.setActive(false);

        assertFalse(account.isActive());
    }
}

class CardTest {

    private Card createCard() {
        CardBrand brand = new CardBrand(1L, "Visa", null);
        Account account = new Account(1L, "Main Account", AccountCategory.CHECKING,
                LocalDate.of(2022, 1, 1), true, null, null, null);
        return new Card(1L, CardType.DEBIT, brand, account, 5000,
                LocalDate.of(2027, 12, 31), "****1234", "token_abc123", true, null);
    }

    @Test
    void testGetIdReturnsCorrectValue() {
        Card card = createCard();

        assertEquals(1L, card.getId());
    }

    @Test
    void testSetIdUpdatesValue() {
        Card card = createCard();

        card.setId(55L);

        assertEquals(55L, card.getId());
    }

    @Test
    void testGetTypeReturnsCorrectValue() {
        Card card = createCard();

        assertEquals(CardType.DEBIT, card.getType());
    }

    @Test
    void testSetTypeUpdatesValue() {
        Card card = createCard();

        card.setType(CardType.CREDIT);

        assertEquals(CardType.CREDIT, card.getType());
    }

    @Test
    void testGetBrandReturnsCorrectValue() {
        Card card = createCard();

        assertEquals("Visa", card.getBrand().getName());
    }

    @Test
    void testSetBrandUpdatesValue() {
        Card card = createCard();
        CardBrand mastercard = new CardBrand(2L, "Mastercard", null);

        card.setBrand(mastercard);

        assertEquals("Mastercard", card.getBrand().getName());
    }

    @Test
    void testGetAccountReturnsCorrectValue() {
        Card card = createCard();

        assertEquals(1L, card.getAccount().getId());
    }

    @Test
    void testSetAccountUpdatesValue() {
        Card card = createCard();
        Account newAccount = new Account(2L, "Second Account", AccountCategory.SAVINGS,
                LocalDate.of(2023, 1, 1), true, null, null, null);

        card.setAccount(newAccount);

        assertEquals(2L, card.getAccount().getId());
    }

    @Test
    void testGetSpendingLimitReturnsCorrectValue() {
        Card card = createCard();

        assertEquals(5000, card.getSpendingLimit());
    }

    @Test
    void testSetSpendingLimitUpdatesValue() {
        Card card = createCard();

        card.setSpendingLimit(10000);

        assertEquals(10000, card.getSpendingLimit());
    }

    @Test
    void testGetExpirationDateReturnsCorrectValue() {
        Card card = createCard();

        assertEquals(LocalDate.of(2027, 12, 31), card.getExpirationDate());
    }

    @Test
    void testSetExpirationDateUpdatesValue() {
        Card card = createCard();

        card.setExpirationDate(LocalDate.of(2028, 6, 30));

        assertEquals(LocalDate.of(2028, 6, 30), card.getExpirationDate());
    }

    @Test
    void testGetPanMaskedReturnsCorrectValue() {
        Card card = createCard();

        assertEquals("****1234", card.getPanMasked());
    }

    @Test
    void testSetPanMaskedUpdatesValue() {
        Card card = createCard();

        card.setPanMasked("****5678");

        assertEquals("****5678", card.getPanMasked());
    }

    @Test
    void testGetPanTokenReturnsCorrectValue() {
        Card card = createCard();

        assertEquals("token_abc123", card.getPanToken());
    }

    @Test
    void testSetPanTokenUpdatesValue() {
        Card card = createCard();

        card.setPanToken("token_xyz999");

        assertEquals("token_xyz999", card.getPanToken());
    }

    @Test
    void testIsActiveReturnsTrueByDefault() {
        Card card = createCard();

        assertTrue(card.isActive());
    }

    @Test
    void testSetActiveUpdatesValue() {
        Card card = createCard();

        card.setActive(false);

        assertFalse(card.isActive());
    }
}

class CardBalanceTest {

    private CardBalance createCardBalance() {
        Card card = new Card(1L, CardType.DEBIT, null, null, 5000,
                LocalDate.of(2027, 12, 31), "****1234", "token_abc", true, null);
        Currency currency = new Currency(1L, "GEL", "Georgian Lari", null, null, null, null);
        return new CardBalance(1L, new BigDecimal("1500.00"), card, currency);
    }

    @Test
    void testGetIdReturnsCorrectValue() {
        CardBalance balance = createCardBalance();

        assertEquals(1L, balance.getId());
    }

    @Test
    void testSetIdUpdatesValue() {
        CardBalance balance = createCardBalance();

        balance.setId(5L);

        assertEquals(5L, balance.getId());
    }

    @Test
    void testGetAmountReturnsCorrectValue() {
        CardBalance balance = createCardBalance();

        assertEquals(new BigDecimal("1500.00"), balance.getAmount());
    }

    @Test
    void testSetAmountUpdatesValue() {
        CardBalance balance = createCardBalance();

        balance.setAmount(new BigDecimal("2000.00"));

        assertEquals(new BigDecimal("2000.00"), balance.getAmount());
    }

    @Test
    void testGetCardReturnsCorrectValue() {
        CardBalance balance = createCardBalance();

        assertEquals(1L, balance.getCard().getId());
    }

    @Test
    void testSetCardUpdatesValue() {
        CardBalance balance = createCardBalance();
        Card newCard = new Card(2L, CardType.CREDIT, null, null, 3000,
                LocalDate.of(2026, 6, 30), "****5678", "token_xyz", true, null);

        balance.setCard(newCard);

        assertEquals(2L, balance.getCard().getId());
    }

    @Test
    void testGetCurrencyReturnsCorrectValue() {
        CardBalance balance = createCardBalance();

        assertEquals("GEL", balance.getCurrency().getCode());
    }

    @Test
    void testSetCurrencyUpdatesValue() {
        CardBalance balance = createCardBalance();
        Currency usd = new Currency(2L, "USD", "US Dollar", null, null, null, null);

        balance.setCurrency(usd);

        assertEquals("USD", balance.getCurrency().getCode());
    }
}

class CardBrandTest {

    @Test
    void testGetIdReturnsCorrectValue() {
        CardBrand brand = new CardBrand(1L, "Visa", null);

        assertEquals(1L, brand.getId());
    }

    @Test
    void testSetIdUpdatesValue() {
        CardBrand brand = new CardBrand(1L, "Visa", null);

        brand.setId(2L);

        assertEquals(2L, brand.getId());
    }

    @Test
    void testGetNameReturnsCorrectValue() {
        CardBrand brand = new CardBrand(1L, "Visa", null);

        assertEquals("Visa", brand.getName());
    }

    @Test
    void testSetNameUpdatesValue() {
        CardBrand brand = new CardBrand(1L, "Visa", null);

        brand.setName("Mastercard");

        assertEquals("Mastercard", brand.getName());
    }
}

class CurrencyTest {

    @Test
    void testGetIdReturnsCorrectValue() {
        Currency currency = new Currency(1L, "GEL", "Georgian Lari", null, null, null, null);

        assertEquals(1L, currency.getId());
    }

    @Test
    void testSetIdUpdatesValue() {
        Currency currency = new Currency(1L, "GEL", "Georgian Lari", null, null, null, null);

        currency.setId(2L);

        assertEquals(2L, currency.getId());
    }

    @Test
    void testGetCodeReturnsCorrectValue() {
        Currency currency = new Currency(1L, "GEL", "Georgian Lari", null, null, null, null);

        assertEquals("GEL", currency.getCode());
    }

    @Test
    void testSetCodeUpdatesValue() {
        Currency currency = new Currency(1L, "GEL", "Georgian Lari", null, null, null, null);

        currency.setCode("USD");

        assertEquals("USD", currency.getCode());
    }

    @Test
    void testGetNameReturnsCorrectValue() {
        Currency currency = new Currency(1L, "GEL", "Georgian Lari", null, null, null, null);

        assertEquals("Georgian Lari", currency.getName());
    }

    @Test
    void testSetNameUpdatesValue() {
        Currency currency = new Currency(1L, "GEL", "Georgian Lari", null, null, null, null);

        currency.setName("US Dollar");

        assertEquals("US Dollar", currency.getName());
    }
}

class CurrencyExchangeTest {

    private CurrencyExchange createCurrencyExchange() {
        Currency gel = new Currency(1L, "GEL", "Georgian Lari", null, null, null, null);
        Currency usd = new Currency(2L, "USD", "US Dollar", null, null, null, null);
        return new CurrencyExchange(1L, gel, usd, new BigDecimal("0.37"),
                LocalDateTime.of(2024, 1, 1, 12, 0, 0));
    }

    @Test
    void testGetIdReturnsCorrectValue() {
        CurrencyExchange exchange = createCurrencyExchange();

        assertEquals(1L, exchange.getId());
    }

    @Test
    void testSetIdUpdatesValue() {
        CurrencyExchange exchange = createCurrencyExchange();

        exchange.setId(5L);

        assertEquals(5L, exchange.getId());
    }

    @Test
    void testGetFromReturnsCorrectValue() {
        CurrencyExchange exchange = createCurrencyExchange();

        assertEquals("GEL", exchange.getFrom().getCode());
    }

    @Test
    void testSetFromUpdatesValue() {
        CurrencyExchange exchange = createCurrencyExchange();
        Currency eur = new Currency(3L, "EUR", "Euro", null, null, null, null);

        exchange.setFrom(eur);

        assertEquals("EUR", exchange.getFrom().getCode());
    }

    @Test
    void testGetToReturnsCorrectValue() {
        CurrencyExchange exchange = createCurrencyExchange();

        assertEquals("USD", exchange.getTo().getCode());
    }

    @Test
    void testSetToUpdatesValue() {
        CurrencyExchange exchange = createCurrencyExchange();
        Currency eur = new Currency(3L, "EUR", "Euro", null, null, null, null);

        exchange.setTo(eur);

        assertEquals("EUR", exchange.getTo().getCode());
    }

    @Test
    void testGetRateReturnsCorrectValue() {
        CurrencyExchange exchange = createCurrencyExchange();

        assertEquals(new BigDecimal("0.37"), exchange.getRate());
    }

    @Test
    void testSetRateUpdatesValue() {
        CurrencyExchange exchange = createCurrencyExchange();

        exchange.setRate(new BigDecimal("0.92"));

        assertEquals(new BigDecimal("0.92"), exchange.getRate());
    }

    @Test
    void testGetTimeStampReturnsCorrectValue() {
        CurrencyExchange exchange = createCurrencyExchange();

        assertEquals(LocalDateTime.of(2024, 1, 1, 12, 0, 0), exchange.getTimeStamp());
    }

    @Test
    void testSetTimeStampUpdatesValue() {
        CurrencyExchange exchange = createCurrencyExchange();
        LocalDateTime newTime = LocalDateTime.of(2025, 6, 15, 9, 30, 0);

        exchange.setTimeStamp(newTime);

        assertEquals(newTime, exchange.getTimeStamp());
    }
}

class CustomerTest {

    private Customer createCustomer() {
        Role role = new Role(1L, RoleName.STANDARD, null);
        return new Customer(1L, "Giorgi", "Maisuradze", "555123456",
                "Tbilisi, Rustaveli 1", LocalDate.of(1995, 3, 20),
                "giorgi@example.com", "hashed_pw_123", role, null);
    }

    @Test
    void testGetIdReturnsCorrectValue() {
        Customer customer = createCustomer();

        assertEquals(1L, customer.getId());
    }

    @Test
    void testSetIdUpdatesValue() {
        Customer customer = createCustomer();

        customer.setId(10L);

        assertEquals(10L, customer.getId());
    }

    @Test
    void testGetFirstNameReturnsCorrectValue() {
        Customer customer = createCustomer();

        assertEquals("Giorgi", customer.getFirstName());
    }

    @Test
    void testSetFirstNameUpdatesValue() {
        Customer customer = createCustomer();

        customer.setFirstName("Luka");

        assertEquals("Luka", customer.getFirstName());
    }

    @Test
    void testGetLastNameReturnsCorrectValue() {
        Customer customer = createCustomer();

        assertEquals("Maisuradze", customer.getLastName());
    }

    @Test
    void testSetLastNameUpdatesValue() {
        Customer customer = createCustomer();

        customer.setLastName("Beridze");

        assertEquals("Beridze", customer.getLastName());
    }

    @Test
    void testGetPhoneNumberReturnsCorrectValue() {
        Customer customer = createCustomer();

        assertEquals("555123456", customer.getPhoneNumber());
    }

    @Test
    void testSetPhoneNumberUpdatesValue() {
        Customer customer = createCustomer();

        customer.setPhoneNumber("599000000");

        assertEquals("599000000", customer.getPhoneNumber());
    }

    @Test
    void testGetAddressReturnsCorrectValue() {
        Customer customer = createCustomer();

        assertEquals("Tbilisi, Rustaveli 1", customer.getAddress());
    }

    @Test
    void testSetAddressUpdatesValue() {
        Customer customer = createCustomer();

        customer.setAddress("Batumi, Aghmashenebeli 5");

        assertEquals("Batumi, Aghmashenebeli 5", customer.getAddress());
    }

    @Test
    void testGetDateOfBirthReturnsCorrectValue() {
        Customer customer = createCustomer();

        assertEquals(LocalDate.of(1995, 3, 20), customer.getDateOfBirth());
    }

    @Test
    void testSetDateOfBirthUpdatesValue() {
        Customer customer = createCustomer();

        customer.setDateOfBirth(LocalDate.of(2000, 1, 1));

        assertEquals(LocalDate.of(2000, 1, 1), customer.getDateOfBirth());
    }

    @Test
    void testGetEmailReturnsCorrectValue() {
        Customer customer = createCustomer();

        assertEquals("giorgi@example.com", customer.getEmail());
    }

    @Test
    void testSetEmailUpdatesValue() {
        Customer customer = createCustomer();

        customer.setEmail("luka@example.com");

        assertEquals("luka@example.com", customer.getEmail());
    }

    @Test
    void testGetHashedPasswordReturnsCorrectValue() {
        Customer customer = createCustomer();

        assertEquals("hashed_pw_123", customer.getHashedPassword());
    }

    @Test
    void testSetHashedPasswordUpdatesValue() {
        Customer customer = createCustomer();

        customer.setHashedPassword("new_hashed_pw");

        assertEquals("new_hashed_pw", customer.getHashedPassword());
    }

    @Test
    void testGetRoleReturnsCorrectValue() {
        Customer customer = createCustomer();

        assertEquals(RoleName.STANDARD, customer.getRole().getName());
    }

    @Test
    void testSetRoleUpdatesValue() {
        Customer customer = createCustomer();
        Role adminRole = new Role(2L, RoleName.ADMIN, null);

        customer.setRole(adminRole);

        assertEquals(RoleName.ADMIN, customer.getRole().getName());
    }
}

class PermissionTest {

    @Test
    void testGetIdReturnsCorrectValue() {
        Permission permission = new Permission(1L, "READ", null);

        assertEquals(1L, permission.getId());
    }

    @Test
    void testSetIdUpdatesValue() {
        Permission permission = new Permission(1L, "READ", null);

        permission.setId(2L);

        assertEquals(2L, permission.getId());
    }

    @Test
    void testGetNameReturnsCorrectValue() {
        Permission permission = new Permission(1L, "READ", null);

        assertEquals("READ", permission.getName());
    }

    @Test
    void testSetNameUpdatesValue() {
        Permission permission = new Permission(1L, "READ", null);

        permission.setName("WRITE");

        assertEquals("WRITE", permission.getName());
    }
}

class RoleTest {

    @Test
    void testGetIdReturnsCorrectValue() {
        Role role = new Role(1L, RoleName.ADMIN, null);

        assertEquals(1L, role.getId());
    }

    @Test
    void testSetIdUpdatesValue() {
        Role role = new Role(1L, RoleName.ADMIN, null);

        role.setId(3L);

        assertEquals(3L, role.getId());
    }

    @Test
    void testGetNameReturnsCorrectValue() {
        Role role = new Role(1L, RoleName.ADMIN, null);

        assertEquals(RoleName.ADMIN, role.getName());
    }

    @Test
    void testSetNameUpdatesValue() {
        Role role = new Role(1L, RoleName.ADMIN, null);

        role.setName(RoleName.MANAGER);

        assertEquals(RoleName.MANAGER, role.getName());
    }
}

class ServiceCategoryTest {

    @Test
    void testGetIdReturnsCorrectValue() {
        ServiceCategory category = new ServiceCategory(1L, "Utilities", null);

        assertEquals(1L, category.getId());
    }

    @Test
    void testSetIdUpdatesValue() {
        ServiceCategory category = new ServiceCategory(1L, "Utilities", null);

        category.setId(2L);

        assertEquals(2L, category.getId());
    }

    @Test
    void testGetNameReturnsCorrectValue() {
        ServiceCategory category = new ServiceCategory(1L, "Utilities", null);

        assertEquals("Utilities", category.getName());
    }

    @Test
    void testSetNameUpdatesValue() {
        ServiceCategory category = new ServiceCategory(1L, "Utilities", null);

        category.setName("Telecom");

        assertEquals("Telecom", category.getName());
    }
}

class ServiceProviderTest {

    private ServiceProvider createServiceProvider() {
        ServiceCategory category = new ServiceCategory(1L, "Utilities", null);
        return new ServiceProvider(1L, category, "Telasi", "https://api.telasi.ge", true, null);
    }

    @Test
    void testGetIdReturnsCorrectValue() {
        ServiceProvider provider = createServiceProvider();

        assertEquals(1L, provider.getId());
    }

    @Test
    void testSetIdUpdatesValue() {
        ServiceProvider provider = createServiceProvider();

        provider.setId(10L);

        assertEquals(10L, provider.getId());
    }

    @Test
    void testGetServiceCategoryReturnsCorrectValue() {
        ServiceProvider provider = createServiceProvider();

        assertEquals("Utilities", provider.getServiceCategory().getName());
    }

    @Test
    void testSetServiceCategoryUpdatesValue() {
        ServiceProvider provider = createServiceProvider();
        ServiceCategory telecom = new ServiceCategory(2L, "Telecom", null);

        provider.setServiceCategory(telecom);

        assertEquals("Telecom", provider.getServiceCategory().getName());
    }

    @Test
    void testGetNameReturnsCorrectValue() {
        ServiceProvider provider = createServiceProvider();

        assertEquals("Telasi", provider.getName());
    }

    @Test
    void testSetNameUpdatesValue() {
        ServiceProvider provider = createServiceProvider();

        provider.setName("Magti");

        assertEquals("Magti", provider.getName());
    }

    @Test
    void testGetApiEndpointReturnsCorrectValue() {
        ServiceProvider provider = createServiceProvider();

        assertEquals("https://api.telasi.ge", provider.getApiEndpoint());
    }

    @Test
    void testSetApiEndpointUpdatesValue() {
        ServiceProvider provider = createServiceProvider();

        provider.setApiEndpoint("https://api.magti.ge");

        assertEquals("https://api.magti.ge", provider.getApiEndpoint());
    }

    @Test
    void testIsActiveReturnsTrueByDefault() {
        ServiceProvider provider = createServiceProvider();

        assertTrue(provider.isActive());
    }

    @Test
    void testSetActiveUpdatesValue() {
        ServiceProvider provider = createServiceProvider();

        provider.setActive(false);

        assertFalse(provider.isActive());
    }
}

class TransactionTest {

    private Transaction createTransaction() {
        Currency gel = new Currency(1L, "GEL", "Georgian Lari", null, null, null, null);
        Account account = new Account(1L, "Main Account", AccountCategory.CHECKING,
                LocalDate.of(2022, 1, 1), true, null, null, null);
        TransactionType type = new TransactionType(1L, "Transfer", null);
        ServiceProvider provider = new ServiceProvider(1L,
                new ServiceCategory(1L, "Utilities", null), "Telasi", "https://api.telasi.ge", true, null);
        return new Transaction(1L, type, account,
                LocalDateTime.of(2024, 6, 1, 10, 30, 0),
                new BigDecimal("250.00"), gel, provider, null, null,
                "idem_key_001", "Monthly payment", TransactionStatus.COMPLETE);
    }

    @Test
    void testGetIdReturnsCorrectValue() {
        Transaction transaction = createTransaction();

        assertEquals(1L, transaction.getId());
    }

    @Test
    void testSetIdUpdatesValue() {
        Transaction transaction = createTransaction();

        transaction.setId(99L);

        assertEquals(99L, transaction.getId());
    }

    @Test
    void testGetTransactionTypeReturnsCorrectValue() {
        Transaction transaction = createTransaction();

        assertEquals("Transfer", transaction.getTransactionType().getName());
    }

    @Test
    void testSetTransactionTypeUpdatesValue() {
        Transaction transaction = createTransaction();
        TransactionType newType = new TransactionType(2L, "Payment", null);

        transaction.setTransactionType(newType);

        assertEquals("Payment", transaction.getTransactionType().getName());
    }

    @Test
    void testGetAccountReturnsCorrectValue() {
        Transaction transaction = createTransaction();

        assertEquals(1L, transaction.getAccount().getId());
    }

    @Test
    void testSetAccountUpdatesValue() {
        Transaction transaction = createTransaction();
        Account newAccount = new Account(2L, "Second Account", AccountCategory.SAVINGS,
                LocalDate.of(2023, 1, 1), true, null, null, null);

        transaction.setAccount(newAccount);

        assertEquals(2L, transaction.getAccount().getId());
    }

    @Test
    void testGetTimeStampReturnsCorrectValue() {
        Transaction transaction = createTransaction();

        assertEquals(LocalDateTime.of(2024, 6, 1, 10, 30, 0), transaction.getTimeStamp());
    }

    @Test
    void testSetTimeStampUpdatesValue() {
        Transaction transaction = createTransaction();
        LocalDateTime newTime = LocalDateTime.of(2025, 1, 1, 0, 0, 0);

        transaction.setTimeStamp(newTime);

        assertEquals(newTime, transaction.getTimeStamp());
    }

    @Test
    void testGetAmountReturnsCorrectValue() {
        Transaction transaction = createTransaction();

        assertEquals(new BigDecimal("250.00"), transaction.getAmount());
    }

    @Test
    void testSetAmountUpdatesValue() {
        Transaction transaction = createTransaction();

        transaction.setAmount(new BigDecimal("500.00"));

        assertEquals(new BigDecimal("500.00"), transaction.getAmount());
    }

    @Test
    void testGetCurrencyReturnsCorrectValue() {
        Transaction transaction = createTransaction();

        assertEquals("GEL", transaction.getCurrency().getCode());
    }

    @Test
    void testSetCurrencyUpdatesValue() {
        Transaction transaction = createTransaction();
        Currency usd = new Currency(2L, "USD", "US Dollar", null, null, null, null);

        transaction.setCurrency(usd);

        assertEquals("USD", transaction.getCurrency().getCode());
    }

    @Test
    void testGetServiceProviderReturnsCorrectValue() {
        Transaction transaction = createTransaction();

        assertEquals("Telasi", transaction.getServiceProvider().getName());
    }

    @Test
    void testSetServiceProviderUpdatesValue() {
        Transaction transaction = createTransaction();
        ServiceProvider newProvider = new ServiceProvider(2L,
                new ServiceCategory(2L, "Telecom", null), "Magti", "https://api.magti.ge", true, null);

        transaction.setServiceProvider(newProvider);

        assertEquals("Magti", transaction.getServiceProvider().getName());
    }

    @Test
    void testGetRelatedTransactionReturnsNullByDefault() {
        Transaction transaction = createTransaction();

        assertNull(transaction.getRelatedTransaction());
    }

    @Test
    void testSetRelatedTransactionUpdatesValue() {
        Transaction transaction = createTransaction();
        Transaction related = new Transaction();
        related.setId(2L);

        transaction.setRelatedTransaction(related);

        assertEquals(2L, transaction.getRelatedTransaction().getId());
    }

    @Test
    void testGetReverseTransactionReturnsNullByDefault() {
        Transaction transaction = createTransaction();

        assertNull(transaction.getReverseTransaction());
    }

    @Test
    void testSetReverseTransactionUpdatesValue() {
        Transaction transaction = createTransaction();
        Transaction reverse = new Transaction();
        reverse.setId(3L);

        transaction.setReverseTransaction(reverse);

        assertEquals(3L, transaction.getReverseTransaction().getId());
    }

    @Test
    void testGetIdempotencyKeyReturnsCorrectValue() {
        Transaction transaction = createTransaction();

        assertEquals("idem_key_001", transaction.getIdempotencyKey());
    }

    @Test
    void testSetIdempotencyKeyUpdatesValue() {
        Transaction transaction = createTransaction();

        transaction.setIdempotencyKey("idem_key_999");

        assertEquals("idem_key_999", transaction.getIdempotencyKey());
    }

    @Test
    void testGetDescriptionReturnsCorrectValue() {
        Transaction transaction = createTransaction();

        assertEquals("Monthly payment", transaction.getDescription());
    }

    @Test
    void testSetDescriptionUpdatesValue() {
        Transaction transaction = createTransaction();

        transaction.setDescription("Annual fee");

        assertEquals("Annual fee", transaction.getDescription());
    }

    @Test
    void testGetStatusReturnsCorrectValue() {
        Transaction transaction = createTransaction();

        assertEquals(TransactionStatus.COMPLETE, transaction.getStatus());
    }

    @Test
    void testSetStatusUpdatesValue() {
        Transaction transaction = createTransaction();

        transaction.setStatus(TransactionStatus.PENDING);

        assertEquals(TransactionStatus.PENDING, transaction.getStatus());
    }
}

class TransactionTypeTest {

    @Test
    void testGetIdReturnsCorrectValue() {
        TransactionType type = new TransactionType(1L, "Transfer", null);

        assertEquals(1L, type.getId());
    }

    @Test
    void testSetIdUpdatesValue() {
        TransactionType type = new TransactionType(1L, "Transfer", null);

        type.setId(2L);

        assertEquals(2L, type.getId());
    }

    @Test
    void testGetNameReturnsCorrectValue() {
        TransactionType type = new TransactionType(1L, "Transfer", null);

        assertEquals("Transfer", type.getName());
    }

    @Test
    void testSetNameUpdatesValue() {
        TransactionType type = new TransactionType(1L, "Transfer", null);

        type.setName("Payment");

        assertEquals("Payment", type.getName());
    }
}


