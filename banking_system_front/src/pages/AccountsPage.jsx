import { useMemo, useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { accountApi } from '../api/accountApi';
import { accountKeys } from '../api/accountQueryKeys';
import { Button, Card, Table, TextField, Toast } from '../components/ui';
import styles from './AccountsPage.module.css';

function trimValue(value) {
  return String(value ?? '').trim();
}

function formatValue(value) {
  return value === null || value === undefined || value === '' ? 'Not provided' : String(value);
}

function getActiveValue(record) {
  if (typeof record?.active === 'boolean') return record.active;
  if (typeof record?.isActive === 'boolean') return record.isActive;
  return null;
}

function getAccountName(account) {
  return trimValue(account?.name) || 'Account';
}

function getCustomerName(customer) {
  const firstName = trimValue(customer?.firstName);
  const lastName = trimValue(customer?.lastName);
  const fullName = `${firstName} ${lastName}`.trim();
  return fullName || 'Customer';
}

function formatAmount(transaction) {
  const amount = formatValue(transaction?.amount);
  const currency = trimValue(transaction?.currencyCode);
  return currency ? `${amount} ${currency}` : amount;
}

function StatusBadge({ active }) {
  const className =
    active === true ? styles.active : active === false ? styles.inactive : styles.unknown;
  const label = active === true ? 'Active' : active === false ? 'Inactive' : 'Unknown';

  return <span className={`${styles.badge} ${className}`}>{label}</span>;
}

function DetailItem({ label, children }) {
  return (
    <div className={styles.detailItem}>
      <dt className={styles.detailLabel}>{label}</dt>
      <dd className={styles.detailValue}>{children}</dd>
    </div>
  );
}

export default function AccountsPage() {
  const [emailLookup, setEmailLookup] = useState(null);
  const [customerIdLookup, setCustomerIdLookup] = useState(null);
  const [accountIdLookup, setAccountIdLookup] = useState(null);

  const {
    register: registerEmailLookup,
    handleSubmit: handleEmailLookupSubmit,
    formState: { errors: emailLookupErrors },
  } = useForm({ defaultValues: { email: '' } });

  const {
    register: registerCustomerIdLookup,
    handleSubmit: handleCustomerIdLookupSubmit,
    formState: { errors: customerIdLookupErrors },
  } = useForm({ defaultValues: { customerId: '' } });

  const {
    register: registerAccountIdLookup,
    handleSubmit: handleAccountIdLookupSubmit,
    formState: { errors: accountIdLookupErrors },
  } = useForm({ defaultValues: { accountId: '' } });

  const emailAccountsQuery = useQuery({
    queryKey: emailLookup
      ? accountKeys.byCustomerEmail(emailLookup)
      : [...accountKeys.all, 'email', 'idle'],
    queryFn: () => accountApi.getByEmail(emailLookup),
    enabled: Boolean(emailLookup),
    retry: false,
  });

  const customerAccountsQuery = useQuery({
    queryKey: customerIdLookup
      ? accountKeys.byCustomerId(customerIdLookup)
      : [...accountKeys.all, 'customer', 'idle'],
    queryFn: () => accountApi.getByCustomerId(customerIdLookup),
    enabled: Boolean(customerIdLookup),
    retry: false,
  });

  const accountDetailQuery = useQuery({
    queryKey: accountIdLookup
      ? accountKeys.byId(accountIdLookup)
      : [...accountKeys.all, 'id', 'idle'],
    queryFn: () => accountApi.getById(accountIdLookup),
    enabled: Boolean(accountIdLookup),
    retry: false,
  });

  const summaryColumns = useMemo(
    () => [
      { key: 'name', header: 'Name', render: (account) => formatValue(account.name) },
      { key: 'category', header: 'Category', render: (account) => formatValue(account.category) },
      {
        key: 'dateOpened',
        header: 'Opened',
        render: (account) => formatValue(account.dateOpened),
      },
      {
        key: 'status',
        header: 'Status',
        render: (account) => <StatusBadge active={getActiveValue(account)} />,
      },
    ],
    []
  );

  const profileColumns = useMemo(
    () => [
      { key: 'name', header: 'Name', render: (account) => formatValue(account.name) },
      { key: 'category', header: 'Category', render: (account) => formatValue(account.category) },
      {
        key: 'dateOpened',
        header: 'Opened',
        render: (account) => formatValue(account.dateOpened),
      },
      {
        key: 'customers',
        header: 'Customers',
        render: (account) => account.customers?.length ?? 0,
      },
      { key: 'cards', header: 'Cards', render: (account) => account.cards?.length ?? 0 },
      {
        key: 'transactions',
        header: 'Transactions',
        render: (account) => account.transactions?.length ?? 0,
      },
      {
        key: 'status',
        header: 'Status',
        render: (account) => <StatusBadge active={getActiveValue(account)} />,
      },
    ],
    []
  );

  const customerColumns = useMemo(
    () => [
      { key: 'name', header: 'Name', render: (customer) => getCustomerName(customer) },
      { key: 'email', header: 'Email', render: (customer) => formatValue(customer.email) },
    ],
    []
  );

  const cardColumns = useMemo(
    () => [
      { key: 'panMasked', header: 'Card', render: (card) => formatValue(card.panMasked) },
      { key: 'type', header: 'Type', render: (card) => formatValue(card.type) },
      { key: 'brand', header: 'Brand', render: (card) => formatValue(card.brand) },
      {
        key: 'spendingLimit',
        header: 'Limit',
        render: (card) => formatValue(card.spendingLimit),
      },
      {
        key: 'expirationDate',
        header: 'Expires',
        render: (card) => formatValue(card.expirationDate),
      },
      {
        key: 'status',
        header: 'Status',
        render: (card) => <StatusBadge active={getActiveValue(card)} />,
      },
    ],
    []
  );

  const transactionColumns = useMemo(
    () => [
      {
        key: 'transactionType',
        header: 'Type',
        render: (transaction) => formatValue(transaction.transactionType),
      },
      {
        key: 'amount',
        header: 'Amount',
        render: (transaction) => formatAmount(transaction),
      },
      { key: 'status', header: 'Status', render: (transaction) => formatValue(transaction.status) },
      {
        key: 'timeStamp',
        header: 'Time',
        render: (transaction) => formatValue(transaction.timeStamp),
      },
      {
        key: 'description',
        header: 'Description',
        render: (transaction) => formatValue(transaction.description),
      },
    ],
    []
  );

  const submitEmailLookup = ({ email }) => {
    setEmailLookup(trimValue(email));
  };

  const submitCustomerIdLookup = ({ customerId }) => {
    setCustomerIdLookup(trimValue(customerId));
  };

  const submitAccountIdLookup = ({ accountId }) => {
    setAccountIdLookup(trimValue(accountId));
  };

  const emailAccounts = emailAccountsQuery.data ?? [];
  const customerAccounts = customerAccountsQuery.data ?? [];
  const accountDetail = accountDetailQuery.data;

  return (
    <div className={styles.page}>
      <header className={styles.pageHeader}>
        <p className={styles.kicker}>Account area</p>
        <h1 className={styles.title}>Accounts</h1>
      </header>

      <Card title="Find accounts">
        <div className={styles.lookupGrid}>
          <form className={styles.lookupForm} onSubmit={handleEmailLookupSubmit(submitEmailLookup)}>
            <TextField
              id="account-email-lookup"
              label="Customer email"
              type="email"
              error={emailLookupErrors.email?.message}
              required
              {...registerEmailLookup('email', {
                required: 'Customer email is required.',
                pattern: { value: /\S+@\S+\.\S+/, message: 'Enter a valid email.' },
              })}
            />
            <Button type="submit" isLoading={emailAccountsQuery.isFetching}>
              Load summaries
            </Button>
          </form>

          <form
            className={styles.lookupForm}
            onSubmit={handleCustomerIdLookupSubmit(submitCustomerIdLookup)}
          >
            <TextField
              id="account-customer-id-lookup"
              label="Customer ID"
              type="number"
              min="1"
              error={customerIdLookupErrors.customerId?.message}
              required
              {...registerCustomerIdLookup('customerId', {
                required: 'Customer ID is required.',
                validate: (value) =>
                  /^\d+$/.test(trimValue(value)) || 'Customer ID must be a positive number.',
              })}
            />
            <Button type="submit" isLoading={customerAccountsQuery.isFetching}>
              Load profiles
            </Button>
          </form>

          <form
            className={styles.lookupForm}
            onSubmit={handleAccountIdLookupSubmit(submitAccountIdLookup)}
          >
            <TextField
              id="account-id-lookup"
              label="Account ID"
              type="number"
              min="1"
              error={accountIdLookupErrors.accountId?.message}
              required
              {...registerAccountIdLookup('accountId', {
                required: 'Account ID is required.',
                validate: (value) =>
                  /^\d+$/.test(trimValue(value)) || 'Account ID must be a positive number.',
              })}
            />
            <Button type="submit" isLoading={accountDetailQuery.isFetching}>
              Load detail
            </Button>
          </form>
        </div>
      </Card>

      <div className={styles.sectionGrid}>
        <Card
          title="Summaries by email"
          subtitle="This endpoint returns summary fields only."
          bodyClassName={styles.stack}
        >
          {emailAccountsQuery.isError && (
            <Toast variant="danger" message="Account summaries could not be loaded." />
          )}
          <Table
            columns={summaryColumns}
            data={emailAccounts}
            getRowKey={(account, index) =>
              `${account.name ?? 'account'}-${account.dateOpened ?? index}`
            }
            emptyMessage={emailLookup ? 'No account summaries found.' : 'No email loaded.'}
            loadingMessage="Loading account summaries..."
            isLoading={emailAccountsQuery.isLoading}
          />
        </Card>

        <Card
          title="Profiles by customer ID"
          subtitle="This endpoint returns full account profiles."
          bodyClassName={styles.stack}
        >
          {customerAccountsQuery.isError && (
            <Toast variant="danger" message="Account profiles could not be loaded." />
          )}
          <Table
            columns={profileColumns}
            data={customerAccounts}
            getRowKey={(account, index) =>
              `${account.name ?? 'account'}-${account.dateOpened ?? index}`
            }
            emptyMessage={customerIdLookup ? 'No account profiles found.' : 'No customer loaded.'}
            loadingMessage="Loading account profiles..."
            isLoading={customerAccountsQuery.isLoading}
          />
        </Card>
      </div>

      <Card title="Account detail" bodyClassName={styles.stack}>
        {!accountIdLookup && <p className={styles.mutedText}>No account selected.</p>}

        {accountDetailQuery.isError && (
          <Toast variant="danger" message="Account detail could not be loaded." />
        )}

        {accountDetail && (
          <>
            <dl className={styles.profileGrid}>
              <DetailItem label="Account ID">{formatValue(accountIdLookup)}</DetailItem>
              <DetailItem label="Name">{getAccountName(accountDetail)}</DetailItem>
              <DetailItem label="Category">{formatValue(accountDetail.category)}</DetailItem>
              <DetailItem label="Opened">{formatValue(accountDetail.dateOpened)}</DetailItem>
              <DetailItem label="Status">
                <StatusBadge active={getActiveValue(accountDetail)} />
              </DetailItem>
              <DetailItem label="Customers">{accountDetail.customers?.length ?? 0}</DetailItem>
              <DetailItem label="Cards">{accountDetail.cards?.length ?? 0}</DetailItem>
              <DetailItem label="Transactions">
                {accountDetail.transactions?.length ?? 0}
              </DetailItem>
            </dl>

            <Table
              columns={customerColumns}
              data={accountDetail.customers ?? []}
              getRowKey={(customer, index) => customer.email ?? index}
              emptyMessage="No customers linked to this account."
              caption="Linked customers"
            />

            <Table
              columns={cardColumns}
              data={accountDetail.cards ?? []}
              getRowKey={(card, index) => card.panToken ?? card.panMasked ?? index}
              emptyMessage="No cards linked to this account."
              caption="Cards"
            />

            <Table
              columns={transactionColumns}
              data={accountDetail.transactions ?? []}
              getRowKey={(transaction, index) =>
                `${transaction.timeStamp ?? 'transaction'}-${transaction.description ?? index}`
              }
              emptyMessage="No transactions found for this account."
              caption="Transactions"
            />
          </>
        )}
      </Card>
    </div>
  );
}
