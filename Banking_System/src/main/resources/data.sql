insert into currencies (currency_id, currency_code, currency_name) values (1, 'GEL', 'Georgian Lari');
insert into currencies (currency_id, currency_code, currency_name) values (2, 'USD', 'US Dollar');
insert into currencies (currency_id, currency_code, currency_name) values (3, 'EUR', 'Euro');
insert into currencies (currency_id, currency_code, currency_name) values (4, 'GBP', 'British Pound');



insert into currency_exchanges (currency_exchange_id, from_currency_id, to_currency_id, exchange_rate, exchange_time_stamp)
select  1, f.currency_id, t.currency_id, 2.7200, '2025-06-01 08:00:00' from currencies f, currencies t where f.currency_code='USD' and t.currency_code='GEL';

insert into currency_exchanges (currency_exchange_id, from_currency_id, to_currency_id, exchange_rate, exchange_time_stamp)
select  2, f.currency_id, t.currency_id, 0.3676, '2025-06-01 08:00:00' from currencies f, currencies t where f.currency_code='GEL' and t.currency_code='USD';

insert into currency_exchanges (currency_exchange_id, from_currency_id, to_currency_id, exchange_rate, exchange_time_stamp)
select  3, f.currency_id, t.currency_id, 2.9400, '2025-06-01 08:00:00' from currencies f, currencies t where f.currency_code='EUR' and t.currency_code='GEL';

insert into currency_exchanges (currency_exchange_id, from_currency_id, to_currency_id, exchange_rate, exchange_time_stamp)
select  4, f.currency_id, t.currency_id, 0.3401, '2025-06-01 08:00:00' from currencies f, currencies t where f.currency_code='GEL' and t.currency_code='EUR';

insert into currency_exchanges (currency_exchange_id, from_currency_id, to_currency_id, exchange_rate, exchange_time_stamp)
select  5, f.currency_id, t.currency_id, 3.4500, '2025-06-01 08:00:00' from currencies f, currencies t where f.currency_code='GBP' and t.currency_code='GEL';

insert into currency_exchanges (currency_exchange_id, from_currency_id, to_currency_id, exchange_rate, exchange_time_stamp)
select  6, f.currency_id, t.currency_id, 0.2899, '2025-06-01 08:00:00' from currencies f, currencies t where f.currency_code='GEL' and t.currency_code='GBP';

insert into currency_exchanges (currency_exchange_id, from_currency_id, to_currency_id, exchange_rate, exchange_time_stamp)
select  7, f.currency_id, t.currency_id, 1.0820, '2025-06-01 08:00:00' from currencies f, currencies t where f.currency_code='USD' and t.currency_code='EUR';

insert into currency_exchanges (currency_exchange_id, from_currency_id, to_currency_id, exchange_rate, exchange_time_stamp)
select  8, f.currency_id, t.currency_id, 0.9242, '2025-06-01 08:00:00' from currencies f, currencies t where f.currency_code='EUR' and t.currency_code='USD';

insert into currency_exchanges (currency_exchange_id, from_currency_id, to_currency_id, exchange_rate, exchange_time_stamp)
select  9, f.currency_id, t.currency_id, 1.2700, '2025-06-01 08:00:00' from currencies f, currencies t where f.currency_code='USD' and t.currency_code='GBP';

insert into currency_exchanges (currency_exchange_id, from_currency_id, to_currency_id, exchange_rate, exchange_time_stamp)
select 10, f.currency_id, t.currency_id, 0.7874, '2025-06-01 08:00:00' from currencies f, currencies t where f.currency_code='GBP' and t.currency_code='USD';

insert into currency_exchanges (currency_exchange_id, from_currency_id, to_currency_id, exchange_rate, exchange_time_stamp)
select 11, f.currency_id, t.currency_id, 1.1770, '2025-06-01 08:00:00' from currencies f, currencies t where f.currency_code='EUR' and t.currency_code='GBP';

insert into currency_exchanges (currency_exchange_id, from_currency_id, to_currency_id, exchange_rate, exchange_time_stamp)
select 12, f.currency_id, t.currency_id, 0.8496, '2025-06-01 08:00:00' from currencies f, currencies t where f.currency_code='GBP' and t.currency_code='EUR';




insert into customers (customer_id, first_name, last_name, phone_number, address, date_of_birth, email, hashed_password, is_active, role) values
    ( 1,'Giga',    'Beradze',   '995551001002', '22 Chavchavadze Ave, Tbilisi',   '1990-07-22', 'gbera23@example.com',      '$2a$10$S9MUPRmdpGtXvQ2DOCILUuo69JZE1L.1rIqkkzLEqycFDxzMAz3VW',   true,  'MANAGER');

insert into customers (customer_id, first_name, last_name, phone_number, address, date_of_birth, email, hashed_password, is_active, role) values
    ( 2,'Carol',  'White',   '995551001003', '5 Agmashenebeli Ave, Tbilisi',   '1992-11-30', 'carol.white@example.com',    '$2a$10$standardHash001',  true,  'STANDARD');

insert into customers (customer_id, first_name, last_name, phone_number, address, date_of_birth, email, hashed_password, is_active, role) values
    ( 3,'David',  'Brown',   '995551001004', '18 Kostava St, Tbilisi',         '1988-05-14', 'david.brown@example.com',    '$2a$10$standardHash002',  true,  'STANDARD');

insert into customers (customer_id, first_name, last_name, phone_number, address, date_of_birth, email, hashed_password, is_active, role) values
    ( 4,'Emma',   'Davis',   '995551001005', '7 Freedom Square, Tbilisi',      '1995-09-03', 'emma.davis@example.com',     '$2a$10$standardHash003',  true,  'STANDARD');

insert into customers (customer_id, first_name, last_name, phone_number, address, date_of_birth, email, hashed_password, is_active, role) values
    ( 5,'Frank',  'Miller',  '995551001006', '33 Pekini Ave, Tbilisi',         '1987-12-19', 'frank.miller@example.com',   '$2a$10$standardHash004',  true,  'STANDARD');

insert into customers (customer_id, first_name, last_name, phone_number, address, date_of_birth, email, hashed_password, is_active, role) values
    ( 6,'Grace',  'Wilson',  '995551001007', '9 Marjanishvili St, Tbilisi',    '1993-04-27', 'grace.wilson@example.com',   '$2a$10$standardHash005',  true,  'STANDARD');

insert into customers (customer_id, first_name, last_name, phone_number, address, date_of_birth, email, hashed_password, is_active, role) values
    ( 7,'Henry',  'Moore',   '995551001008', '45 Vake Park Rd, Tbilisi',       '1980-08-11', 'henry.moore@example.com',    '$2a$10$standardHash006',  false, 'STANDARD');

insert into customers (customer_id, first_name, last_name, phone_number, address, date_of_birth, email, hashed_password, is_active, role) values
    ( 8,'Iris',   'Taylor',  '995551001009', '2 Saburtalo St, Tbilisi',        '1991-01-05', 'iris.taylor@example.com',    '$2a$10$managerHash002',   true,  'MANAGER');

insert into customers (customer_id, first_name, last_name, phone_number, address, date_of_birth, email, hashed_password, is_active, role) values
    (9,'Jack',   'Anderson','995551001010', '11 Isani St, Tbilisi',           '1997-06-18', 'jack.anderson@example.com',  '$2a$10$standardHash007',  true,  'STANDARD');

insert into customers (customer_id, first_name, last_name, phone_number, address, date_of_birth, email, hashed_password, is_active, role) values
    (10,'Karen',  'Thomas',  '995551001011', '66 Gldani Rd, Tbilisi',          '1986-10-29', 'karen.thomas@example.com',   '$2a$10$standardHash008',  true,  'STANDARD');

insert into customers (customer_id, first_name, last_name, phone_number, address, date_of_birth, email, hashed_password, is_active, role) values
    (11,'Liam',   'Jackson', '995551001012', '3 Nadzaladevi Blvd, Tbilisi',    '1999-02-14', 'liam.jackson@example.com',   '$2a$10$standardHash009',  true,  'STANDARD');


insert into accounts (account_id, account_name, account_category, date_opened, is_active) values  (1,  'Alice Main Checking',   'CHECKING', '2020-01-10', true);
insert into accounts (account_id, account_name, account_category, date_opened, is_active) values  (2,  'Alice Savings',          'SAVINGS',  '2020-01-10', true);
insert into accounts (account_id, account_name, account_category, date_opened, is_active) values  (3,  'Bob Checking',           'CHECKING', '2019-06-15', true);
insert into accounts (account_id, account_name, account_category, date_opened, is_active) values  (4,  'Carol Checking',         'CHECKING', '2021-03-22', true);
insert into accounts (account_id, account_name, account_category, date_opened, is_active) values  (5,  'David Savings',          'SAVINGS',  '2018-11-05', true);
insert into accounts (account_id, account_name, account_category, date_opened, is_active) values  (6,  'Emma Credit',            'CREDIT',   '2022-07-01', true);
insert into accounts (account_id, account_name, account_category, date_opened, is_active) values  (7,  'Frank Checking',         'CHECKING', '2017-09-30', true);
insert into accounts (account_id, account_name, account_category, date_opened, is_active) values  (8,  'Shared Family Account',  'CHECKING', '2023-01-01', true);
insert into accounts (account_id, account_name, account_category, date_opened, is_active) values  (9,  'Business Account',       'CHECKING', '2021-08-14', true);
insert into accounts (account_id, account_name, account_category, date_opened, is_active) values (10,  'Grace Savings',          'SAVINGS',  '2020-05-17', true);
insert into accounts (account_id, account_name, account_category, date_opened, is_active) values (11,  'Jack Credit',            'CREDIT',   '2023-09-10', true);
insert into accounts (account_id, account_name, account_category, date_opened, is_active) values (12,  'Karen Checking',         'CHECKING', '2016-04-20', false);



insert into account_customer (account_id, customer_id) values  (1,  1);
insert into account_customer (account_id, customer_id) values  (2,  1);
insert into account_customer (account_id, customer_id) values  (3,  2);
insert into account_customer (account_id, customer_id) values  (4,  3);
insert into account_customer (account_id, customer_id) values  (5,  4);
insert into account_customer (account_id, customer_id) values  (6,  5);
insert into account_customer (account_id, customer_id) values  (7,  6);
insert into account_customer (account_id, customer_id) values (10,  7);
insert into account_customer (account_id, customer_id) values (11, 10);
insert into account_customer (account_id, customer_id) values (12, 11);
insert into account_customer (account_id, customer_id) values  (8,  3);
insert into account_customer (account_id, customer_id) values  (8,  4);
insert into account_customer (account_id, customer_id) values  (9,  2);
insert into account_customer (account_id, customer_id) values  (9,  9);



insert into cards (card_id, card_type, brand, account_id, spending_limit, expiration_date, pan_masked,            pan_token,               is_active) values
    ( 1, 'DEBIT',  'VISA',       1,  5000, '2027-12-31', '4111 **** **** 1001', '4111 1234 1234 1001', true);

insert into cards (card_id, card_type, brand, account_id, spending_limit, expiration_date, pan_masked,            pan_token,               is_active) values
    ( 2, 'CREDIT', 'VISA',       2, 10000, '2026-09-30', '4111 **** **** 1002', 'tok_visa_cred_1002',  true);

insert into cards (card_id, card_type, brand, account_id, spending_limit, expiration_date, pan_masked,            pan_token,               is_active) values
    ( 3, 'DEBIT',  'MASTERCARD', 3,  3000, '2028-03-31', '5555 **** **** 1003', 'tok_mc_debit_1003',   true);

insert into cards (card_id, card_type, brand, account_id, spending_limit, expiration_date, pan_masked,            pan_token,               is_active) values
    ( 4, 'CREDIT', 'MASTERCARD', 4,  7000, '2027-06-30', '5555 **** **** 1004', 'tok_mc_cred_1004',    true);

insert into cards (card_id, card_type, brand, account_id, spending_limit, expiration_date, pan_masked,            pan_token,               is_active) values
    ( 5, 'DEBIT',  'VISA',       5,  2000, '2026-12-31', '4111 **** **** 1005', 'tok_visa_debit_1005', true);

insert into cards (card_id, card_type, brand, account_id, spending_limit, expiration_date, pan_masked,            pan_token,               is_active) values
    ( 6, 'CREDIT', 'MASTERCARD', 6, 15000, '2028-11-30', '5555 **** **** 1006', 'tok_mc_cred_1006',    true);

insert into cards (card_id, card_type, brand, account_id, spending_limit, expiration_date, pan_masked,            pan_token,               is_active) values
    ( 7, 'DEBIT',  'MASTERCARD', 7,  4000, '2027-08-31', '5555 **** **** 1007', 'tok_mc_debit_1007',   false);

insert into cards (card_id, card_type, brand, account_id, spending_limit, expiration_date, pan_masked,            pan_token,               is_active) values
    ( 8, 'DEBIT',  'VISA',       8,  6000, '2027-01-31', '4111 **** **** 1008', 'tok_visa_debit_1008', true);

insert into cards (card_id, card_type, brand, account_id, spending_limit, expiration_date, pan_masked,            pan_token,               is_active) values
    ( 9, 'DEBIT',  'MASTERCARD', 9,  8000, '2028-07-31', '5555 **** **** 1009', 'tok_mc_debit_1009',   true);

insert into cards (card_id, card_type, brand, account_id, spending_limit, expiration_date, pan_masked,            pan_token,               is_active) values
    (10, 'CREDIT', 'VISA',      10,  5000, '2026-06-30', '4111 **** **** 1010', 'tok_visa_cred_1010',  true);

insert into cards (card_id, card_type, brand, account_id, spending_limit, expiration_date, pan_masked,            pan_token,               is_active) values
    (11, 'CREDIT', 'VISA',      11, 20000, '2029-02-28', '4111 **** **** 1011', 'tok_visa_cred_1011',  true);

insert into cards (card_id, card_type, brand, account_id, spending_limit, expiration_date, pan_masked,            pan_token,               is_active) values
    (12, 'DEBIT',  'MASTERCARD', 1,  3000, '2027-05-31', '5555 **** **** 1012', 'tok_mc_debit_1012',   true);



insert into card_balances (card_balance_id, card_balance_amount, card_id, currency_id) select  1, 4250.75, 1, currency_id from currencies where currency_code = 'GEL';
insert into card_balances (card_balance_id, card_balance_amount, card_id, currency_id) select  2,  320.00, 1, currency_id from currencies where currency_code = 'USD';

insert into card_balances (card_balance_id, card_balance_amount, card_id, currency_id) select  3, 8500.00, 2, currency_id from currencies where currency_code = 'GEL';
insert into card_balances (card_balance_id, card_balance_amount, card_id, currency_id) select  4,   850.50, 2, currency_id from currencies where currency_code = 'EUR';

insert into card_balances (card_balance_id, card_balance_amount, card_id, currency_id) select  5, 2450.00, 3, currency_id from currencies where currency_code = 'GEL';

insert into card_balances (card_balance_id, card_balance_amount, card_id, currency_id) select  6, 2100.00, 4, currency_id from currencies where currency_code = 'GEL';
insert into card_balances (card_balance_id, card_balance_amount, card_id, currency_id) select  7,  175.25, 4, currency_id from currencies where currency_code = 'USD';

insert into card_balances (card_balance_id, card_balance_amount, card_id, currency_id) select  8, 1650.00, 5, currency_id from currencies where currency_code = 'GEL';

insert into card_balances (card_balance_id, card_balance_amount, card_id, currency_id) select  9, 1200.00, 6, currency_id from currencies where currency_code = 'GEL';
insert into card_balances (card_balance_id, card_balance_amount, card_id, currency_id) select 10,  400.00, 6, currency_id from currencies where currency_code = 'GBP';

insert into card_balances (card_balance_id, card_balance_amount, card_id, currency_id) select 11, 4800.00, 8, currency_id from currencies where currency_code = 'GEL';
insert into card_balances (card_balance_id, card_balance_amount, card_id, currency_id) select 12,  600.00, 8, currency_id from currencies where currency_code = 'USD';

insert into card_balances (card_balance_id, card_balance_amount, card_id, currency_id) select 13, 2600.00, 9, currency_id from currencies where currency_code = 'GEL';
insert into card_balances (card_balance_id, card_balance_amount, card_id, currency_id) select 14,  3200.00, 9, currency_id from currencies where currency_code = 'USD';
insert into card_balances (card_balance_id, card_balance_amount, card_id, currency_id) select 15,  1800.00, 9, currency_id from currencies where currency_code = 'EUR';

insert into card_balances (card_balance_id, card_balance_amount, card_id, currency_id) select 16, 4200.00, 10, currency_id from currencies where currency_code = 'GEL';

insert into card_balances (card_balance_id, card_balance_amount, card_id, currency_id) select 17, 3500.00, 11, currency_id from currencies where currency_code = 'GEL';
insert into card_balances (card_balance_id, card_balance_amount, card_id, currency_id) select 18,  220.00, 11, currency_id from currencies where currency_code = 'USD';
insert into card_balances (card_balance_id, card_balance_amount, card_id, currency_id) select 19,  150.00, 11, currency_id from currencies where currency_code = 'GBP';

insert into card_balances (card_balance_id, card_balance_amount, card_id, currency_id) select 20, 800.00, 12, currency_id from currencies where currency_code = 'GEL';



UPDATE accounts_seq             SET next_val = 1000;
UPDATE customers_seq             SET next_val = 1000;
UPDATE cards_seq                 SET next_val = 1000;
UPDATE card_balances_seq         SET next_val = 1000;
UPDATE currencies_seq             SET next_val = 1000;
UPDATE currency_exchanges_seq     SET next_val = 1000;
UPDATE transactions_seq           SET next_val = 1000;