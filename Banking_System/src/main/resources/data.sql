

-- ------------------------------------------------------------
--  1. ROLES
-- ------------------------------------------------------------
INSERT INTO Roles (Role_id, Role_name) VALUES (1, 'ADMIN');
INSERT INTO Roles (Role_id, Role_name) VALUES (2, 'MANAGER');
INSERT INTO Roles (Role_id, Role_name) VALUES (3, 'STANDARD');


-- ------------------------------------------------------------
--  2. PERMISSIONS
-- ------------------------------------------------------------
INSERT INTO Permissions (Permission_id, Permission_name) VALUES (1, 'VIEW_ACCOUNTS');
INSERT INTO Permissions (Permission_id, Permission_name) VALUES (2, 'MANAGE_ACCOUNTS');
INSERT INTO Permissions (Permission_id, Permission_name) VALUES (3, 'VIEW_TRANSACTIONS');
INSERT INTO Permissions (Permission_id, Permission_name) VALUES (4, 'CREATE_TRANSACTIONS');
INSERT INTO Permissions (Permission_id, Permission_name) VALUES (5, 'MANAGE_TRANSACTIONS');
INSERT INTO Permissions (Permission_id, Permission_name) VALUES (6, 'MANAGE_USERS');
INSERT INTO Permissions (Permission_id, Permission_name) VALUES (7, 'VIEW_REPORTS');
INSERT INTO Permissions (Permission_id, Permission_name) VALUES (8, 'MANAGE_SYSTEM');


-- ------------------------------------------------------------
--  3. ROLE_PERMISSION  (join table – no surrogate key)
-- ------------------------------------------------------------
-- ADMIN receives every permission
INSERT INTO Role_permission (Role_id, Permission_id)
SELECT r.Role_id, p.Permission_id
FROM   Roles r, Permissions p
WHERE  r.Role_name = 'ADMIN';

-- MANAGER
INSERT INTO Role_permission (Role_id, Permission_id)
SELECT r.Role_id, p.Permission_id
FROM   Roles r, Permissions p
WHERE  r.Role_name = 'MANAGER'
  AND  p.Permission_name IN (
                             'VIEW_ACCOUNTS', 'MANAGE_ACCOUNTS',
                             'VIEW_TRANSACTIONS', 'CREATE_TRANSACTIONS',
                             'MANAGE_TRANSACTIONS', 'VIEW_REPORTS');

-- STANDARD
INSERT INTO Role_permission (Role_id, Permission_id)
SELECT r.Role_id, p.Permission_id
FROM   Roles r, Permissions p
WHERE  r.Role_name = 'STANDARD'
  AND  p.Permission_name IN (
                             'VIEW_ACCOUNTS', 'VIEW_TRANSACTIONS', 'CREATE_TRANSACTIONS');


-- ------------------------------------------------------------
--  4. SERVICE CATEGORIES
-- ------------------------------------------------------------
INSERT INTO Service_categories (Service_category_id, Service_category_name) VALUES (1, 'TELECOM');
INSERT INTO Service_categories (Service_category_id, Service_category_name) VALUES (2, 'UTILITY');
INSERT INTO Service_categories (Service_category_id, Service_category_name) VALUES (3, 'INSURANCE');
INSERT INTO Service_categories (Service_category_id, Service_category_name) VALUES (4, 'GOVERNMENT');
INSERT INTO Service_categories (Service_category_id, Service_category_name) VALUES (5, 'ENTERTAINMENT');
INSERT INTO Service_categories (Service_category_id, Service_category_name) VALUES (6, 'RETAIL');


-- ------------------------------------------------------------
--  5. SERVICE PROVIDERS
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
--  6. CURRENCIES
-- ------------------------------------------------------------
INSERT INTO Currencies (Currency_id, Currency_code, Currency_name) VALUES (1, 'GEL', 'Georgian Lari');
INSERT INTO Currencies (Currency_id, Currency_code, Currency_name) VALUES (2, 'USD', 'US Dollar');
INSERT INTO Currencies (Currency_id, Currency_code, Currency_name) VALUES (3, 'EUR', 'Euro');
INSERT INTO Currencies (Currency_id, Currency_code, Currency_name) VALUES (4, 'GBP', 'British Pound');


-- ------------------------------------------------------------
--  7. CURRENCY EXCHANGES  (all 12 cross-pairs)
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
--  8. CUSTOMERS  (1 ADMIN · 2 MANAGER · 9 STANDARD)
--     FK resolved via subquery on unique Role_name
-- ------------------------------------------------------------
INSERT INTO Customers (Customer_id, First_name, Last_name, Phone_number, Address, Date_of_birth, Email, Hashed_password, Is_active, Role_id)
SELECT  1,'Alice',  'Johnson', '+995551001001', '1 Rustaveli Ave, Tbilisi',       '1985-03-15', 'alice.johnson@example.com',  '$2a$10$adminHash001',     TRUE,  Role_id FROM Roles WHERE Role_name='ADMIN';

INSERT INTO Customers (Customer_id, First_name, Last_name, Phone_number, Address, Date_of_birth, Email, Hashed_password, Is_active, Role_id)
SELECT  2,'Bob',    'Smith',   '+995551001002', '22 Chavchavadze Ave, Tbilisi',    '1990-07-22', 'bob.smith@example.com',      '$2a$10$managerHash001',   TRUE,  Role_id FROM Roles WHERE Role_name='MANAGER';

INSERT INTO Customers (Customer_id, First_name, Last_name, Phone_number, Address, Date_of_birth, Email, Hashed_password, Is_active, Role_id)
SELECT  3,'Carol',  'White',   '+995551001003', '5 Agmashenebeli Ave, Tbilisi',    '1992-11-30', 'carol.white@example.com',    '$2a$10$standardHash001',  TRUE,  Role_id FROM Roles WHERE Role_name='STANDARD';

INSERT INTO Customers (Customer_id, First_name, Last_name, Phone_number, Address, Date_of_birth, Email, Hashed_password, Is_active, Role_id)
SELECT  4,'David',  'Brown',   '+995551001004', '18 Kostava St, Tbilisi',          '1988-05-14', 'david.brown@example.com',    '$2a$10$standardHash002',  TRUE,  Role_id FROM Roles WHERE Role_name='STANDARD';

INSERT INTO Customers (Customer_id, First_name, Last_name, Phone_number, Address, Date_of_birth, Email, Hashed_password, Is_active, Role_id)
SELECT  5,'Emma',   'Davis',   '+995551001005', '7 Freedom Square, Tbilisi',       '1995-09-03', 'emma.davis@example.com',     '$2a$10$standardHash003',  TRUE,  Role_id FROM Roles WHERE Role_name='STANDARD';

INSERT INTO Customers (Customer_id, First_name, Last_name, Phone_number, Address, Date_of_birth, Email, Hashed_password, Is_active, Role_id)
SELECT  6,'Frank',  'Miller',  '+995551001006', '33 Pekini Ave, Tbilisi',          '1987-12-19', 'frank.miller@example.com',   '$2a$10$standardHash004',  TRUE,  Role_id FROM Roles WHERE Role_name='STANDARD';

INSERT INTO Customers (Customer_id, First_name, Last_name, Phone_number, Address, Date_of_birth, Email, Hashed_password, Is_active, Role_id)
SELECT  7,'Grace',  'Wilson',  '+995551001007', '9 Marjanishvili St, Tbilisi',     '1993-04-27', 'grace.wilson@example.com',   '$2a$10$standardHash005',  TRUE,  Role_id FROM Roles WHERE Role_name='STANDARD';

INSERT INTO Customers (Customer_id, First_name, Last_name, Phone_number, Address, Date_of_birth, Email, Hashed_password, Is_active, Role_id)
SELECT  8,'Henry',  'Moore',   '+995551001008', '45 Vake Park Rd, Tbilisi',        '1980-08-11', 'henry.moore@example.com',    '$2a$10$standardHash006',  FALSE, Role_id FROM Roles WHERE Role_name='STANDARD';

INSERT INTO Customers (Customer_id, First_name, Last_name, Phone_number, Address, Date_of_birth, Email, Hashed_password, Is_active, Role_id)
SELECT  9,'Iris',   'Taylor',  '+995551001009', '2 Saburtalo St, Tbilisi',         '1991-01-05', 'iris.taylor@example.com',    '$2a$10$managerHash002',   TRUE,  Role_id FROM Roles WHERE Role_name='MANAGER';

INSERT INTO Customers (Customer_id, First_name, Last_name, Phone_number, Address, Date_of_birth, Email, Hashed_password, Is_active, Role_id)
SELECT 10,'Jack',   'Anderson','+995551001010', '11 Isani St, Tbilisi',            '1997-06-18', 'jack.anderson@example.com',  '$2a$10$standardHash007',  TRUE,  Role_id FROM Roles WHERE Role_name='STANDARD';

INSERT INTO Customers (Customer_id, First_name, Last_name, Phone_number, Address, Date_of_birth, Email, Hashed_password, Is_active, Role_id)
SELECT 11,'Karen',  'Thomas',  '+995551001011', '66 Gldani Rd, Tbilisi',           '1986-10-29', 'karen.thomas@example.com',   '$2a$10$standardHash008',  TRUE,  Role_id FROM Roles WHERE Role_name='STANDARD';

INSERT INTO Customers (Customer_id, First_name, Last_name, Phone_number, Address, Date_of_birth, Email, Hashed_password, Is_active, Role_id)
SELECT 12,'Liam',   'Jackson', '+995551001012', '3 Nadzaladevi Blvd, Tbilisi',     '1999-02-14', 'liam.jackson@example.com',   '$2a$10$standardHash009',  TRUE,  Role_id FROM Roles WHERE Role_name='STANDARD';


-- ------------------------------------------------------------
--  9. ACCOUNTS
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
-- 10. ACCOUNT_CUSTOMER  (join table – no surrogate key)
--     individual ownership + two shared accounts
-- ------------------------------------------------------------
-- Individual
INSERT INTO Account_customer (Account_id, Customer_id) VALUES  (1,  1);   -- Alice      → Alice Main Checking
INSERT INTO Account_customer (Account_id, Customer_id) VALUES  (2,  1);   -- Alice      → Alice Savings
INSERT INTO Account_customer (Account_id, Customer_id) VALUES  (3,  2);   -- Bob        → Bob Checking
INSERT INTO Account_customer (Account_id, Customer_id) VALUES  (4,  3);   -- Carol      → Carol Checking
INSERT INTO Account_customer (Account_id, Customer_id) VALUES  (5,  4);   -- David      → David Savings
INSERT INTO Account_customer (Account_id, Customer_id) VALUES  (6,  5);   -- Emma       → Emma Credit
INSERT INTO Account_customer (Account_id, Customer_id) VALUES  (7,  6);   -- Frank      → Frank Checking
INSERT INTO Account_customer (Account_id, Customer_id) VALUES (10,  7);   -- Grace      → Grace Savings
INSERT INTO Account_customer (Account_id, Customer_id) VALUES (11, 10);   -- Jack       → Jack Credit
INSERT INTO Account_customer (Account_id, Customer_id) VALUES (12, 11);   -- Karen      → Karen Checking
-- Shared
INSERT INTO Account_customer (Account_id, Customer_id) VALUES  (8,  3);   -- Carol      → Shared Family Account
INSERT INTO Account_customer (Account_id, Customer_id) VALUES  (8,  4);   -- David      → Shared Family Account
INSERT INTO Account_customer (Account_id, Customer_id) VALUES  (9,  2);   -- Bob        → Business Account
INSERT INTO Account_customer (Account_id, Customer_id) VALUES  (9,  9);   -- Iris       → Business Account


-- ------------------------------------------------------------
-- 11. CARD BRANDS
-- ------------------------------------------------------------
INSERT INTO Card_brands (Card_brand_id, Card_brand_name) VALUES (1, 'Visa');
INSERT INTO Card_brands (Card_brand_id, Card_brand_name) VALUES (2, 'Mastercard');
INSERT INTO Card_brands (Card_brand_id, Card_brand_name) VALUES (3, 'American Express');


-- ------------------------------------------------------------
-- 12. CARDS
--     Brand_id resolved via subquery on unique Card_brand_name
-- ------------------------------------------------------------
INSERT INTO Cards (Card_id, Card_type, Brand_id, Account_id, Spending_limit, Expiration_date, Pan_masked,              Pan_token,                Is_active)
SELECT  1, 'DEBIT',  Card_brand_id,  1,  5000, '2027-12-31', '4111 **** **** 1001', 'tok_visa_debit_1001',  TRUE  FROM Card_brands WHERE Card_brand_name = 'Visa';

INSERT INTO Cards (Card_id, Card_type, Brand_id, Account_id, Spending_limit, Expiration_date, Pan_masked,              Pan_token,                Is_active)
SELECT  2, 'CREDIT', Card_brand_id,  2, 10000, '2026-09-30', '4111 **** **** 1002', 'tok_visa_cred_1002',   TRUE  FROM Card_brands WHERE Card_brand_name = 'Visa';

INSERT INTO Cards (Card_id, Card_type, Brand_id, Account_id, Spending_limit, Expiration_date, Pan_masked,              Pan_token,                Is_active)
SELECT  3, 'DEBIT',  Card_brand_id,  3,  3000, '2028-03-31', '5555 **** **** 1003', 'tok_mc_debit_1003',    TRUE  FROM Card_brands WHERE Card_brand_name = 'Mastercard';

INSERT INTO Cards (Card_id, Card_type, Brand_id, Account_id, Spending_limit, Expiration_date, Pan_masked,              Pan_token,                Is_active)
SELECT  4, 'CREDIT', Card_brand_id,  4,  7000, '2027-06-30', '5555 **** **** 1004', 'tok_mc_cred_1004',     TRUE  FROM Card_brands WHERE Card_brand_name = 'Mastercard';

INSERT INTO Cards (Card_id, Card_type, Brand_id, Account_id, Spending_limit, Expiration_date, Pan_masked,              Pan_token,                Is_active)
SELECT  5, 'DEBIT',  Card_brand_id,  5,  2000, '2026-12-31', '4111 **** **** 1005', 'tok_visa_debit_1005',  TRUE  FROM Card_brands WHERE Card_brand_name = 'Visa';

INSERT INTO Cards (Card_id, Card_type, Brand_id, Account_id, Spending_limit, Expiration_date, Pan_masked,              Pan_token,                Is_active)
SELECT  6, 'CREDIT', Card_brand_id,  6, 15000, '2028-11-30', '3782 **** **** 1006', 'tok_amex_cred_1006',   TRUE  FROM Card_brands WHERE Card_brand_name = 'American Express';

INSERT INTO Cards (Card_id, Card_type, Brand_id, Account_id, Spending_limit, Expiration_date, Pan_masked,              Pan_token,                Is_active)
SELECT  7, 'DEBIT',  Card_brand_id,  7,  4000, '2027-08-31', '5555 **** **** 1007', 'tok_mc_debit_1007',    FALSE FROM Card_brands WHERE Card_brand_name = 'Mastercard';

INSERT INTO Cards (Card_id, Card_type, Brand_id, Account_id, Spending_limit, Expiration_date, Pan_masked,              Pan_token,                Is_active)
SELECT  8, 'DEBIT',  Card_brand_id,  8,  6000, '2027-01-31', '4111 **** **** 1008', 'tok_visa_debit_1008',  TRUE  FROM Card_brands WHERE Card_brand_name = 'Visa';

INSERT INTO Cards (Card_id, Card_type, Brand_id, Account_id, Spending_limit, Expiration_date, Pan_masked,              Pan_token,                Is_active)
SELECT  9, 'DEBIT',  Card_brand_id,  9,  8000, '2028-07-31', '5555 **** **** 1009', 'tok_mc_debit_1009',    TRUE  FROM Card_brands WHERE Card_brand_name = 'Mastercard';

INSERT INTO Cards (Card_id, Card_type, Brand_id, Account_id, Spending_limit, Expiration_date, Pan_masked,              Pan_token,                Is_active)
SELECT 10, 'CREDIT', Card_brand_id, 10,  5000, '2026-06-30', '4111 **** **** 1010', 'tok_visa_cred_1010',   TRUE  FROM Card_brands WHERE Card_brand_name = 'Visa';

INSERT INTO Cards (Card_id, Card_type, Brand_id, Account_id, Spending_limit, Expiration_date, Pan_masked,              Pan_token,                Is_active)
SELECT 11, 'CREDIT', Card_brand_id, 11, 20000, '2029-02-28', '3782 **** **** 1011', 'tok_amex_cred_1011',   TRUE  FROM Card_brands WHERE Card_brand_name = 'American Express';

INSERT INTO Cards (Card_id, Card_type, Brand_id, Account_id, Spending_limit, Expiration_date, Pan_masked,              Pan_token,                Is_active)
SELECT 12, 'DEBIT',  Card_brand_id,  1,  3000, '2027-05-31', '5555 **** **** 1012', 'tok_mc_debit_1012',    TRUE  FROM Card_brands WHERE Card_brand_name = 'Mastercard'; -- Alice 2nd card


-- ------------------------------------------------------------
-- 13. CARD BALANCES
--     Currency_id resolved via subquery on unique Currency_code
-- ------------------------------------------------------------
-- Card 1  (Alice Main Checking – Visa Debit) : GEL + USD
INSERT INTO Card_balances (Card_balance_id, Card_balance_amount, Card_id, Currency_id) SELECT  1, 4250.75, 1, Currency_id FROM Currencies WHERE Currency_code = 'GEL';
INSERT INTO Card_balances (Card_balance_id, Card_balance_amount, Card_id, Currency_id) SELECT  2,  320.00, 1, Currency_id FROM Currencies WHERE Currency_code = 'USD';

-- Card 2  (Alice Savings – Visa Credit) : GEL + EUR
INSERT INTO Card_balances (Card_balance_id, Card_balance_amount, Card_id, Currency_id) SELECT  3, 12000.00, 2, Currency_id FROM Currencies WHERE Currency_code = 'GEL';
INSERT INTO Card_balances (Card_balance_id, Card_balance_amount, Card_id, Currency_id) SELECT  4,   850.50, 2, Currency_id FROM Currencies WHERE Currency_code = 'EUR';

-- Card 3  (Bob Checking – MC Debit) : GEL
INSERT INTO Card_balances (Card_balance_id, Card_balance_amount, Card_id, Currency_id) SELECT  5, 7800.00, 3, Currency_id FROM Currencies WHERE Currency_code = 'GEL';

-- Card 4  (Carol Checking – MC Credit) : GEL + USD
INSERT INTO Card_balances (Card_balance_id, Card_balance_amount, Card_id, Currency_id) SELECT  6, 2100.00, 4, Currency_id FROM Currencies WHERE Currency_code = 'GEL';
INSERT INTO Card_balances (Card_balance_id, Card_balance_amount, Card_id, Currency_id) SELECT  7,  175.25, 4, Currency_id FROM Currencies WHERE Currency_code = 'USD';

-- Card 5  (David Savings – Visa Debit) : GEL
INSERT INTO Card_balances (Card_balance_id, Card_balance_amount, Card_id, Currency_id) SELECT  8, 5500.00, 5, Currency_id FROM Currencies WHERE Currency_code = 'GEL';

-- Card 6  (Emma Credit – AmEx) : GEL + GBP
INSERT INTO Card_balances (Card_balance_id, Card_balance_amount, Card_id, Currency_id) SELECT  9, 1200.00, 6, Currency_id FROM Currencies WHERE Currency_code = 'GEL';
INSERT INTO Card_balances (Card_balance_id, Card_balance_amount, Card_id, Currency_id) SELECT 10,  400.00, 6, Currency_id FROM Currencies WHERE Currency_code = 'GBP';

-- Card 8  (Shared Family – Visa Debit) : GEL + USD
INSERT INTO Card_balances (Card_balance_id, Card_balance_amount, Card_id, Currency_id) SELECT 11, 9300.00, 8, Currency_id FROM Currencies WHERE Currency_code = 'GEL';
INSERT INTO Card_balances (Card_balance_id, Card_balance_amount, Card_id, Currency_id) SELECT 12,  600.00, 8, Currency_id FROM Currencies WHERE Currency_code = 'USD';

-- Card 9  (Business – MC Debit) : GEL + USD + EUR
INSERT INTO Card_balances (Card_balance_id, Card_balance_amount, Card_id, Currency_id) SELECT 13, 25000.00, 9, Currency_id FROM Currencies WHERE Currency_code = 'GEL';
INSERT INTO Card_balances (Card_balance_id, Card_balance_amount, Card_id, Currency_id) SELECT 14,  3200.00, 9, Currency_id FROM Currencies WHERE Currency_code = 'USD';
INSERT INTO Card_balances (Card_balance_id, Card_balance_amount, Card_id, Currency_id) SELECT 15,  1800.00, 9, Currency_id FROM Currencies WHERE Currency_code = 'EUR';

-- Card 10 (Grace Savings – Visa Credit) : GEL
INSERT INTO Card_balances (Card_balance_id, Card_balance_amount, Card_id, Currency_id) SELECT 16, 6200.00, 10, Currency_id FROM Currencies WHERE Currency_code = 'GEL';

-- Card 11 (Jack Credit – AmEx) : GEL + USD + GBP
INSERT INTO Card_balances (Card_balance_id, Card_balance_amount, Card_id, Currency_id) SELECT 17, 3500.00, 11, Currency_id FROM Currencies WHERE Currency_code = 'GEL';
INSERT INTO Card_balances (Card_balance_id, Card_balance_amount, Card_id, Currency_id) SELECT 18,  220.00, 11, Currency_id FROM Currencies WHERE Currency_code = 'USD';
INSERT INTO Card_balances (Card_balance_id, Card_balance_amount, Card_id, Currency_id) SELECT 19,  150.00, 11, Currency_id FROM Currencies WHERE Currency_code = 'GBP';

-- Card 12 (Alice 2nd card – MC Debit) : GEL
INSERT INTO Card_balances (Card_balance_id, Card_balance_amount, Card_id, Currency_id) SELECT 20, 800.00, 12, Currency_id FROM Currencies WHERE Currency_code = 'GEL';


-- ------------------------------------------------------------
-- 14. TRANSACTIONS
--     All 11 TransactionType values + all 3 statuses covered.
--     Related_Transaction_id links TRANSFER pairs and REVERSAL.
--     Currency_id / Service_provider_id via subqueries.
-- ------------------------------------------------------------

-- 1. DEPOSIT – Alice main account (COMPLETE)
INSERT INTO Transactions (Transaction_id, Transaction_type, Account_id, Transaction_time_stamp, Transaction_amount, Currency_id, Service_provider_id, Related_Transaction_id, Transaction_idempotency_key, Transaction_description, Transaction_status)
SELECT  1, 'DEPOSIT', 1, '2025-01-05 09:15:00',  500.00, c.Currency_id, NULL, NULL, 'idem-tx-001', 'Initial deposit', 'COMPLETE'
FROM Currencies c WHERE c.Currency_code = 'GEL';

-- 2. WITHDRAWAL – Alice ATM (COMPLETE)
INSERT INTO Transactions (Transaction_id, Transaction_type, Account_id, Transaction_time_stamp, Transaction_amount, Currency_id, Service_provider_id, Related_Transaction_id, Transaction_idempotency_key, Transaction_description, Transaction_status)
SELECT  2, 'WITHDRAWAL', 1, '2025-01-10 14:30:00', 200.00, c.Currency_id, NULL, NULL, 'idem-tx-002', 'ATM withdrawal', 'COMPLETE'
FROM Currencies c WHERE c.Currency_code = 'GEL';

-- 3. TRANSFER_OUT – Alice → Bob (COMPLETE, paired with tx 4)
INSERT INTO Transactions (Transaction_id, Transaction_type, Account_id, Transaction_time_stamp, Transaction_amount, Currency_id, Service_provider_id, Related_Transaction_id, Transaction_idempotency_key, Transaction_description, Transaction_status)
SELECT  3, 'TRANSFER_OUT', 1, '2025-02-01 11:00:00', 1000.00, c.Currency_id, NULL, NULL, 'idem-tx-003', 'Transfer to Bob', 'COMPLETE'
FROM Currencies c WHERE c.Currency_code = 'GEL';

-- 4. TRANSFER_IN – Bob receives from Alice (COMPLETE, related = tx 3)
INSERT INTO Transactions (Transaction_id, Transaction_type, Account_id, Transaction_time_stamp, Transaction_amount, Currency_id, Service_provider_id, Related_Transaction_id, Transaction_idempotency_key, Transaction_description, Transaction_status)
SELECT  4, 'TRANSFER_IN', 3, '2025-02-01 11:00:01', 1000.00, c.Currency_id, NULL, 3, 'idem-tx-004', 'Transfer from Alice', 'COMPLETE'
FROM Currencies c WHERE c.Currency_code = 'GEL';

-- 5. BILL_PAYMENT – Carol pays Telasi electricity (COMPLETE)
INSERT INTO Transactions (Transaction_id, Transaction_type, Account_id, Transaction_time_stamp, Transaction_amount, Currency_id, Service_provider_id, Related_Transaction_id, Transaction_idempotency_key, Transaction_description, Transaction_status)
SELECT  5, 'BILL_PAYMENT', 4, '2025-02-15 08:45:00', 85.50, c.Currency_id, s.Service_provider_id, NULL, 'idem-tx-005', 'Electricity bill February', 'COMPLETE'
FROM Currencies c, Service_providers s WHERE c.Currency_code = 'GEL' AND s.Service_provider_name = 'Telasi';

-- 6. MOBILE_TOP_UP – Carol tops up Magti (COMPLETE)
INSERT INTO Transactions (Transaction_id, Transaction_type, Account_id, Transaction_time_stamp, Transaction_amount, Currency_id, Service_provider_id, Related_Transaction_id, Transaction_idempotency_key, Transaction_description, Transaction_status)
SELECT  6, 'MOBILE_TOP_UP', 4, '2025-02-20 12:10:00', 20.00, c.Currency_id, s.Service_provider_id, NULL, 'idem-tx-006', 'Magti mobile top-up', 'COMPLETE'
FROM Currencies c, Service_providers s WHERE c.Currency_code = 'GEL' AND s.Service_provider_name = 'Magti';

-- 7. MERCHANT_PAYMENT – David pays Carrefour (COMPLETE, will be reversed by tx 8)
INSERT INTO Transactions (Transaction_id, Transaction_type, Account_id, Transaction_time_stamp, Transaction_amount, Currency_id, Service_provider_id, Related_Transaction_id, Transaction_idempotency_key, Transaction_description, Transaction_status)
SELECT  7, 'MERCHANT_PAYMENT', 5, '2025-03-03 17:55:00', 63.80, c.Currency_id, s.Service_provider_id, NULL, 'idem-tx-007', 'Grocery shopping', 'COMPLETE'
FROM Currencies c, Service_providers s WHERE c.Currency_code = 'GEL' AND s.Service_provider_name = 'Carrefour';

-- 8. REVERSAL – reverses tx 7 (COMPLETE, related = tx 7)
INSERT INTO Transactions (Transaction_id, Transaction_type, Account_id, Transaction_time_stamp, Transaction_amount, Currency_id, Service_provider_id, Related_Transaction_id, Transaction_idempotency_key, Transaction_description, Transaction_status)
SELECT  8, 'REVERSAL', 5, '2025-03-03 18:30:00', 63.80, c.Currency_id, s.Service_provider_id, 7, 'idem-tx-008', 'Reversal of grocery payment', 'COMPLETE'
FROM Currencies c, Service_providers s WHERE c.Currency_code = 'GEL' AND s.Service_provider_name = 'Carrefour';

-- 9. REFUND – Emma refunded by Netflix (COMPLETE)
INSERT INTO Transactions (Transaction_id, Transaction_type, Account_id, Transaction_time_stamp, Transaction_amount, Currency_id, Service_provider_id, Related_Transaction_id, Transaction_idempotency_key, Transaction_description, Transaction_status)
SELECT  9, 'REFUND', 6, '2025-03-10 09:00:00', 45.00, c.Currency_id, s.Service_provider_id, NULL, 'idem-tx-009', 'Netflix double-charge refund', 'COMPLETE'
FROM Currencies c, Service_providers s WHERE c.Currency_code = 'USD' AND s.Service_provider_name = 'Netflix';

-- 10. FEE – Frank monthly maintenance (COMPLETE)
INSERT INTO Transactions (Transaction_id, Transaction_type, Account_id, Transaction_time_stamp, Transaction_amount, Currency_id, Service_provider_id, Related_Transaction_id, Transaction_idempotency_key, Transaction_description, Transaction_status)
SELECT 10, 'FEE', 7, '2025-03-31 23:59:00', 5.00, c.Currency_id, NULL, NULL, 'idem-tx-010', 'Monthly maintenance fee', 'COMPLETE'
FROM Currencies c WHERE c.Currency_code = 'GEL';

-- 11. INTEREST – Alice savings account credit (COMPLETE)
INSERT INTO Transactions (Transaction_id, Transaction_type, Account_id, Transaction_time_stamp, Transaction_amount, Currency_id, Service_provider_id, Related_Transaction_id, Transaction_idempotency_key, Transaction_description, Transaction_status)
SELECT 11, 'INTEREST', 2, '2025-04-01 00:01:00', 12.50, c.Currency_id, NULL, NULL, 'idem-tx-011', 'Monthly savings interest', 'COMPLETE'
FROM Currencies c WHERE c.Currency_code = 'GEL';

-- 12. BILL_PAYMENT – Shared Family pays GWP water (COMPLETE)
INSERT INTO Transactions (Transaction_id, Transaction_type, Account_id, Transaction_time_stamp, Transaction_amount, Currency_id, Service_provider_id, Related_Transaction_id, Transaction_idempotency_key, Transaction_description, Transaction_status)
SELECT 12, 'BILL_PAYMENT', 8, '2025-04-05 10:20:00', 34.00, c.Currency_id, s.Service_provider_id, NULL, 'idem-tx-012', 'Water bill April', 'COMPLETE'
FROM Currencies c, Service_providers s WHERE c.Currency_code = 'GEL' AND s.Service_provider_name = 'GWP';

-- 13. MERCHANT_PAYMENT – Shared Family pays Carrefour (COMPLETE)
INSERT INTO Transactions (Transaction_id, Transaction_type, Account_id, Transaction_time_stamp, Transaction_amount, Currency_id, Service_provider_id, Related_Transaction_id, Transaction_idempotency_key, Transaction_description, Transaction_status)
SELECT 13, 'MERCHANT_PAYMENT', 8, '2025-04-07 13:45:00', 112.30, c.Currency_id, s.Service_provider_id, NULL, 'idem-tx-013', 'Family weekly groceries', 'COMPLETE'
FROM Currencies c, Service_providers s WHERE c.Currency_code = 'GEL' AND s.Service_provider_name = 'Carrefour';

-- 14. DEPOSIT – Business account in USD (COMPLETE)
INSERT INTO Transactions (Transaction_id, Transaction_type, Account_id, Transaction_time_stamp, Transaction_amount, Currency_id, Service_provider_id, Related_Transaction_id, Transaction_idempotency_key, Transaction_description, Transaction_status)
SELECT 14, 'DEPOSIT', 9, '2025-04-12 09:00:00', 5000.00, c.Currency_id, NULL, NULL, 'idem-tx-014', 'Business revenue deposit', 'COMPLETE'
FROM Currencies c WHERE c.Currency_code = 'USD';

-- 15. BILL_PAYMENT – Business pays Aldagi insurance (COMPLETE)
INSERT INTO Transactions (Transaction_id, Transaction_type, Account_id, Transaction_time_stamp, Transaction_amount, Currency_id, Service_provider_id, Related_Transaction_id, Transaction_idempotency_key, Transaction_description, Transaction_status)
SELECT 15, 'BILL_PAYMENT', 9, '2025-04-20 11:00:00', 280.00, c.Currency_id, s.Service_provider_id, NULL, 'idem-tx-015', 'Annual car insurance payment', 'COMPLETE'
FROM Currencies c, Service_providers s WHERE c.Currency_code = 'GEL' AND s.Service_provider_name = 'Aldagi';

-- 16. BILL_PAYMENT – Business pays Revenue Service (tax) (COMPLETE)
INSERT INTO Transactions (Transaction_id, Transaction_type, Account_id, Transaction_time_stamp, Transaction_amount, Currency_id, Service_provider_id, Related_Transaction_id, Transaction_idempotency_key, Transaction_description, Transaction_status)
SELECT 16, 'BILL_PAYMENT', 9, '2025-05-01 08:00:00', 1200.00, c.Currency_id, s.Service_provider_id, NULL, 'idem-tx-016', 'Q1 corporate income tax', 'COMPLETE'
FROM Currencies c, Service_providers s WHERE c.Currency_code = 'GEL' AND s.Service_provider_name = 'Revenue Service';

-- 17. MOBILE_TOP_UP – Business tops up Silknet internet (COMPLETE)
INSERT INTO Transactions (Transaction_id, Transaction_type, Account_id, Transaction_time_stamp, Transaction_amount, Currency_id, Service_provider_id, Related_Transaction_id, Transaction_idempotency_key, Transaction_description, Transaction_status)
SELECT 17, 'MOBILE_TOP_UP', 9, '2025-05-10 15:00:00', 30.00, c.Currency_id, s.Service_provider_id, NULL, 'idem-tx-017', 'Office internet top-up', 'COMPLETE'
FROM Currencies c, Service_providers s WHERE c.Currency_code = 'GEL' AND s.Service_provider_name = 'Silknet';

-- 18. TRANSFER_OUT – Jack → Karen loan repayment (PENDING, paired with tx 19)
INSERT INTO Transactions (Transaction_id, Transaction_type, Account_id, Transaction_time_stamp, Transaction_amount, Currency_id, Service_provider_id, Related_Transaction_id, Transaction_idempotency_key, Transaction_description, Transaction_status)
SELECT 18, 'TRANSFER_OUT', 11, '2025-05-15 16:30:00', 500.00, c.Currency_id, NULL, NULL, 'idem-tx-018', 'Loan repayment to Karen', 'PENDING'
FROM Currencies c WHERE c.Currency_code = 'GEL';

-- 19. TRANSFER_IN – Karen receives from Jack (PENDING, related = tx 18)
INSERT INTO Transactions (Transaction_id, Transaction_type, Account_id, Transaction_time_stamp, Transaction_amount, Currency_id, Service_provider_id, Related_Transaction_id, Transaction_idempotency_key, Transaction_description, Transaction_status)
SELECT 19, 'TRANSFER_IN', 12, '2025-05-15 16:30:01', 500.00, c.Currency_id, NULL, 18, 'idem-tx-019', 'Received from Jack', 'PENDING'
FROM Currencies c WHERE c.Currency_code = 'GEL';

-- 20. WITHDRAWAL – Emma over-limit ATM attempt (FAILED)
INSERT INTO Transactions (Transaction_id, Transaction_type, Account_id, Transaction_time_stamp, Transaction_amount, Currency_id, Service_provider_id, Related_Transaction_id, Transaction_idempotency_key, Transaction_description, Transaction_status)
SELECT 20, 'WITHDRAWAL', 6, '2025-05-20 20:00:00', 5000.00, c.Currency_id, NULL, NULL, 'idem-tx-020', 'Over-limit ATM withdrawal attempt', 'FAILED'
FROM Currencies c WHERE c.Currency_code = 'GEL';

-- 21. MERCHANT_PAYMENT – Emma pays Netflix subscription in USD (COMPLETE)
INSERT INTO Transactions (Transaction_id, Transaction_type, Account_id, Transaction_time_stamp, Transaction_amount, Currency_id, Service_provider_id, Related_Transaction_id, Transaction_idempotency_key, Transaction_description, Transaction_status)
SELECT 21, 'MERCHANT_PAYMENT', 6, '2025-06-01 00:00:00', 15.99, c.Currency_id, s.Service_provider_id, NULL, 'idem-tx-021', 'Netflix monthly subscription', 'COMPLETE'
FROM Currencies c, Service_providers s WHERE c.Currency_code = 'USD' AND s.Service_provider_name = 'Netflix';

-- 22. INTEREST – Grace savings interest credit (COMPLETE)
INSERT INTO Transactions (Transaction_id, Transaction_type, Account_id, Transaction_time_stamp, Transaction_amount, Currency_id, Service_provider_id, Related_Transaction_id, Transaction_idempotency_key, Transaction_description, Transaction_status)
SELECT 22, 'INTEREST', 10, '2025-06-01 00:01:00', 18.75, c.Currency_id, NULL, NULL, 'idem-tx-022', 'Monthly savings interest credit', 'COMPLETE'
FROM Currencies c WHERE c.Currency_code = 'GEL';

-- 23. FEE – Jack credit card annual fee (COMPLETE)
INSERT INTO Transactions (Transaction_id, Transaction_type, Account_id, Transaction_time_stamp, Transaction_amount, Currency_id, Service_provider_id, Related_Transaction_id, Transaction_idempotency_key, Transaction_description, Transaction_status)
SELECT 23, 'FEE', 11, '2025-06-01 23:59:00', 15.00, c.Currency_id, NULL, NULL, 'idem-tx-023', 'Credit card annual fee', 'COMPLETE'
FROM Currencies c WHERE c.Currency_code = 'GEL';

-- 24. BILL_PAYMENT – Silknet mobile – Grace's savings (COMPLETE)
INSERT INTO Transactions (Transaction_id, Transaction_type, Account_id, Transaction_time_stamp, Transaction_amount, Currency_id, Service_provider_id, Related_Transaction_id, Transaction_idempotency_key, Transaction_description, Transaction_status)
SELECT 24, 'BILL_PAYMENT', 10, '2025-06-05 09:30:00', 25.00, c.Currency_id, s.Service_provider_id, NULL, 'idem-tx-024', 'Silknet mobile bill June', 'COMPLETE'
FROM Currencies c, Service_providers s WHERE c.Currency_code = 'GEL' AND s.Service_provider_name = 'Silknet';

-- 25. DEPOSIT – Bob checking in GEL (COMPLETE)
INSERT INTO Transactions (Transaction_id, Transaction_type, Account_id, Transaction_time_stamp, Transaction_amount, Currency_id, Service_provider_id, Related_Transaction_id, Transaction_idempotency_key, Transaction_description, Transaction_status)
SELECT 25, 'DEPOSIT', 3, '2025-06-10 10:00:00', 2000.00, c.Currency_id, NULL, NULL, 'idem-tx-025', 'Salary deposit', 'COMPLETE'
FROM Currencies c WHERE c.Currency_code = 'GEL';


-- Advance all sequences past seeded data range
ALTER SEQUENCE accounts_seq RESTART WITH 1000;
ALTER SEQUENCE customers_seq RESTART WITH 1000;
ALTER SEQUENCE cards_seq RESTART WITH 1000;
ALTER SEQUENCE card_balances_seq RESTART WITH 1000;
ALTER SEQUENCE card_brands_seq RESTART WITH 1000;
ALTER SEQUENCE currencies_seq RESTART WITH 1000;
ALTER SEQUENCE currency_exchanges_seq RESTART WITH 1000;
ALTER SEQUENCE permissions_seq RESTART WITH 1000;
ALTER SEQUENCE roles_seq RESTART WITH 1000;
ALTER SEQUENCE service_categories_seq RESTART WITH 1000;
ALTER SEQUENCE service_providers_seq RESTART WITH 1000;
ALTER SEQUENCE transactions_seq RESTART WITH 1000;