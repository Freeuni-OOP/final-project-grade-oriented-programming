import { useMemo, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { accountApi } from '../api/accountApi';
import { accountKeys } from '../api/accountQueryKeys';
import { applyBackendFormErrors } from '../api/formErrors';
import { useAuth } from '../components/AuthContext';
import { Button, Card, Modal, Select, Table, TextField, Toast, useToast } from '../components/ui';
import styles from './AccountsPage.module.css';

const ACCOUNT_CATEGORIES = [
  { value: 'CHECKING', label: 'Checking' },
  { value: 'SAVINGS', label: 'Savings' },
  { value: 'CREDIT', label: 'Credit' },
];

const CARD_TYPES = [
  { value: 'DEBIT', label: 'Debit' },
  { value: 'CREDIT', label: 'Credit' },
];

const CARD_BRANDS = [
  { value: 'VISA', label: 'Visa' },
  { value: 'MASTERCARD', label: 'Mastercard' },
];

const UPDATE_NAME_FIELDS = ['accountId', 'accountName'];
const STATUS_FIELDS = ['accountId'];
const CREATE_ACCOUNT_FIELDS = ['accountName', 'category', 'customerId'];
const CREATE_CARD_FIELDS = ['accountId', 'cardType', 'cardBrand', 'spendingLimit', 'pan'];
const DELETE_ACCOUNT_FIELDS = ['accountId'];

function trimValue(value) {
  return String(value ?? '').trim();
}

function formatValue(value) {
  return value === null || value === undefined || value === '' ? 'Not provided' : String(value);
}

function validatePositiveId(value, label) {
  return /^\d+$/.test(trimValue(value)) || `${label} must be a positive number.`;
}

function buildCreatePayload(values) {
  return {
    accountName: trimValue(values.accountName),
    category: values.category,
  };
}

// Backend now sends createdAccountId, but this keeps it safe if the name changes.
function getCreatedAccountId(response) {
  return response?.createdAccountId ?? response?.accountId ?? response?.id ?? null;
}

function buildCreateCardPayload(values) {
  return {
    cardType: values.cardType,
    cardBrand: values.cardBrand,
    spendingLimit: trimValue(values.spendingLimit),
    pan: trimValue(values.pan),
  };
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
  const queryClient = useQueryClient();
  const { authority } = useAuth();
  const { showToast } = useToast();
  const [emailLookup, setEmailLookup] = useState(null);
  const [customerIdLookup, setCustomerIdLookup] = useState(null);
  const [accountIdLookup, setAccountIdLookup] = useState(null);
  const [balanceLookup, setBalanceLookup] = useState(null);
  const [pendingDelete, setPendingDelete] = useState(null);
  const [createdAccountLink, setCreatedAccountLink] = useState(null);
  const isManager = authority === 'MANAGER';

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

  const {
    register: registerCreateAccount,
    handleSubmit: handleCreateAccountSubmit,
    reset: resetCreateAccount,
    setError: setCreateAccountError,
    formState: { errors: createAccountErrors, isSubmitting: isCreateAccountSubmitting },
  } = useForm({ defaultValues: { accountName: '', category: '', customerId: '' } });

  const {
    register: registerUpdateName,
    handleSubmit: handleUpdateNameSubmit,
    reset: resetUpdateName,
    setError: setUpdateNameError,
    formState: { errors: updateNameErrors, isSubmitting: isUpdateNameSubmitting },
  } = useForm({ defaultValues: { accountId: '', accountName: '' } });

  const {
    register: registerStatus,
    handleSubmit: handleStatusSubmit,
    setError: setStatusError,
    formState: { errors: statusErrors, isSubmitting: isStatusSubmitting },
  } = useForm({ defaultValues: { accountId: '' } });

  const {
    register: registerBalance,
    handleSubmit: handleBalanceSubmit,
    formState: { errors: balanceErrors },
  } = useForm({ defaultValues: { accountId: '', currencyCode: 'GEL' } });

  const {
    register: registerCreateCard,
    handleSubmit: handleCreateCardSubmit,
    reset: resetCreateCard,
    setError: setCreateCardError,
    formState: { errors: createCardErrors, isSubmitting: isCreateCardSubmitting },
  } = useForm({
    defaultValues: {
      accountId: '',
      cardType: '',
      cardBrand: '',
      spendingLimit: '',
      pan: '',
    },
  });

  const {
    register: registerDeleteAccount,
    handleSubmit: handleDeleteAccountSubmit,
    reset: resetDeleteAccount,
    setError: setDeleteAccountError,
    formState: { errors: deleteAccountErrors },
  } = useForm({ defaultValues: { accountId: '' } });

  const emailAccountsQuery = useQuery({
    queryKey: emailLookup
      ? accountKeys.byCustomerEmail(emailLookup)
      : [...accountKeys.all, 'email', 'idle'],
    queryFn: () => accountApi.getByEmail(emailLookup),
    enabled: Boolean(emailLookup),
    retry: false,
  });

  const balanceQuery = useQuery({
    queryKey: balanceLookup
      ? accountKeys.balance(balanceLookup.accountId, balanceLookup.currencyCode)
      : [...accountKeys.all, 'balance', 'idle'],
    queryFn: () =>
      accountApi.getBalanceByCurrency(balanceLookup.accountId, balanceLookup.currencyCode),
    enabled: Boolean(balanceLookup),
    retry: false,
  });

  const createAccountMutation = useMutation({
    mutationFn: async ({ payload, customerId }) => {
      const createdAccount = await accountApi.create(payload);
      const createdAccountId = getCreatedAccountId(createdAccount);

      if (!createdAccountId) {
        const error = new Error('Account id was not returned.');
        error.missingCreatedAccountId = true;
        throw error;
      }

      // Backend gives us the new account id, so the user does not have to copy it.
      await accountApi.registerCustomer(createdAccountId, customerId);

      return { createdAccountId, customerId };
    },
    retry: false,
    onSuccess: ({ createdAccountId, customerId }) => {
      queryClient.invalidateQueries({ queryKey: accountKeys.all });
      resetCreateAccount();
      setCreatedAccountLink({ createdAccountId, customerId });
      showToast({
        title: 'Account created and linked.',
        message: `New account ID: ${createdAccountId}`,
        variant: 'success',
      });
    },
  });

  const updateNameMutation = useMutation({
    mutationFn: ({ accountId, accountName }) => accountApi.updateName(accountId, accountName),
    retry: false,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: accountKeys.all });
      resetUpdateName();
      showToast({ title: 'Account name updated.', variant: 'success' });
    },
  });

  const statusMutation = useMutation({
    mutationFn: ({ accountId, action }) =>
      action === 'activate' ? accountApi.activate(accountId) : accountApi.deactivate(accountId),
    retry: false,
    onSuccess: (_result, variables) => {
      queryClient.invalidateQueries({ queryKey: accountKeys.all });
      showToast({
        title: variables.action === 'activate' ? 'Account activated.' : 'Account deactivated.',
        variant: 'success',
      });
    },
  });

  const createCardMutation = useMutation({
    mutationFn: ({ accountId, payload }) => accountApi.createCard(accountId, payload),
    retry: false,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: accountKeys.all });
      resetCreateCard();
      showToast({ title: 'Card created for account.', variant: 'success' });
    },
  });

  const deleteAccountMutation = useMutation({
    mutationFn: (accountId) => accountApi.delete(accountId),
    retry: false,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: accountKeys.all });
      resetDeleteAccount();
      setPendingDelete(null);
      showToast({ title: 'Account deleted.', variant: 'success' });
    },
    onError: (error) => {
      applyBackendFormErrors(error, setDeleteAccountError, DELETE_ACCOUNT_FIELDS);
    },
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

  const submitCreateAccount = async (values) => {
    try {
      await createAccountMutation.mutateAsync({
        payload: buildCreatePayload(values),
        customerId: trimValue(values.customerId),
      });
    } catch (error) {
      if (error.missingCreatedAccountId) {
        setCreateAccountError('root', {
          type: 'server',
          message: 'Account was created, but the backend did not return its ID.',
        });
        return;
      }

      applyBackendFormErrors(error, setCreateAccountError, CREATE_ACCOUNT_FIELDS);
    }
  };

  const submitUpdateName = async (values) => {
    try {
      await updateNameMutation.mutateAsync({
        accountId: trimValue(values.accountId),
        accountName: trimValue(values.accountName),
      });
    } catch (error) {
      applyBackendFormErrors(error, setUpdateNameError, UPDATE_NAME_FIELDS);
    }
  };

  const submitStatus = async ({ accountId }, action) => {
    try {
      await statusMutation.mutateAsync({ accountId: trimValue(accountId), action });
    } catch (error) {
      applyBackendFormErrors(error, setStatusError, STATUS_FIELDS);
    }
  };

  const submitBalanceLookup = ({ accountId, currencyCode }) => {
    setBalanceLookup({
      accountId: trimValue(accountId),
      currencyCode: trimValue(currencyCode).toUpperCase(),
    });
  };

  const submitCreateCard = async (values) => {
    try {
      await createCardMutation.mutateAsync({
        accountId: trimValue(values.accountId),
        payload: buildCreateCardPayload(values),
      });
    } catch (error) {
      applyBackendFormErrors(error, setCreateCardError, CREATE_CARD_FIELDS);
    }
  };

  const requestDeleteAccount = ({ accountId }) => {
    setPendingDelete({ accountId: trimValue(accountId) });
  };

  const confirmDeleteAccount = () => {
    if (!pendingDelete?.accountId) {
      return;
    }

    deleteAccountMutation.mutate(pendingDelete.accountId);
  };

  const emailAccounts = emailAccountsQuery.data ?? [];
  const customerAccounts = customerAccountsQuery.data ?? [];
  const accountDetail = accountDetailQuery.data;
  const statusAction = statusMutation.isPending ? statusMutation.variables?.action : null;

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

      <div className={styles.managementGrid}>
        <Card title="Create account">
          <form
            className={styles.formStack}
            onSubmit={handleCreateAccountSubmit(submitCreateAccount)}
            noValidate
          >
            <TextField
              id="create-account-name"
              label="Account name"
              error={createAccountErrors.accountName?.message}
              required
              {...registerCreateAccount('accountName', {
                required: 'Account name is required.',
                minLength: { value: 3, message: 'At least 3 characters.' },
                maxLength: { value: 20, message: 'At most 20 characters.' },
              })}
            />
            <Select
              id="create-account-category"
              label="Category"
              placeholder="Choose category"
              options={ACCOUNT_CATEGORIES}
              error={createAccountErrors.category?.message}
              required
              {...registerCreateAccount('category', {
                required: 'Category is required.',
              })}
            />
            <TextField
              id="create-account-customer-id"
              label="Customer ID"
              type="number"
              min="1"
              error={createAccountErrors.customerId?.message}
              required
              {...registerCreateAccount('customerId', {
                required: 'Customer ID is required.',
                validate: (value) => validatePositiveId(value, 'Customer ID'),
              })}
            />

            {createAccountErrors.root && (
              <Toast variant="danger" message={createAccountErrors.root.message} />
            )}

            {createdAccountLink && (
              <div className={styles.createdResult}>
                <span className={styles.detailLabel}>Created account ID</span>
                <strong className={styles.createdValue}>
                  {createdAccountLink.createdAccountId}
                </strong>
                <p className={styles.mutedText}>
                  Linked to customer {createdAccountLink.customerId}.
                </p>
              </div>
            )}

            <Button
              type="submit"
              isLoading={createAccountMutation.isPending || isCreateAccountSubmitting}
            >
              Create account
            </Button>
          </form>
        </Card>

        <Card title="Update account name">
          <form
            className={styles.formStack}
            onSubmit={handleUpdateNameSubmit(submitUpdateName)}
            noValidate
          >
            <div className={styles.twoColumnForm}>
              <TextField
                id="update-name-account-id"
                label="Account ID"
                type="number"
                min="1"
                error={updateNameErrors.accountId?.message}
                required
                {...registerUpdateName('accountId', {
                  required: 'Account ID is required.',
                  validate: (value) => validatePositiveId(value, 'Account ID'),
                })}
              />
              <TextField
                id="update-account-name"
                label="New account name"
                error={updateNameErrors.accountName?.message}
                required
                {...registerUpdateName('accountName', {
                  required: 'Account name is required.',
                  minLength: { value: 3, message: 'At least 3 characters.' },
                  maxLength: { value: 20, message: 'At most 20 characters.' },
                })}
              />
            </div>

            {updateNameErrors.root && (
              <Toast variant="danger" message={updateNameErrors.root.message} />
            )}

            <Button
              type="submit"
              isLoading={updateNameMutation.isPending || isUpdateNameSubmitting}
            >
              Save name
            </Button>
          </form>
        </Card>

        <Card title="Account status">
          <form className={styles.formStack} noValidate>
            <TextField
              id="status-account-id"
              label="Account ID"
              type="number"
              min="1"
              error={statusErrors.accountId?.message}
              required
              {...registerStatus('accountId', {
                required: 'Account ID is required.',
                validate: (value) => validatePositiveId(value, 'Account ID'),
              })}
            />

            {statusErrors.root && <Toast variant="danger" message={statusErrors.root.message} />}

            <div className={styles.actionsRow}>
              <Button
                type="button"
                variant="secondary"
                disabled={statusMutation.isPending || isStatusSubmitting}
                isLoading={statusAction === 'activate'}
                onClick={handleStatusSubmit((values) => submitStatus(values, 'activate'))}
              >
                Activate
              </Button>
              <Button
                type="button"
                variant="danger"
                disabled={statusMutation.isPending || isStatusSubmitting}
                isLoading={statusAction === 'deactivate'}
                onClick={handleStatusSubmit((values) => submitStatus(values, 'deactivate'))}
              >
                Deactivate
              </Button>
            </div>
          </form>
        </Card>

        <Card title="Create card">
          <form
            className={styles.formStack}
            onSubmit={handleCreateCardSubmit(submitCreateCard)}
            noValidate
          >
            <div className={styles.twoColumnForm}>
              <TextField
                id="create-card-account-id"
                label="Account ID"
                type="number"
                min="1"
                error={createCardErrors.accountId?.message}
                required
                {...registerCreateCard('accountId', {
                  required: 'Account ID is required.',
                  validate: (value) => validatePositiveId(value, 'Account ID'),
                })}
              />
              <TextField
                id="create-card-spending-limit"
                label="Spending limit"
                type="number"
                min="100"
                max="100000"
                step="0.01"
                error={createCardErrors.spendingLimit?.message}
                required
                {...registerCreateCard('spendingLimit', {
                  required: 'Spending limit is required.',
                  min: { value: 100, message: 'Spending limit must be at least 100.' },
                  max: { value: 100000, message: 'Spending limit must be at most 100000.' },
                })}
              />
              <Select
                id="create-card-type"
                label="Card type"
                placeholder="Choose type"
                options={CARD_TYPES}
                error={createCardErrors.cardType?.message}
                required
                {...registerCreateCard('cardType', { required: 'Card type is required.' })}
              />
              <Select
                id="create-card-brand"
                label="Card brand"
                placeholder="Choose brand"
                options={CARD_BRANDS}
                error={createCardErrors.cardBrand?.message}
                required
                {...registerCreateCard('cardBrand', { required: 'Card brand is required.' })}
              />
            </div>

            <TextField
              id="create-card-pan"
              label="PAN"
              inputMode="numeric"
              error={createCardErrors.pan?.message}
              required
              {...registerCreateCard('pan', {
                required: 'PAN is required.',
                minLength: { value: 12, message: 'PAN must be at least 12 digits.' },
                maxLength: { value: 19, message: 'PAN must be at most 19 digits.' },
                pattern: { value: /^\d+$/, message: 'PAN must contain only digits.' },
              })}
            />

            {createCardErrors.root && (
              <Toast variant="danger" message={createCardErrors.root.message} />
            )}

            <Button
              type="submit"
              isLoading={createCardMutation.isPending || isCreateCardSubmitting}
            >
              Create card
            </Button>
          </form>
        </Card>
      </div>

      <Card title="Balance by currency">
        <div className={styles.formStack}>
          <form
            className={styles.balanceForm}
            onSubmit={handleBalanceSubmit(submitBalanceLookup)}
            noValidate
          >
            <TextField
              id="balance-account-id"
              label="Account ID"
              type="number"
              min="1"
              error={balanceErrors.accountId?.message}
              required
              {...registerBalance('accountId', {
                required: 'Account ID is required.',
                validate: (value) => validatePositiveId(value, 'Account ID'),
              })}
            />
            <TextField
              id="balance-currency-code"
              label="Currency code"
              error={balanceErrors.currencyCode?.message}
              required
              {...registerBalance('currencyCode', {
                required: 'Currency code is required.',
                pattern: {
                  value: /^[A-Za-z]{3}$/,
                  message: 'Use a three-letter currency code.',
                },
              })}
            />
            <Button type="submit" isLoading={balanceQuery.isFetching}>
              View balance
            </Button>
          </form>

          {balanceQuery.isError && (
            <Toast variant="danger" message="Account balance could not be loaded." />
          )}

          {balanceLookup && balanceQuery.isSuccess && (
            <div className={styles.balanceResult}>
              <span className={styles.detailLabel}>Converted balance</span>
              <strong className={styles.balanceValue}>
                {formatValue(balanceQuery.data)} {balanceLookup.currencyCode}
              </strong>
            </div>
          )}
        </div>
      </Card>

      {isManager && (
        <Card title="Delete account" subtitle="Manager-only action. Confirmation is required.">
          <form
            className={styles.formStack}
            onSubmit={handleDeleteAccountSubmit(requestDeleteAccount)}
            noValidate
          >
            <TextField
              id="delete-account-id"
              label="Account ID"
              type="number"
              min="1"
              error={deleteAccountErrors.accountId?.message}
              required
              {...registerDeleteAccount('accountId', {
                required: 'Account ID is required.',
                validate: (value) => validatePositiveId(value, 'Account ID'),
              })}
            />

            {deleteAccountErrors.root && (
              <Toast variant="danger" message={deleteAccountErrors.root.message} />
            )}

            <div className={styles.actionsRow}>
              <Button type="submit" variant="danger" disabled={deleteAccountMutation.isPending}>
                Delete account
              </Button>
            </div>
          </form>
        </Card>
      )}

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

      <Modal
        open={Boolean(pendingDelete)}
        title="Delete account"
        onClose={() => setPendingDelete(null)}
        footer={
          <>
            <Button
              type="button"
              variant="secondary"
              disabled={deleteAccountMutation.isPending}
              onClick={() => setPendingDelete(null)}
            >
              Cancel
            </Button>
            <Button
              type="button"
              variant="danger"
              isLoading={deleteAccountMutation.isPending}
              onClick={confirmDeleteAccount}
            >
              Delete account
            </Button>
          </>
        }
      >
        <p className={styles.modalText}>
          Delete account {pendingDelete?.accountId}? This action cannot be undone.
        </p>
      </Modal>
    </div>
  );
}
