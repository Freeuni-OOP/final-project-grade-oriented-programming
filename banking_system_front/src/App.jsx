import { useState } from 'react';
import { authApi } from './api/authApi';
import { customerApi } from './api/customerApi';
import { accountApi } from './api/accountApi';
import { cardApi } from './api/cardApi';

const REGISTER_BODY = {
  firstName: 'Test',
  lastName: 'User',
  phoneNumber: '995555123456',
  address: 'Tbilisi',
  dateOfBirth: '1990-05-15',
  email: 'test.user@example.com',
  password: 'Password123',
};

const UPDATE_CUSTOMER_BODY = {
  firstName: 'Updated',
  lastName: 'Name',
  phoneNumber: '995555000000',
  address: 'New Address',
};

const ACCOUNT_BODY = {
  accountName: 'Test Account',
  category: 'CHECKING',
};

const CARD_BODY = {
  cardType: 'DEBIT',
  cardBrand: 'VISA',
  spendingLimit: 1000,
  pan: '1234567890123456',
};

const DEPOSIT_BODY = { amountToDeposit: 100, currencyCode: 'USD' };
const WITHDRAW_BODY = { amountToWithdraw: 50, currencyCode: 'USD' };
const TRANSFER_BODY = {
  senderCardId: 1,
  receiverCardId: 2,
  amount: 25,
  currencyCode: 'USD',
};
const EXCHANGE_BODY = {
  amount: 10,
  fromCurrencyCode: 'USD',
  toCurrencyCode: 'EUR',
};

function Section({ title, children }) {
  return (
    <div style={{ marginBottom: 16 }}>
      <h3 style={{ margin: '10px 0 6px' }}>{title}</h3>
      <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8 }}>{children}</div>
    </div>
  );
}

export default function SmokeTest() {
  const [customerId, setCustomerId] = useState('1');
  const [accountId, setAccountId] = useState('1');
  const [cardId, setCardId] = useState('1');
  const [email, setEmail] = useState('test.user@example.com');
  const [currency, setCurrency] = useState('USD');
  const [accountName, setAccountName] = useState('Renamed Account');
  const [loginEmail, setLoginEmail] = useState('test.user@example.com');
  const [loginPassword, setLoginPassword] = useState('Password123');
  const [log, setLog] = useState([]);

  const record = (label, status, data) => {
    const time = new Date().toLocaleTimeString();
    console.log(label, status, data);
    setLog((prev) => [`${time}  ${label} → ${status}`, ...prev].slice(0, 40));
  };

  const run = (label, fn) => async () => {
    try {
      const data = await fn();
      record(label, 'OK', data);
    } catch (error) {
      record(label, error.response?.status ?? 'NETWORK / CORS ERROR', error.response?.data);
    }
  };

  const doLogin = async () => {
    try {
      const data = await authApi.login({
        email: loginEmail,
        password: loginPassword,
      });
      const token = data['Generated JWT token'];
      if (token) {
        localStorage.setItem('token', token);
        record('login + store token', 'OK', '(token stored in localStorage)');
      } else {
        record('login', 'OK but no token field found', data);
      }
    } catch (error) {
      record('login', error.response?.status ?? 'NETWORK / CORS ERROR', error.response?.data);
    }
  };

  const clearToken = () => {
    localStorage.removeItem('token');
    record('clear token', 'done', null);
  };

  const input = (value, setter, width = 130) => (
    <input
      value={value}
      onChange={(e) => setter(e.target.value)}
      style={{ width, marginLeft: 4 }}
    />
  );

  return (
    <div
      style={{
        padding: 20,
        fontFamily: 'system-ui, sans-serif',
        maxWidth: 920,
        margin: '0 auto',
        textAlign: 'left',
      }}
    >
      <h1 style={{ marginBottom: 4 }}>API Smoke Test</h1>
      <p style={{ marginTop: 0 }}>
        Open DevTools → Network and turn on <strong>Preserve log</strong>. Each button fires one
        request — inspect its URL, method, payload and headers.
      </p>

      <fieldset style={{ marginBottom: 16 }}>
        <legend>Auth / token</legend>
        <label>email{input(loginEmail, setLoginEmail, 200)}</label>{' '}
        <label>password{input(loginPassword, setLoginPassword)}</label>{' '}
        <button onClick={doLogin}>Login &amp; store token</button>{' '}
        <button onClick={clearToken}>Clear token</button>
      </fieldset>

      <fieldset style={{ marginBottom: 16 }}>
        <legend>Shared params</legend>
        <label>customerId{input(customerId, setCustomerId, 50)}</label>{' '}
        <label>accountId{input(accountId, setAccountId, 50)}</label>{' '}
        <label>cardId{input(cardId, setCardId, 50)}</label>{' '}
        <label>email{input(email, setEmail, 200)}</label>{' '}
        <label>currency{input(currency, setCurrency, 60)}</label>{' '}
        <label>accountName{input(accountName, setAccountName)}</label>
      </fieldset>

      <Section title="authApi">
        <button onClick={run('auth.register', () => authApi.register(REGISTER_BODY))}>
          register
        </button>
        <button onClick={run('auth.validate', () => authApi.validate())}>validate</button>
      </Section>

      <Section title="customerApi">
        <button onClick={run('customer.getById', () => customerApi.getById(customerId))}>
          getById
        </button>
        <button onClick={run('customer.getByEmail', () => customerApi.getByEmail(email))}>
          getByEmail
        </button>
        <button onClick={run('customer.getByAccount', () => customerApi.getByAccount(accountId))}>
          getByAccount
        </button>
        <button
          onClick={run('customer.update', () =>
            customerApi.update(customerId, UPDATE_CUSTOMER_BODY)
          )}
        >
          update
        </button>
        <button onClick={run('customer.activate', () => customerApi.activate(customerId))}>
          activate
        </button>
        <button onClick={run('customer.deactivate', () => customerApi.deactivate(customerId))}>
          deactivate
        </button>
        <button onClick={run('customer.delete', () => customerApi.delete(customerId))}>
          delete
        </button>
      </Section>

      <Section title="accountApi">
        <button onClick={run('account.create', () => accountApi.create(ACCOUNT_BODY))}>
          create
        </button>
        <button
          onClick={run('account.createCard', () => accountApi.createCard(accountId, CARD_BODY))}
        >
          createCard
        </button>
        <button onClick={run('account.activate', () => accountApi.activate(accountId))}>
          activate
        </button>
        <button onClick={run('account.deactivate', () => accountApi.deactivate(accountId))}>
          deactivate
        </button>
        <button onClick={run('account.getById', () => accountApi.getById(accountId))}>
          getById
        </button>
        <button onClick={run('account.delete', () => accountApi.delete(accountId))}>delete</button>
        <button onClick={run('account.getByEmail', () => accountApi.getByEmail(email))}>
          getByEmail
        </button>
        <button
          onClick={run('account.getByCustomerId', () => accountApi.getByCustomerId(customerId))}
        >
          getByCustomerId
        </button>
        <button
          onClick={run('account.updateName', () => accountApi.updateName(accountId, accountName))}
        >
          updateName
        </button>
        <button
          onClick={run('account.registerCustomer', () =>
            accountApi.registerCustomer(accountId, customerId)
          )}
        >
          registerCustomer
        </button>
        <button
          onClick={run('account.getBalanceByCurrency', () =>
            accountApi.getBalanceByCurrency(accountId, currency)
          )}
        >
          getBalanceByCurrency
        </button>
      </Section>

      <Section title="cardApi">
        <button onClick={run('card.getById', () => cardApi.getById(cardId))}>getById</button>
        <button onClick={run('card.getLinkedAccount', () => cardApi.getLinkedAccount(cardId))}>
          getLinkedAccount
        </button>
        <button onClick={run('card.getBalances', () => cardApi.getBalances(cardId))}>
          getBalances
        </button>
        <button onClick={run('card.checkExpiration', () => cardApi.checkExpiration(cardId))}>
          checkExpiration
        </button>
        <button onClick={run('card.deposit', () => cardApi.deposit(cardId, DEPOSIT_BODY))}>
          deposit
        </button>
        <button onClick={run('card.withdraw', () => cardApi.withdraw(cardId, WITHDRAW_BODY))}>
          withdraw
        </button>
        <button onClick={run('card.transfer', () => cardApi.transfer(TRANSFER_BODY))}>
          transfer
        </button>
        <button
          onClick={run('card.exchangeCurrency', () =>
            cardApi.exchangeCurrency(cardId, EXCHANGE_BODY)
          )}
        >
          exchangeCurrency
        </button>
        <button onClick={run('card.addCurrency', () => cardApi.addCurrency(cardId, currency))}>
          addCurrency
        </button>
        <button onClick={run('card.activate', () => cardApi.activate(cardId))}>activate</button>
        <button onClick={run('card.deactivate', () => cardApi.deactivate(cardId))}>
          deactivate
        </button>
        <button onClick={run('card.delete', () => cardApi.delete(cardId))}>delete</button>
      </Section>

      <h2 style={{ marginBottom: 6 }}>Log (newest first)</h2>
      <pre
        style={{
          background: '#111',
          color: '#3f3',
          padding: 12,
          height: 240,
          overflow: 'auto',
          borderRadius: 6,
          fontSize: 13,
        }}
      >
        {log.join('\n') || '(no calls yet)'}
      </pre>
    </div>
  );
}
