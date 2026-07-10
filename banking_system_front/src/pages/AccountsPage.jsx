import { useMemo, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { useTranslation } from 'react-i18next';
import { accountApi } from '../api/accountApi';
import { accountKeys } from '../api/accountQueryKeys';
import { applyBackendFormErrors } from '../api/formErrors';
import { useAuth } from '../components/AuthContext';
import { Button, Card, Modal, Select, Table, TextField, Toast, useToast } from '../components/ui';
import styles from './AccountsPage.module.css';

const ACCOUNT_CATEGORIES = [
  { value: 'CHECKING', label: 'category_checking' },
  { value: 'SAVINGS', label: 'category_savings' },
  { value: 'CREDIT', label: 'category_credit' },
];

const CARD_TYPES = [
  { value: 'DEBIT', label: 'type_debit' },
  { value: 'CREDIT', label: 'type_credit' },
];

const CARD_BRANDS = [
  { value: 'VISA', label: 'brand_visa' },
  { value: 'MASTERCARD', label: 'brand_mastercard' },
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
  return value === null || value === undefined || value === '' ? 'not_provided' : String(value);
}

function validatePositiveId(value, label, t) {
  return /^\d+$/.test(trimValue(value)) || t(`error_positive_id`, { label });
}

function buildCreatePayload(values) {
  return {
    accountName: trimValue(values.accountName),
    category: values.category,
  };
}

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

function StatusBadge({ active, t }) {
  const className =
    active === true ? styles.active : active === false ? styles.inactive : styles.unknown;
  const label =
    active === true
      ? t('status_active')
      : active === false
        ? t('status_inactive')
        : t('status_unknown');

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
  const { t } = useTranslation('accounts');
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
        const error = new Error(t('account_id_not_returned'));
        error.missingCreatedAccountId = true;
        throw error;
      }

      await accountApi.registerCustomer(createdAccountId, customerId);

      return { createdAccountId, customerId };
    },
    retry: false,
    onSuccess: ({ createdAccountId, customerId }) => {
      queryClient.invalidateQueries({ queryKey: accountKeys.all });
      resetCreateAccount();
      setCreatedAccountLink({ createdAccountId, customerId });
      showToast({
        title: t('account_created'),
        message: t('created_account_id') + `: ${createdAccountId}`,
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
      showToast({ title: t('account_name_updated'), variant: 'success' });
    },
  });

  const statusMutation = useMutation({
    mutationFn: ({ accountId, action }) =>
      action === 'activate' ? accountApi.activate(accountId) : accountApi.deactivate(accountId),
    retry: false,
    onSuccess: (_result, variables) => {
      queryClient.invalidateQueries({ queryKey: accountKeys.all });
      showToast({
        title: variables.action === 'activate' ? t('account_activated') : t('account_deactivated'),
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
      showToast({ title: t('card_created'), variant: 'success' });
    },
  });

  const deleteAccountMutation = useMutation({
    mutationFn: (accountId) => accountApi.delete(accountId),
    retry: false,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: accountKeys.all });
      resetDeleteAccount();
      setPendingDelete(null);
      showToast({ title: t('account_deleted'), variant: 'success' });
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
      { key: 'name', header: t('col_name'), render: (account) => formatValue(account.name) },
      {
        key: 'category',
        header: t('col_category'),
        render: (account) => formatValue(account.category),
      },
      {
        key: 'dateOpened',
        header: t('col_opened'),
        render: (account) => formatValue(account.dateOpened),
      },
      {
        key: 'status',
        header: t('col_status'),
        render: (account) => <StatusBadge active={getActiveValue(account)} t={t} />,
      },
    ],
    [t]
  );

  const profileColumns = useMemo(
    () => [
      { key: 'name', header: t('col_name'), render: (account) => formatValue(account.name) },
      {
        key: 'category',
        header: t('col_category'),
        render: (account) => formatValue(account.category),
      },
      {
        key: 'dateOpened',
        header: t('col_opened'),
        render: (account) => formatValue(account.dateOpened),
      },
      {
        key: 'customers',
        header: t('col_customers'),
        render: (account) => account.customers?.length ?? 0,
      },
      {
        key: 'cards',
        header: t('col_transactions'),
        render: (account) => account.cards?.length ?? 0,
      },
      {
        key: 'transactions',
        header: t('col_transactions'),
        render: (account) => account.transactions?.length ?? 0,
      },
      {
        key: 'status',
        header: t('col_status'),
        render: (account) => <StatusBadge active={getActiveValue(account)} t={t} />,
      },
    ],
    [t]
  );

  const customerColumns = useMemo(
    () => [
      { key: 'name', header: t('col_name'), render: (customer) => getCustomerName(customer) },
      { key: 'email', header: t('col_email'), render: (customer) => formatValue(customer.email) },
    ],
    [t]
  );

  const cardColumns = useMemo(
    () => [
      { key: 'panMasked', header: t('col_card'), render: (card) => formatValue(card.panMasked) },
      { key: 'type', header: t('col_type'), render: (card) => formatValue(card.type) },
      { key: 'brand', header: t('col_brand'), render: (card) => formatValue(card.brand) },
      {
        key: 'spendingLimit',
        header: t('col_limit'),
        render: (card) => formatValue(card.spendingLimit),
      },
      {
        key: 'expirationDate',
        header: t('col_expires'),
        render: (card) => formatValue(card.expirationDate),
      },
      {
        key: 'status',
        header: t('col_status'),
        render: (card) => <StatusBadge active={getActiveValue(card)} t={t} />,
      },
    ],
    [t]
  );

  const transactionColumns = useMemo(
    () => [
      {
        key: 'transactionType',
        header: t('col_type'),
        render: (transaction) => formatValue(transaction.transactionType),
      },
      {
        key: 'amount',
        header: t('col_amount'),
        render: (transaction) => formatAmount(transaction),
      },
      {
        key: 'status',
        header: t('col_status'),
        render: (transaction) => formatValue(transaction.status),
      },
      {
        key: 'timeStamp',
        header: t('col_time'),
        render: (transaction) => formatValue(transaction.timeStamp),
      },
      {
        key: 'description',
        header: t('col_description'),
        render: (transaction) => formatValue(transaction.description),
      },
    ],
    [t]
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
          message: t('account_id_not_returned'),
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
        <p className={styles.kicker}>{t('page_kicker')}</p>
        <h1 className={styles.title}>{t('page_title')}</h1>
      </header>

      <Card title={t('find_accounts')}>
        <div className={styles.lookupGrid}>
          <form className={styles.lookupForm} onSubmit={handleEmailLookupSubmit(submitEmailLookup)}>
            <TextField
              id="account-email-lookup"
              label={t('customer_email')}
              type="email"
              error={emailLookupErrors.email?.message}
              required
              {...registerEmailLookup('email', {
                required: t('customer_email_required'),
                pattern: { value: /\S+@\S+\.\S+/, message: t('customer_email_invalid') },
              })}
            />
            <Button type="submit" isLoading={emailAccountsQuery.isFetching}>
              {t('load_summaries')}
            </Button>
          </form>

          <form
            className={styles.lookupForm}
            onSubmit={handleCustomerIdLookupSubmit(submitCustomerIdLookup)}
          >
            <TextField
              id="account-customer-id-lookup"
              label={t('customer_id')}
              type="number"
              min="1"
              error={customerIdLookupErrors.customerId?.message}
              required
              {...registerCustomerIdLookup('customerId', {
                required: t('customer_id_required'),
                validate: (value) => /^\d+$/.test(trimValue(value)) || t('customer_id_positive'),
              })}
            />
            <Button type="submit" isLoading={customerAccountsQuery.isFetching}>
              {t('load_profiles')}
            </Button>
          </form>

          <form
            className={styles.lookupForm}
            onSubmit={handleAccountIdLookupSubmit(submitAccountIdLookup)}
          >
            <TextField
              id="account-id-lookup"
              label={t('account_id')}
              type="number"
              min="1"
              error={accountIdLookupErrors.accountId?.message}
              required
              {...registerAccountIdLookup('accountId', {
                required: t('account_id_required'),
                validate: (value) => /^\d+$/.test(trimValue(value)) || t('account_id_positive'),
              })}
            />
            <Button type="submit" isLoading={accountDetailQuery.isFetching}>
              {t('load_detail')}
            </Button>
          </form>
        </div>
      </Card>

      <div className={styles.managementGrid}>
        <Card title={t('create_account')}>
          <form
            className={styles.formStack}
            onSubmit={handleCreateAccountSubmit(submitCreateAccount)}
            noValidate
          >
            <TextField
              id="create-account-name"
              label={t('account_name')}
              error={createAccountErrors.accountName?.message}
              required
              {...registerCreateAccount('accountName', {
                required: t('account_name_required'),
                minLength: { value: 3, message: t('account_name_min') },
                maxLength: { value: 20, message: t('account_name_max') },
              })}
            />
            <Select
              id="create-account-category"
              label={t('category')}
              placeholder={t('choose_category')}
              options={ACCOUNT_CATEGORIES.map((cat) => ({ ...cat, label: t(cat.label) }))}
              error={createAccountErrors.category?.message}
              required
              {...registerCreateAccount('category', {
                required: t('category_required'),
              })}
            />
            <TextField
              id="create-account-customer-id"
              label={t('customer_id')}
              type="number"
              min="1"
              error={createAccountErrors.customerId?.message}
              required
              {...registerCreateAccount('customerId', {
                required: t('customer_id_required'),
                validate: (value) => validatePositiveId(value, t('customer_id'), t),
              })}
            />

            {createAccountErrors.root && (
              <Toast variant="danger" message={createAccountErrors.root.message} />
            )}

            {createdAccountLink && (
              <div className={styles.createdResult}>
                <span className={styles.detailLabel}>{t('created_account_id')}</span>
                <strong className={styles.createdValue}>
                  {createdAccountLink.createdAccountId}
                </strong>
                <p className={styles.mutedText}>
                  {t('linked_to_customer', { customerId: createdAccountLink.customerId })}
                </p>
              </div>
            )}

            <Button
              type="submit"
              isLoading={createAccountMutation.isPending || isCreateAccountSubmitting}
            >
              {t('create_account')}
            </Button>
          </form>
        </Card>

        <Card title={t('update_account_name')}>
          <form
            className={styles.formStack}
            onSubmit={handleUpdateNameSubmit(submitUpdateName)}
            noValidate
          >
            <div className={styles.twoColumnForm}>
              <TextField
                id="update-name-account-id"
                label={t('account_id')}
                type="number"
                min="1"
                error={updateNameErrors.accountId?.message}
                required
                {...registerUpdateName('accountId', {
                  required: t('account_id_required'),
                  validate: (value) => validatePositiveId(value, t('account_id'), t),
                })}
              />
              <TextField
                id="update-account-name"
                label={t('new_account_name')}
                error={updateNameErrors.accountName?.message}
                required
                {...registerUpdateName('accountName', {
                  required: t('account_name_required'),
                  minLength: { value: 3, message: t('account_name_min') },
                  maxLength: { value: 20, message: t('account_name_max') },
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
              {t('save_name')}
            </Button>
          </form>
        </Card>

        <Card title={t('account_status')}>
          <form className={styles.formStack} noValidate>
            <TextField
              id="status-account-id"
              label={t('account_id')}
              type="number"
              min="1"
              error={statusErrors.accountId?.message}
              required
              {...registerStatus('accountId', {
                required: t('account_id_required'),
                validate: (value) => validatePositiveId(value, t('account_id'), t),
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
                {t('activate')}
              </Button>
              <Button
                type="button"
                variant="danger"
                disabled={statusMutation.isPending || isStatusSubmitting}
                isLoading={statusAction === 'deactivate'}
                onClick={handleStatusSubmit((values) => submitStatus(values, 'deactivate'))}
              >
                {t('deactivate')}
              </Button>
            </div>
          </form>
        </Card>

        <Card title={t('create_card')}>
          <form
            className={styles.formStack}
            onSubmit={handleCreateCardSubmit(submitCreateCard)}
            noValidate
          >
            <div className={styles.twoColumnForm}>
              <TextField
                id="create-card-account-id"
                label={t('account_id')}
                type="number"
                min="1"
                error={createCardErrors.accountId?.message}
                required
                {...registerCreateCard('accountId', {
                  required: t('account_id_required'),
                  validate: (value) => validatePositiveId(value, t('account_id'), t),
                })}
              />
              <TextField
                id="create-card-spending-limit"
                label={t('spending_limit')}
                type="number"
                min="100"
                max="100000"
                step="0.01"
                error={createCardErrors.spendingLimit?.message}
                required
                {...registerCreateCard('spendingLimit', {
                  required: t('spending_limit_required'),
                  min: { value: 100, message: t('spending_limit_min') },
                  max: { value: 100000, message: t('spending_limit_max') },
                })}
              />
              <Select
                id="create-card-type"
                label={t('card_type')}
                placeholder={t('choose_type')}
                options={CARD_TYPES.map((type) => ({ ...type, label: t(type.label) }))}
                error={createCardErrors.cardType?.message}
                required
                {...registerCreateCard('cardType', { required: t('card_type_required') })}
              />
              <Select
                id="create-card-brand"
                label={t('card_brand')}
                placeholder={t('choose_brand')}
                options={CARD_BRANDS.map((brand) => ({ ...brand, label: t(brand.label) }))}
                error={createCardErrors.cardBrand?.message}
                required
                {...registerCreateCard('cardBrand', { required: t('card_brand_required') })}
              />
            </div>

            <TextField
              id="create-card-pan"
              label={t('pan')}
              inputMode="numeric"
              error={createCardErrors.pan?.message}
              required
              {...registerCreateCard('pan', {
                required: t('pan_required'),
                minLength: { value: 12, message: t('pan_min') },
                maxLength: { value: 19, message: t('pan_max') },
                pattern: { value: /^\d+$/, message: t('pan_digits_only') },
              })}
            />

            {createCardErrors.root && (
              <Toast variant="danger" message={createCardErrors.root.message} />
            )}

            <Button
              type="submit"
              isLoading={createCardMutation.isPending || isCreateCardSubmitting}
            >
              {t('create_card')}
            </Button>
          </form>
        </Card>
      </div>

      <Card title={t('balance_by_currency')}>
        <div className={styles.formStack}>
          <form
            className={styles.balanceForm}
            onSubmit={handleBalanceSubmit(submitBalanceLookup)}
            noValidate
          >
            <TextField
              id="balance-account-id"
              label={t('account_id')}
              type="number"
              min="1"
              error={balanceErrors.accountId?.message}
              required
              {...registerBalance('accountId', {
                required: t('account_id_required'),
                validate: (value) => validatePositiveId(value, t('account_id'), t),
              })}
            />
            <TextField
              id="balance-currency-code"
              label={t('currency_code')}
              error={balanceErrors.currencyCode?.message}
              required
              {...registerBalance('currencyCode', {
                required: t('currency_code_required'),
                pattern: {
                  value: /^[A-Za-z]{3}$/,
                  message: t('currency_code_invalid'),
                },
              })}
            />
            <Button type="submit" isLoading={balanceQuery.isFetching}>
              {t('view_balance')}
            </Button>
          </form>

          {balanceQuery.isError && <Toast variant="danger" message={t('balance_error')} />}

          {balanceLookup && balanceQuery.isSuccess && (
            <div className={styles.balanceResult}>
              <span className={styles.detailLabel}>{t('converted_balance')}</span>
              <strong className={styles.balanceValue}>
                {formatValue(balanceQuery.data)} {balanceLookup.currencyCode}
              </strong>
            </div>
          )}
        </div>
      </Card>

      {isManager && (
        <Card title={t('delete_account')} subtitle={t('delete_account_subtitle')}>
          <form
            className={styles.formStack}
            onSubmit={handleDeleteAccountSubmit(requestDeleteAccount)}
            noValidate
          >
            <TextField
              id="delete-account-id"
              label={t('account_id')}
              type="number"
              min="1"
              error={deleteAccountErrors.accountId?.message}
              required
              {...registerDeleteAccount('accountId', {
                required: t('account_id_required'),
                validate: (value) => validatePositiveId(value, t('account_id'), t),
              })}
            />

            {deleteAccountErrors.root && (
              <Toast variant="danger" message={deleteAccountErrors.root.message} />
            )}

            <div className={styles.actionsRow}>
              <Button type="submit" variant="danger" disabled={deleteAccountMutation.isPending}>
                {t('delete_account')}
              </Button>
            </div>
          </form>
        </Card>
      )}

      <div className={styles.sectionGrid}>
        <Card
          title={t('summaries_by_email')}
          subtitle={t('summaries_subtitle')}
          bodyClassName={styles.stack}
        >
          {emailAccountsQuery.isError && <Toast variant="danger" message={t('summaries_error')} />}
          <Table
            columns={summaryColumns}
            data={emailAccounts}
            getRowKey={(account, index) =>
              `${account.name ?? 'account'}-${account.dateOpened ?? index}`
            }
            emptyMessage={emailLookup ? t('no_summaries') : t('no_email_loaded')}
            loadingMessage={t('loading_summaries')}
            isLoading={emailAccountsQuery.isLoading}
          />
        </Card>

        <Card
          title={t('profiles_by_customer')}
          subtitle={t('profiles_subtitle')}
          bodyClassName={styles.stack}
        >
          {customerAccountsQuery.isError && (
            <Toast variant="danger" message={t('profiles_error')} />
          )}
          <Table
            columns={profileColumns}
            data={customerAccounts}
            getRowKey={(account, index) =>
              `${account.name ?? 'account'}-${account.dateOpened ?? index}`
            }
            emptyMessage={customerIdLookup ? t('no_profiles') : t('no_customer_loaded')}
            loadingMessage={t('loading_profiles')}
            isLoading={customerAccountsQuery.isLoading}
          />
        </Card>
      </div>

      <Card title={t('account_detail')} bodyClassName={styles.stack}>
        {!accountIdLookup && <p className={styles.mutedText}>{t('no_account_selected')}</p>}

        {accountDetailQuery.isError && (
          <Toast variant="danger" message={t('account_detail_error')} />
        )}

        {accountDetail && (
          <>
            <dl className={styles.profileGrid}>
              <DetailItem label={t('label_account_id')}>{formatValue(accountIdLookup)}</DetailItem>
              <DetailItem label={t('label_name')}>{getAccountName(accountDetail)}</DetailItem>
              <DetailItem label={t('label_category')}>
                {formatValue(accountDetail.category)}
              </DetailItem>
              <DetailItem label={t('label_opened')}>
                {formatValue(accountDetail.dateOpened)}
              </DetailItem>
              <DetailItem label={t('label_status')}>
                <StatusBadge active={getActiveValue(accountDetail)} t={t} />
              </DetailItem>
              <DetailItem label={t('label_customers')}>
                {accountDetail.customers?.length ?? 0}
              </DetailItem>
              <DetailItem label={t('label_cards')}>{accountDetail.cards?.length ?? 0}</DetailItem>
              <DetailItem label={t('label_transactions')}>
                {accountDetail.transactions?.length ?? 0}
              </DetailItem>
            </dl>

            <Table
              columns={customerColumns}
              data={accountDetail.customers ?? []}
              getRowKey={(customer, index) => customer.email ?? index}
              emptyMessage={t('no_customers')}
              caption={t('linked_customers')}
            />

            <Table
              columns={cardColumns}
              data={accountDetail.cards ?? []}
              getRowKey={(card, index) => card.panToken ?? card.panMasked ?? index}
              emptyMessage={t('no_cards')}
              caption={t('label_cards')}
            />

            <Table
              columns={transactionColumns}
              data={accountDetail.transactions ?? []}
              getRowKey={(transaction, index) =>
                `${transaction.timeStamp ?? 'transaction'}-${transaction.description ?? index}`
              }
              emptyMessage={t('no_transactions')}
              caption={t('label_transactions')}
            />
          </>
        )}
      </Card>

      <Modal
        open={Boolean(pendingDelete)}
        title={t('delete_account')}
        onClose={() => setPendingDelete(null)}
        footer={
          <>
            <Button
              type="button"
              variant="secondary"
              disabled={deleteAccountMutation.isPending}
              onClick={() => setPendingDelete(null)}
            >
              {t('cancel')}
            </Button>
            <Button
              type="button"
              variant="danger"
              isLoading={deleteAccountMutation.isPending}
              onClick={confirmDeleteAccount}
            >
              {t('delete_account')}
            </Button>
          </>
        }
      >
        <p className={styles.modalText}>
          {t('delete_confirm_text', { accountId: pendingDelete?.accountId })}
        </p>
      </Modal>
    </div>
  );
}
