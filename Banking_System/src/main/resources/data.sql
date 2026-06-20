-- ------------------------------------------------------------
--  1. SERVICE CATEGORIES
-- ------------------------------------------------------------
INSERT INTO Service_categories (Service_category_id, Service_category_name) VALUES (1, 'TELECOM');
INSERT INTO Service_categories (Service_category_id, Service_category_name) VALUES (2, 'UTILITY');
INSERT INTO Service_categories (Service_category_id, Service_category_name) VALUES (3, 'INSURANCE');
INSERT INTO Service_categories (Service_category_id, Service_category_name) VALUES (4, 'GOVERNMENT');
INSERT INTO Service_categories (Service_category_id, Service_category_name) VALUES (5, 'ENTERTAINMENT');
INSERT INTO Service_categories (Service_category_id, Service_category_name) VALUES (6, 'RETAIL');


-- ------------------------------------------------------------
--  2. SERVICE PROVIDERS
--     FK resolved via subquery on unique Service_category_name
-- ------------------------------------------------------------
INSERT INTO Service_providers (Service_provider_id, Service_category_id, Service_provider_name, Api_endpoint, Is_active)
SELECT 1, Service_category_id, 'Magti',            'https://api.magticom.ge/payments',   TRUE  FROM Service_categories WHERE Service_category_name = 'TELECOM';

INSERT INTO Service_providers (Service_provider_id, Service_category_id, Service_provider_name, Api_endpoint, Is_active)
SELECT 2, Service_category_id, 'Silknet',          'https://api.silknet.com/payments',   TRUE  FROM Service_categories WHERE Service_category_name = 'TELECOM';

INSERT INTO Service_providers (Service_provider_id, Service_category_id, Service_provider_name, Api_endpoint, Is_active)
SELECT 3, Service_category_id, 'Telasi',           'https://api.telasi.ge/payments',     TRUE  FROM Service_categories WHERE Service_category_name = 'UTILITY';

INSERT INTO Service_providers (Service_provider_id, Service_category_id, Service_provider_name, Api_endpoint, Is_active)
SELECT 4, Service_category_id, 'GWP',              'https://api.gwp.ge/payments',        TRUE  FROM Service_categories WHERE Service_category_name = 'UTILITY';

INSERT INTO Service_providers (Service_provider_id, Service_category_id, Service_provider_name, Api_endpoint, Is_active)
SELECT 5, Service_category_id, 'Aldagi',           'https://api.aldagi.ge/payments',     TRUE  FROM Service_categories WHERE Service_category_name = 'INSURANCE';

INSERT INTO Service_providers (Service_provider_id, Service_category_id, Service_provider_name, Api_endpoint, Is_active)
SELECT 6, Service_category_id, 'Revenue Service',  'https://api.rs.ge/payments',         TRUE  FROM Service_categories WHERE Service_category_name = 'GOVERNMENT';

INSERT INTO Service_providers (Service_provider_id, Service_category_id, Service_provider_name, Api_endpoint, Is_active)
SELECT 7, Service_category_id, 'Netflix',          'https://api.netflix.com/payments',   TRUE  FROM Service_categories WHERE Service_category_name = 'ENTERTAINMENT';

INSERT INTO Service_providers (Service_provider_id, Service_category_id, Service_provider_name, Api_endpoint, Is_active)
SELECT 8, Service_category_id, 'Carrefour',        'https://api.carrefour.ge/payments',  FALSE FROM Service_categories WHERE Service_category_name = 'RETAIL';


-- ------------------------------------------------------------
--  3. CURRENCIES
-- ------------------------------------------------------------
INSERT INTO Currencies (Currency_id, Currency_code, Currency_name) VALUES (1, 'GEL', 'Georgian Lari');
INSERT INTO Currencies (Currency_id, Currency_code, Currency_name) VALUES (2, 'USD', 'US Dollar');
INSERT INTO Currencies (Currency_id, Currency_code, Currency_name) VALUES (3, 'EUR', 'Euro');
INSERT INTO Currencies (Currency_id, Currency_code, Currency_name) VALUES (4, 'GBP', 'British Pound');


-- ------------------------------------------------------------
--  4. CURRENCY EXCHANGES  (all 12 cross-pairs)
--     FKs resolved via subquery on unique Currency_code
-- ------------------------------------------------------------
INSERT INTO Currency_exchanges (Currency_exchange_id, From_currency_id, To_currency_id, Exchange_rate, Exchange_time_stamp)
SELECT  1, f.Currency_id, t.Currency_id, 2.7200, '2025-06-01 08:00:00' FROM Currencies f, Currencies t WHERE f.Currency_code='USD' AND t.Currency_code='GEL';

INSERT INTO Currency_exchanges (Currency_exchange_id, From_currency_id, To_currency_id, Exchange_rate, Exchange_time_stamp)
SELECT  2, f.Currency_id, t.Currency_id, 0.3676, '2025-06-01 08:00:00' FROM Currencies f, Currencies t WHERE f.Currency_code='GEL' AND t.Currency_code='USD';

INSERT INTO Currency_exchanges (Currency_exchange_id, From_currency_id, To_currency_id, Exchange_rate, Exchange_time_stamp)
SELECT  3, f.Currency_id, t.Currency_id, 2.9400, '2025-06-01 08:00:00' FROM Currencies f, Currencies t WHERE f.Currency_code='EUR' AND t.Currency_code='GEL';

INSERT INTO Currency_exchanges (Currency_exchange_id, From_currency_id, To_currency_id, Exchange_rate, Exchange_time_stamp)
SELECT  4, f.Currency_id, t.Currency_id, 0.3401, '2025-06-01 08:00:00' FROM Currencies f, Currencies t WHERE f.Currency_code='GEL' AND t.Currency_code='EUR';

INSERT INTO Currency_exchanges (Currency_exchange_id, From_currency_id, To_currency_id, Exchange_rate, Exchange_time_stamp)
SELECT  5, f.Currency_id, t.Currency_id, 3.4500, '2025-06-01 08:00:00' FROM Currencies f, Currencies t WHERE f.Currency_code='GBP' AND t.Currency_code='GEL';

INSERT INTO Currency_exchanges (Currency_exchange_id, From_currency_id, To_currency_id, Exchange_rate, Exchange_time_stamp)
SELECT  6, f.Currency_id, t.Currency_id, 0.2899, '2025-06-01 08:00:00' FROM Currencies f, Currencies t WHERE f.Currency_code='GEL' AND t.Currency_code='GBP';

INSERT INTO Currency_exchanges (Currency_exchange_id, From_currency_id, To_currency_id, Exchange_rate, Exchange_time_stamp)
SELECT  7, f.Currency_id, t.Currency_id, 1.0820, '2025-06-01 08:00:00' FROM Currencies f, Currencies t WHERE f.Currency_code='USD' AND t.Currency_code='EUR';

INSERT INTO Currency_exchanges (Currency_exchange_id, From_currency_id, To_currency_id, Exchange_rate, Exchange_time_stamp)
SELECT  8, f.Currency_id, t.Currency_id, 0.9242, '2025-06-01 08:00:00' FROM Currencies f, Currencies t WHERE f.Currency_code='EUR' AND t.Currency_code='USD';

INSERT INTO Currency_exchanges (Currency_exchange_id, From_currency_id, To_currency_id, Exchange_rate, Exchange_time_stamp)
SELECT  9, f.Currency_id, t.Currency_id, 1.2700, '2025-06-01 08:00:00' FROM Currencies f, Currencies t WHERE f.Currency_code='USD' AND t.Currency_code='GBP';

INSERT INTO Currency_exchanges (Currency_exchange_id, From_currency_id, To_currency_id, Exchange_rate, Exchange_time_stamp)
SELECT 10, f.Currency_id, t.Currency_id, 0.7874, '2025-06-01 08:00:00' FROM Currencies f, Currencies t WHERE f.Currency_code='GBP' AND t.Currency_code='USD';

INSERT INTO Currency_exchanges (Currency_exchange_id, From_currency_id, To_currency_id, Exchange_rate, Exchange_time_stamp)
SELECT 11, f.Currency_id, t.Currency_id, 1.1770, '2025-06-01 08:00:00' FROM Currencies f, Currencies t WHERE f.Currency_code='EUR' AND t.Currency_code='GBP';

INSERT INTO Currency_exchanges (Currency_exchange_id, From_currency_id, To_currency_id, Exchange_rate, Exchange_time_stamp)
SELECT 12, f.Currency_id, t.Currency_id, 0.8496, '2025-06-01 08:00:00' FROM Currencies f, Currencies t WHERE f.Currency_code='GBP' AND t.Currency_code='EUR';


-- ------------------------------------------------------------
--  5. CUSTOMERS  (1 ADMIN · 2 MANAGER · 9 STANDARD)
--     Role is a @Enumerated(STRING) column on Customer itself
--     (no separate Roles table any more), so it is inserted directly.
-- ------------------------------------------------------------
INSERT INTO Customers (Customer_id, First_name, Last_name, Phone_number, Address, Date_of_birth, Email, Hashed_password, Is_active, Role) VALUES
    ( 1,'Alice',  'Johnson', '+995551001001', '1 Rustaveli Ave, Tbilisi',       '1985-03-15', 'alice.johnson@example.com',  '$2a$10$adminHash001',     TRUE,  'ADMIN');

INSERT INTO Customers (Customer_id, First_name, Last_name, Phone_number, Address, Date_of_birth, Email, Hashed_password, Is_active, Role) VALUES
    ( 2,'Bob',    'Smith',   '+995551001002', '22 Chavchavadze Ave, Tbilisi',   '1990-07-22', 'bob.smith@example.com',      '$2a$10$managerHash001',   TRUE,  'MANAGER');

INSERT INTO Customers (Customer_id, First_name, Last_name, Phone_number, Address, Date_of_birth, Email, Hashed_password, Is_active, Role) VALUES
    ( 3,'Carol',  'White',   '+995551001003', '5 Agmashenebeli Ave, Tbilisi',   '1992-11-30', 'carol.white@example.com',    '$2a$10$standardHash001',  TRUE,  'STANDARD');

INSERT INTO Customers (Customer_id, First_name, Last_name, Phone_number, Address, Date_of_birth, Email, Hashed_password, Is_active, Role) VALUES
    ( 4,'David',  'Brown',   '+995551001004', '18 Kostava St, Tbilisi',         '1988-05-14', 'david.brown@example.com',    '$2a$10$standardHash002',  TRUE,  'STANDARD');

INSERT INTO Customers (Customer_id, First_name, Last_name, Phone_number, Address, Date_of_birth, Email, Hashed_password, Is_active, Role) VALUES
    ( 5,'Emma',   'Davis',   '+995551001005', '7 Freedom Square, Tbilisi',      '1995-09-03', 'emma.davis@example.com',     '$2a$10$standardHash003',  TRUE,  'STANDARD');

INSERT INTO Customers (Customer_id, First_name, Last_name, Phone_number, Address, Date_of_birth, Email, Hashed_password, Is_active, Role) VALUES
    ( 6,'Frank',  'Miller',  '+995551001006', '33 Pekini Ave, Tbilisi',         '1987-12-19', 'frank.miller@example.com',   '$2a$10$standardHash004',  TRUE,  'STANDARD');

INSERT INTO Customers (Customer_id, First_name, Last_name, Phone_number, Address, Date_of_birth, Email, Hashed_password, Is_active, Role) VALUES
    ( 7,'Grace',  'Wilson',  '+995551001007', '9 Marjanishvili St, Tbilisi',    '1993-04-27', 'grace.wilson@example.com',   '$2a$10$standardHash005',  TRUE,  'STANDARD');

INSERT INTO Customers (Customer_id, First_name, Last_name, Phone_number, Address, Date_of_birth, Email, Hashed_password, Is_active, Role) VALUES
    ( 8,'Henry',  'Moore',   '+995551001008', '45 Vake Park Rd, Tbilisi',       '1980-08-11', 'henry.moore@example.com',    '$2a$10$standardHash006',  FALSE, 'STANDARD');

INSERT INTO Customers (Customer_id, First_name, Last_name, Phone_number, Address, Date_of_birth, Email, Hashed_password, Is_active, Role) VALUES
    ( 9,'Iris',   'Taylor',  '+995551001009', '2 Saburtalo St, Tbilisi',        '1991-01-05', 'iris.taylor@example.com',    '$2a$10$managerHash002',   TRUE,  'MANAGER');

INSERT INTO Customers (Customer_id, First_name, Last_name, Phone_number, Address, Date_of_birth, Email, Hashed_password, Is_active, Role) VALUES
    (10,'Jack',   'Anderson','+995551001010', '11 Isani St, Tbilisi',           '1997-06-18', 'jack.anderson@example.com',  '$2a$10$standardHash007',  TRUE,  'STANDARD');

INSERT INTO Customers (Customer_id, First_name, Last_name, Phone_number, Address, Date_of_birth, Email, Hashed_password, Is_active, Role) VALUES
    (11,'Karen',  'Thomas',  '+995551001011', '66 Gldani Rd, Tbilisi',          '1986-10-29', 'karen.thomas@example.com',   '$2a$10$standardHash008',  TRUE,  'STANDARD');

INSERT INTO Customers (Customer_id, First_name, Last_name, Phone_number, Address, Date_of_birth, Email, Hashed_password, Is_active, Role) VALUES
    (12,'Liam',   'Jackson', '+995551001012', '3 Nadzaladevi Blvd, Tbilisi',    '1999-02-14', 'liam.jackson@example.com',   '$2a$10$standardHash009',  TRUE,  'STANDARD');


-- ------------------------------------------------------------
--  6. ACCOUNTS
-- ------------------------------------------------------------
INSERT INTO Accounts (Account_id, Account_name, Account_category, Date_opened, Is_active) VALUES  (1,  'Alice Main Checking',   'CHECKING', '2020-01-10', TRUE);
INSERT INTO Accounts (Account_id, Account_name, Account_category, Date_opened, Is_active) VALUES  (2,  'Alice Savings',          'SAVINGS',  '2020-01-10', TRUE);
INSERT INTO Accounts (Account_id, Account_name, Account_category, Date_opened, Is_active) VALUES  (3,  'Bob Checking',           'CHECKING', '2019-06-15', TRUE);
INSERT INTO Accounts (Account_id, Account_name, Account_category, Date_opened, Is_active) VALUES  (4,  'Carol Checking',         'CHECKING', '2021-03-22', TRUE);
INSERT INTO Accounts (Account_id, Account_name, Account_category, Date_opened, Is_active) VALUES  (5,  'David Savings',          'SAVINGS',  '2018-11-05', TRUE);
INSERT INTO Accounts (Account_id, Account_name, Account_category, Date_opened, Is_active) VALUES  (6,  'Emma Credit',            'CREDIT',   '2022-07-01', TRUE);
INSERT INTO Accounts (Account_id, Account_name, Account_category, Date_opened, Is_active) VALUES  (7,  'Frank Checking',         'CHECKING', '2017-09-30', TRUE);
INSERT INTO Accounts (Account_id, Account_name, Account_category, Date_opened, Is_active) VALUES  (8,  'Shared Family Account',  'CHECKING', '2023-01-01', TRUE);   -- Carol + David
INSERT INTO Accounts (Account_id, Account_name, Account_category, Date_opened, Is_active) VALUES  (9,  'Business Account',       'CHECKING', '2021-08-14', TRUE);   -- Bob + Iris
INSERT INTO Accounts (Account_id, Account_name, Account_category, Date_opened, Is_active) VALUES (10,  'Grace Savings',          'SAVINGS',  '2020-05-17', TRUE);
INSERT INTO Accounts (Account_id, Account_name, Account_category, Date_opened, Is_active) VALUES (11,  'Jack Credit',            'CREDIT',   '2023-09-10', TRUE);
INSERT INTO Accounts (Account_id, Account_name, Account_category, Date_opened, Is_active) VALUES (12,  'Karen Checking',         'CHECKING', '2016-04-20', FALSE);


-- ------------------------------------------------------------
-- 7. ACCOUNT_CUSTOMER  (join table for the Account <-> Customer
--    many-to-many – no surrogate key)
--    individual ownership + two shared accounts
-- ------------------------------------------------------------
-- Individual
INSERT INTO Account_customer (Account_id, Customer_id) VALUES  (1,  1);   -- Alice      -> Alice Main Checking
INSERT INTO Account_customer (Account_id, Customer_id) VALUES  (2,  1);   -- Alice      -> Alice Savings
INSERT INTO Account_customer (Account_id, Customer_id) VALUES  (3,  2);   -- Bob        -> Bob Checking
INSERT INTO Account_customer (Account_id, Customer_id) VALUES  (4,  3);   -- Carol      -> Carol Checking
INSERT INTO Account_customer (Account_id, Customer_id) VALUES  (5,  4);   -- David      -> David Savings
INSERT INTO Account_customer (Account_id, Customer_id) VALUES  (6,  5);   -- Emma       -> Emma Credit
INSERT INTO Account_customer (Account_id, Customer_id) VALUES  (7,  6);   -- Frank      -> Frank Checking
INSERT INTO Account_customer (Account_id, Customer_id) VALUES (10,  7);   -- Grace      -> Grace Savings
INSERT INTO Account_customer (Account_id, Customer_id) VALUES (11, 10);   -- Jack       -> Jack Credit
INSERT INTO Account_customer (Account_id, Customer_id) VALUES (12, 11);   -- Karen      -> Karen Checking
-- Shared
INSERT INTO Account_customer (Account_id, Customer_id) VALUES  (8,  3);   -- Carol      -> Shared Family Account
INSERT INTO Account_customer (Account_id, Customer_id) VALUES  (8,  4);   -- David      -> Shared Family Account
INSERT INTO Account_customer (Account_id, Customer_id) VALUES  (9,  2);   -- Bob        -> Business Account
INSERT INTO Account_customer (Account_id, Customer_id) VALUES  (9,  9);   -- Iris       -> Business Account


-- ------------------------------------------------------------
-- 8. CARDS
--    Brand is a @Enumerated(STRING) column on Card itself
--    (CardBrand only has MASTERCARD / VISA now, no separate
--    Card_brands table), so it is inserted directly.
-- ------------------------------------------------------------
INSERT INTO Cards (Card_id, Card_type, Brand, Account_id, Spending_limit, Expiration_date, Pan_masked,            Pan_token,               Is_active) VALUES
    ( 1, 'DEBIT',  'VISA',       1,  5000, '2027-12-31', '4111 **** **** 1001', 'tok_visa_debit_1001', TRUE);

INSERT INTO Cards (Card_id, Card_type, Brand, Account_id, Spending_limit, Expiration_date, Pan_masked,            Pan_token,               Is_active) VALUES
    ( 2, 'CREDIT', 'VISA',       2, 10000, '2026-09-30', '4111 **** **** 1002', 'tok_visa_cred_1002',  TRUE);

INSERT INTO Cards (Card_id, Card_type, Brand, Account_id, Spending_limit, Expiration_date, Pan_masked,            Pan_token,               Is_active) VALUES
    ( 3, 'DEBIT',  'MASTERCARD', 3,  3000, '2028-03-31', '5555 **** **** 1003', 'tok_mc_debit_1003',   TRUE);

INSERT INTO Cards (Card_id, Card_type, Brand, Account_id, Spending_limit, Expiration_date, Pan_masked,            Pan_token,               Is_active) VALUES
    ( 4, 'CREDIT', 'MASTERCARD', 4,  7000, '2027-06-30', '5555 **** **** 1004', 'tok_mc_cred_1004',    TRUE);

INSERT INTO Cards (Card_id, Card_type, Brand, Account_id, Spending_limit, Expiration_date, Pan_masked,            Pan_token,               Is_active) VALUES
    ( 5, 'DEBIT',  'VISA',       5,  2000, '2026-12-31', '4111 **** **** 1005', 'tok_visa_debit_1005', TRUE);

INSERT INTO Cards (Card_id, Card_type, Brand, Account_id, Spending_limit, Expiration_date, Pan_masked,            Pan_token,               Is_active) VALUES
    ( 6, 'CREDIT', 'MASTERCARD', 6, 15000, '2028-11-30', '5555 **** **** 1006', 'tok_mc_cred_1006',    TRUE);

INSERT INTO Cards (Card_id, Card_type, Brand, Account_id, Spending_limit, Expiration_date, Pan_masked,            Pan_token,               Is_active) VALUES
    ( 7, 'DEBIT',  'MASTERCARD', 7,  4000, '2027-08-31', '5555 **** **** 1007', 'tok_mc_debit_1007',   FALSE);

INSERT INTO Cards (Card_id, Card_type, Brand, Account_id, Spending_limit, Expiration_date, Pan_masked,            Pan_token,               Is_active) VALUES
    ( 8, 'DEBIT',  'VISA',       8,  6000, '2027-01-31', '4111 **** **** 1008', 'tok_visa_debit_1008', TRUE);

INSERT INTO Cards (Card_id, Card_type, Brand, Account_id, Spending_limit, Expiration_date, Pan_masked,            Pan_token,               Is_active) VALUES
    ( 9, 'DEBIT',  'MASTERCARD', 9,  8000, '2028-07-31', '5555 **** **** 1009', 'tok_mc_debit_1009',   TRUE);

INSERT INTO Cards (Card_id, Card_type, Brand, Account_id, Spending_limit, Expiration_date, Pan_masked,            Pan_token,               Is_active) VALUES
    (10, 'CREDIT', 'VISA',      10,  5000, '2026-06-30', '4111 **** **** 1010', 'tok_visa_cred_1010',  TRUE);

INSERT INTO Cards (Card_id, Card_type, Brand, Account_id, Spending_limit, Expiration_date, Pan_masked,            Pan_token,               Is_active) VALUES
    (11, 'CREDIT', 'VISA',      11, 20000, '2029-02-28', '4111 **** **** 1011', 'tok_visa_cred_1011',  TRUE);

INSERT INTO Cards (Card_id, Card_type, Brand, Account_id, Spending_limit, Expiration_date, Pan_masked,            Pan_token,               Is_active) VALUES
    (12, 'DEBIT',  'MASTERCARD', 1,  3000, '2027-05-31', '5555 **** **** 1012', 'tok_mc_debit_1012',   TRUE);  -- Alice 2nd card


-- ------------------------------------------------------------
-- 9. CARD BALANCES
--    Currency_id resolved via subquery on unique Currency_code
-- ------------------------------------------------------------
-- Card 1  (Alice Main Checking - Visa Debit) : GEL + USD
INSERT INTO Card_balances (Card_balance_id, Card_balance_amount, Card_id, Currency_id) SELECT  1, 4250.75, 1, Currency_id FROM Currencies WHERE Currency_code = 'GEL';
INSERT INTO Card_balances (Card_balance_id, Card_balance_amount, Card_id, Currency_id) SELECT  2,  320.00, 1, Currency_id FROM Currencies WHERE Currency_code = 'USD';

-- Card 2  (Alice Savings - Visa Credit) : GEL + EUR
INSERT INTO Card_balances (Card_balance_id, Card_balance_amount, Card_id, Currency_id) SELECT  3, 8500.00, 2, Currency_id FROM Currencies WHERE Currency_code = 'GEL';
INSERT INTO Card_balances (Card_balance_id, Card_balance_amount, Card_id, Currency_id) SELECT  4,   850.50, 2, Currency_id FROM Currencies WHERE Currency_code = 'EUR';

-- Card 3  (Bob Checking - MC Debit) : GEL
INSERT INTO Card_balances (Card_balance_id, Card_balance_amount, Card_id, Currency_id) SELECT  5, 2450.00, 3, Currency_id FROM Currencies WHERE Currency_code = 'GEL';

-- Card 4  (Carol Checking - MC Credit) : GEL + USD
INSERT INTO Card_balances (Card_balance_id, Card_balance_amount, Card_id, Currency_id) SELECT  6, 2100.00, 4, Currency_id FROM Currencies WHERE Currency_code = 'GEL';
INSERT INTO Card_balances (Card_balance_id, Card_balance_amount, Card_id, Currency_id) SELECT  7,  175.25, 4, Currency_id FROM Currencies WHERE Currency_code = 'USD';

-- Card 5  (David Savings - Visa Debit) : GEL
INSERT INTO Card_balances (Card_balance_id, Card_balance_amount, Card_id, Currency_id) SELECT  8, 1650.00, 5, Currency_id FROM Currencies WHERE Currency_code = 'GEL';

-- Card 6  (Emma Credit - MC Credit) : GEL + GBP
INSERT INTO Card_balances (Card_balance_id, Card_balance_amount, Card_id, Currency_id) SELECT  9, 1200.00, 6, Currency_id FROM Currencies WHERE Currency_code = 'GEL';
INSERT INTO Card_balances (Card_balance_id, Card_balance_amount, Card_id, Currency_id) SELECT 10,  400.00, 6, Currency_id FROM Currencies WHERE Currency_code = 'GBP';

-- Card 8  (Shared Family - Visa Debit) : GEL + USD
INSERT INTO Card_balances (Card_balance_id, Card_balance_amount, Card_id, Currency_id) SELECT 11, 4800.00, 8, Currency_id FROM Currencies WHERE Currency_code = 'GEL';
INSERT INTO Card_balances (Card_balance_id, Card_balance_amount, Card_id, Currency_id) SELECT 12,  600.00, 8, Currency_id FROM Currencies WHERE Currency_code = 'USD';

-- Card 9  (Business - MC Debit) : GEL + USD + EUR
INSERT INTO Card_balances (Card_balance_id, Card_balance_amount, Card_id, Currency_id) SELECT 13, 2600.00, 9, Currency_id FROM Currencies WHERE Currency_code = 'GEL';
INSERT INTO Card_balances (Card_balance_id, Card_balance_amount, Card_id, Currency_id) SELECT 14,  3200.00, 9, Currency_id FROM Currencies WHERE Currency_code = 'USD';
INSERT INTO Card_balances (Card_balance_id, Card_balance_amount, Card_id, Currency_id) SELECT 15,  1800.00, 9, Currency_id FROM Currencies WHERE Currency_code = 'EUR';

-- Card 10 (Grace Savings - Visa Credit) : GEL
INSERT INTO Card_balances (Card_balance_id, Card_balance_amount, Card_id, Currency_id) SELECT 16, 4200.00, 10, Currency_id FROM Currencies WHERE Currency_code = 'GEL';

-- Card 11 (Jack Credit - Visa Credit) : GEL + USD + GBP
INSERT INTO Card_balances (Card_balance_id, Card_balance_amount, Card_id, Currency_id) SELECT 17, 3500.00, 11, Currency_id FROM Currencies WHERE Currency_code = 'GEL';
INSERT INTO Card_balances (Card_balance_id, Card_balance_amount, Card_id, Currency_id) SELECT 18,  220.00, 11, Currency_id FROM Currencies WHERE Currency_code = 'USD';
INSERT INTO Card_balances (Card_balance_id, Card_balance_amount, Card_id, Currency_id) SELECT 19,  150.00, 11, Currency_id FROM Currencies WHERE Currency_code = 'GBP';

-- Card 12 (Alice 2nd card - MC Debit) : GEL
INSERT INTO Card_balances (Card_balance_id, Card_balance_amount, Card_id, Currency_id) SELECT 20, 800.00, 12, Currency_id FROM Currencies WHERE Currency_code = 'GEL';

-- ------------------------------------------------------------
-- 10. Advance all sequences past the seeded data range
--     (Role and CardBrand are now plain enum columns, not their
--     own tables, so roles_seq / card_brands_seq / permissions_seq
--     no longer apply.)
-- ------------------------------------------------------------
ALTER SEQUENCE accounts_seq RESTART WITH 1000;
ALTER SEQUENCE customers_seq RESTART WITH 1000;
ALTER SEQUENCE cards_seq RESTART WITH 1000;
ALTER SEQUENCE card_balances_seq RESTART WITH 1000;
ALTER SEQUENCE currencies_seq RESTART WITH 1000;
ALTER SEQUENCE currency_exchanges_seq RESTART WITH 1000;
ALTER SEQUENCE service_categories_seq RESTART WITH 1000;
ALTER SEQUENCE service_providers_seq RESTART WITH 1000;
ALTER SEQUENCE transactions_seq RESTART WITH 1000;