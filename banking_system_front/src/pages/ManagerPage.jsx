import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { useTranslation } from 'react-i18next';
import { customerApi } from '../api/customerApi';
import { accountApi } from '../api/accountApi';
import { cardApi } from '../api/cardApi';
import { customerKeys } from '../api/customerQueryKeys';
import { accountKeys } from '../api/accountQueryKeys';
import { cardKeys } from '../api/cardQueryKeys';
import { applyBackendFormErrors } from '../api/formErrors';
import {
  Button,
  Card,
  Modal,
  Select,
  Spinner,
  Table,
  TextField,
  Toast,
  useToast,
} from '../components/ui';
import ScrollStrip from '../components/customer/ScrollStrip';
import {
  DetailItem,
  StatusBadge,
  formatValue,
  getActiveValue,
  getCustomerName,
  getId,
  trimValue,
} from '../components/customer/shared';
import styles from './CustomerPage.module.css';

const DEFAULT_PAGE_SIZE = 20;

function sortTransactionsDesc(transactions) {
  return [...(transactions ?? [])].sort((a, b) => {
    const timeA = new Date(a?.timeStamp ?? 0).getTime();
    const timeB = new Date(b?.timeStamp ?? 0).getTime();
    return timeB - timeA;
  });
}

function cleanValue(value) {
  const trimmed = trimValue(value);
  return trimmed === '' ? undefined : trimmed;
}

function cleanBoolean(value) {
  if (value === '' || value === undefined) return undefined;
  return value === 'true';
}

// Shared Previous/Next control. The filter endpoints return a flat list with
// no total-count metadata, so "is there a next page" is inferred: if this
// page came back full (== page size), there might be more.
function PaginationControls({ page, onPrevious, onNext, canGoNext, isFetching }) {
  const { t } = useTranslation('common');

  return (
    <div className={styles.actionsRow}>
      <Button
        type="button"
        variant="secondary"
        size="sm"
        disabled={page === 0 || isFetching}
        onClick={onPrevious}
      >
        {t('previous')}
      </Button>
      <span className={styles.mutedText}>
        {t('page')} {page + 1}
      </span>
      <Button
        type="button"
        variant="secondary"
        size="sm"
        disabled={!canGoNext || isFetching}
        onClick={onNext}
      >
        {t('next')}
      </Button>
    </div>
  );
}

// Activate/Deactivate/Delete row shared by the customer, account, and card
// detail panels -- same three actions, same loading/disabled wiring, just
// pointed at a different kind + id each time.
function StatusActions({ kind, id, isActive, statusMutation, onDelete }) {
  const { t } = useTranslation('common');

  return (
    <div className={styles.actionsRow}>
      <Button
        type="button"
        variant="secondary"
        size="sm"
        disabled={isActive === true || statusMutation.isPending}
        isLoading={statusMutation.isPending && statusMutation.variables?.action === 'activate'}
        onClick={() => statusMutation.mutate({ kind, id, action: 'activate' })}
      >
        {t('activate')}
      </Button>
      <Button
        type="button"
        variant="secondary"
        size="sm"
        disabled={isActive === false || statusMutation.isPending}
        isLoading={statusMutation.isPending && statusMutation.variables?.action === 'deactivate'}
        onClick={() => statusMutation.mutate({ kind, id, action: 'deactivate' })}
      >
        {t('deactivate')}
      </Button>
      <Button type="button" variant="danger" size="sm" onClick={onDelete}>
        {t('delete')}
      </Button>
    </div>
  );
}

export default function ManagerPage() {
  const queryClient = useQueryClient();
  const { showToast } = useToast();
  const { t } = useTranslation(['common', 'accounts']);

  // ---- Drill-down selection state ----
  const [selectedCustomerId, setSelectedCustomerId] = useState(null);
  const [selectedAccountId, setSelectedAccountId] = useState(null);
  const [selectedCardId, setSelectedCardId] = useState(null);
  const [showTransactions, setShowTransactions] = useState(false);

  const selectCustomer = (customer) => {
    const id = getId(customer);
    const isReopening = String(selectedCustomerId) !== String(id);
    setSelectedCustomerId((current) => (String(current) === String(id) ? null : id));
    setSelectedAccountId(null);
    setSelectedCardId(null);
    setShowTransactions(false);
    if (isReopening) {
      // detail panel renders at the top of the page; bring it into view
      requestAnimationFrame(() => {
        document
          .getElementById('mgr-customer-detail')
          ?.scrollIntoView({ behavior: 'smooth', block: 'start' });
      });
    }
  };

  const selectAccount = (account) => {
    const id = getId(account);
    setSelectedAccountId((current) => (String(current) === String(id) ? null : id));
    setSelectedCardId(null);
    setShowTransactions(false);
  };

  const selectCard = (card) => {
    const id = getId(card);
    setSelectedCardId((current) => (String(current) === String(id) ? null : id));
  };

  // ---- Add a joint owner to the selected account (resolve customer by email,
  // ---- then link them -- registerCustomerToAccount ADDS, so this makes the
  // ---- account jointly owned rather than transferring it). ----
  const {
    register: registerJointOwner,
    handleSubmit: handleJointOwnerSubmit,
    reset: resetJointOwnerForm,
    setError: setJointOwnerError,
    formState: { errors: jointOwnerErrors, isSubmitting: isJointOwnerSubmitting },
  } = useForm({ defaultValues: { email: '' } });

  const addJointOwnerMutation = useMutation({
    mutationFn: async ({ accountId, email }) => {
      const foundCustomer = await customerApi.getByEmail(email);
      const foundCustomerId = getId(foundCustomer);
      if (!foundCustomerId) {
        const error = new Error('Customer id was not returned.');
        error.missingCustomerId = true;
        throw error;
      }
      await accountApi.registerCustomer(accountId, foundCustomerId);
      return foundCustomer;
    },
    retry: false,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: accountKeys.byId(selectedAccountId) });
      queryClient.invalidateQueries({ queryKey: customerKeys.all });
      showToast({
        title: t('accounts:joint_owner_added', { defaultValue: 'Joint owner added.' }),
        variant: 'success',
      });
      resetJointOwnerForm();
    },
  });

  const submitJointOwner = async (values) => {
    try {
      await addJointOwnerMutation.mutateAsync({
        accountId: selectedAccountId,
        email: trimValue(values.email),
      });
    } catch (error) {
      if (error.missingCustomerId) {
        setJointOwnerError('email', {
          type: 'server',
          message: t('accounts:no_customer_with_email', {
            defaultValue: 'No customer found with that email.',
          }),
        });
        return;
      }
      applyBackendFormErrors(error, setJointOwnerError, ['email']);
    }
  };

  // ---- Drill-down data ----
  const customerDetailQuery = useQuery({
    queryKey: selectedCustomerId
      ? customerKeys.byId(selectedCustomerId)
      : [...customerKeys.all, 'idle'],
    queryFn: () => customerApi.getById(selectedCustomerId),
    enabled: Boolean(selectedCustomerId),
    retry: false,
  });

  const accountDetailQuery = useQuery({
    queryKey: selectedAccountId
      ? accountKeys.byId(selectedAccountId)
      : [...accountKeys.all, 'idle'],
    queryFn: () => accountApi.getById(selectedAccountId),
    enabled: Boolean(selectedAccountId),
    retry: false,
  });

  const cardDetailQuery = useQuery({
    queryKey: selectedCardId ? cardKeys.byId(selectedCardId) : [...cardKeys.all, 'idle'],
    queryFn: () => cardApi.getById(selectedCardId),
    enabled: Boolean(selectedCardId),
    retry: false,
  });

  const customerDetail = customerDetailQuery.data;
  const accountDetail = accountDetailQuery.data;
  const cardDetail = cardDetailQuery.data;

  // ---- Shared delete-confirmation (one modal, parameterized by kind) ----
  const [pendingDelete, setPendingDelete] = useState(null); // { kind, id, label }

  const deleteMutation = useMutation({
    mutationFn: ({ kind, id }) => {
      if (kind === 'customer') return customerApi.delete(id);
      if (kind === 'account') return accountApi.delete(id);
      return cardApi.delete(id);
    },
    retry: false,
    onSuccess: (_, variables) => {
      if (variables.kind === 'customer') {
        queryClient.invalidateQueries({ queryKey: customerKeys.all });
        if (String(selectedCustomerId) === String(variables.id)) {
          setSelectedCustomerId(null);
          setSelectedAccountId(null);
          setSelectedCardId(null);
        }
      } else if (variables.kind === 'account') {
        queryClient.invalidateQueries({ queryKey: accountKeys.all });
        if (String(selectedAccountId) === String(variables.id)) {
          setSelectedAccountId(null);
          setSelectedCardId(null);
        }
      } else {
        queryClient.invalidateQueries({ queryKey: cardKeys.all });
        if (String(selectedCardId) === String(variables.id)) {
          setSelectedCardId(null);
        }
      }
      setPendingDelete(null);
      showToast({ title: t('deleted_successfully'), variant: 'success' });
    },
  });

  const requestDelete = (kind, id, label) => {
    if (!id) return;
    setPendingDelete({ kind, id, label });
  };

  const confirmDelete = () => {
    if (!pendingDelete) return;
    deleteMutation.mutate({ kind: pendingDelete.kind, id: pendingDelete.id });
  };

  // ---- Shared activate/deactivate (one mutation, parameterized by kind) ----
  const statusMutation = useMutation({
    mutationFn: ({ kind, id, action }) => {
      const api = kind === 'customer' ? customerApi : kind === 'account' ? accountApi : cardApi;
      return action === 'activate' ? api.activate(id) : api.deactivate(id);
    },
    retry: false,
    onSuccess: (_, variables) => {
      if (variables.kind === 'customer') {
        queryClient.invalidateQueries({ queryKey: customerKeys.all });
      } else if (variables.kind === 'account') {
        queryClient.invalidateQueries({ queryKey: accountKeys.all });
      } else {
        queryClient.invalidateQueries({ queryKey: cardKeys.all });
      }
      showToast({
        title: t(`${variables.kind}_${variables.action}d`),
        variant: 'success',
      });
    },
  });

  // ---- Search customers (now actionable: CustomerSummaryResponse has id) ----
  const [customerPage, setCustomerPage] = useState(0);
  const [customerFilters, setCustomerFilters] = useState(null);

  const { register: registerCustomerSearch, handleSubmit: handleCustomerSearchSubmit } = useForm({
    defaultValues: { firstName: '', lastName: '', email: '' },
  });

  const customerSearchQuery = useQuery({
    queryKey: customerKeys.filter(customerFilters, customerPage),
    queryFn: () =>
      customerApi.filter(customerFilters, {
        page: customerPage,
        size: DEFAULT_PAGE_SIZE,
        sortBy: 'id',
        sortDirection: 'asc',
      }),
    enabled: Boolean(customerFilters),
    retry: false,
  });

  const submitCustomerSearch = (values) => {
    setCustomerPage(0);
    setCustomerFilters({
      firstName: cleanValue(values.firstName),
      lastName: cleanValue(values.lastName),
      email: cleanValue(values.email),
    });
  };

  const customerResults = customerSearchQuery.data ?? [];

  // ---- Filter accounts (independent of any specific customer) ----
  const [accountPage, setAccountPage] = useState(0);
  const [accountFilters, setAccountFilters] = useState(null);

  const { register: registerAccountFilter, handleSubmit: handleAccountFilterSubmit } = useForm({
    defaultValues: { name: '', category: '', dateOpened: '', isActive: '' },
  });

  const accountFilterQuery = useQuery({
    queryKey: accountKeys.filter(accountFilters, accountPage),
    queryFn: () =>
      accountApi.filter(accountFilters, {
        page: accountPage,
        size: DEFAULT_PAGE_SIZE,
        sortBy: 'id',
        sortDirection: 'asc',
      }),
    enabled: Boolean(accountFilters),
    retry: false,
  });

  const submitAccountFilter = (values) => {
    setAccountPage(0);
    setAccountFilters({
      name: cleanValue(values.name),
      category: cleanValue(values.category),
      dateOpened: cleanValue(values.dateOpened),
      isActive: cleanBoolean(values.isActive),
    });
  };

  const accountResults = accountFilterQuery.data ?? [];

  // ---- Filter cards (independent of any specific account) ----
  const [cardPage, setCardPage] = useState(0);
  const [cardFilters, setCardFilters] = useState(null);

  const { register: registerCardFilter, handleSubmit: handleCardFilterSubmit } = useForm({
    defaultValues: { type: '', brand: '', spendingLimit: '', expirationDate: '' },
  });

  const cardFilterQuery = useQuery({
    queryKey: cardKeys.filter(cardFilters, cardPage),
    queryFn: () =>
      cardApi.filter(cardFilters, {
        page: cardPage,
        size: DEFAULT_PAGE_SIZE,
        sortBy: 'id',
        sortDirection: 'asc',
      }),
    enabled: Boolean(cardFilters),
    retry: false,
  });

  const submitCardFilter = (values) => {
    setCardPage(0);
    setCardFilters({
      type: cleanValue(values.type),
      brand: cleanValue(values.brand),
      spendingLimit: cleanValue(values.spendingLimit),
      expirationDate: cleanValue(values.expirationDate),
    });
  };

  const cardResults = cardFilterQuery.data ?? [];

  // ---- Existing: look up a specific account's customers by account id ----
  const [lookupAccountId, setLookupAccountId] = useState(null);

  const {
    register: registerAccountLookup,
    handleSubmit: handleAccountLookupSubmit,
    formState: { errors: accountLookupErrors },
  } = useForm({ defaultValues: { accountId: '' } });

  const accountCustomersQuery = useQuery({
    queryKey: lookupAccountId
      ? customerKeys.byAccount(lookupAccountId)
      : [...customerKeys.all, 'account', 'idle'],
    queryFn: () => customerApi.getByAccount(lookupAccountId),
    enabled: Boolean(lookupAccountId),
    retry: false,
  });

  const submitAccountLookup = ({ accountId: value }) => {
    setLookupAccountId(trimValue(value));
  };

  const accountCustomers = accountCustomersQuery.data ?? [];

  // ---- Manual delete-by-id forms ----
  const {
    register: registerDeleteCustomer,
    handleSubmit: handleDeleteCustomerSubmit,
    reset: resetDeleteCustomerForm,
  } = useForm({ defaultValues: { customerId: '' } });

  const {
    register: registerDeleteAccount,
    handleSubmit: handleDeleteAccountSubmit,
    reset: resetDeleteAccountForm,
  } = useForm({ defaultValues: { accountId: '' } });

  const {
    register: registerDeleteCard,
    handleSubmit: handleDeleteCardSubmit,
    reset: resetDeleteCardForm,
  } = useForm({ defaultValues: { cardId: '' } });

  const requestManualDelete = (kind, idValue, resetForm) => {
    const id = trimValue(idValue);
    if (!id) return;
    requestDelete(kind, id, `${kind} ${id}`);
    resetForm();
  };

  // ---- Chip renderers for the drill-down strips ----
  const renderAccountChip = (item) => (
    <>
      <span className={styles.chipTitle}>{formatValue(item.name)}</span>
      <span className={styles.chipMeta}>{formatValue(item.category)}</span>
      <StatusBadge active={getActiveValue(item)} />
    </>
  );

  const renderCardChip = (item) => (
    <>
      <span className={styles.chipTitle}>
        {formatValue(item.brand)} &middot; {formatValue(item.type)}
      </span>
      <span className={styles.chipMeta}>{formatValue(item.panMasked)}</span>
      <StatusBadge active={getActiveValue(item)} />
    </>
  );

  // ---- Table column definitions ----
  const customerSearchColumns = [
    {
      key: 'id',
      header: t('common:col_id', { defaultValue: 'ID' }),
      render: (c) => formatValue(getId(c)),
    },
    {
      key: 'name',
      header: t('common:label_name', { defaultValue: 'Name' }),
      render: (c) => getCustomerName(c),
    },
    {
      key: 'email',
      header: t('customer:label_email', { defaultValue: 'Email' }),
      render: (c) => formatValue(c.email),
    },
    {
      key: 'status',
      header: t('customer:col_status', { defaultValue: 'Status' }),
      render: (c) => <StatusBadge active={getActiveValue(c)} />,
    },
    {
      key: 'actions',
      header: t('common:col_actions', { defaultValue: 'Actions' }),
      align: 'right',
      render: (c) => (
        <div className={styles.tableActions}>
          <Button type="button" size="sm" onClick={() => selectCustomer(c)}>
            {t('common:view', { defaultValue: 'View' })}
          </Button>
          <Button
            type="button"
            variant="danger"
            size="sm"
            disabled={deleteMutation.isPending}
            onClick={() => requestDelete('customer', getId(c), getCustomerName(c))}
          >
            {t('delete')}
          </Button>
        </div>
      ),
    },
  ];

  const accountColumns = [
    {
      key: 'id',
      header: t('common:col_id', { defaultValue: 'ID' }),
      render: (a) => formatValue(getId(a)),
    },
    { key: 'name', header: t('accounts:col_name'), render: (a) => formatValue(a.name) },
    { key: 'category', header: t('accounts:col_category'), render: (a) => formatValue(a.category) },
    {
      key: 'status',
      header: t('customer:col_status', { defaultValue: 'Status' }),
      render: (a) => <StatusBadge active={getActiveValue(a)} />,
    },
    {
      key: 'actions',
      header: t('common:col_actions', { defaultValue: 'Actions' }),
      align: 'right',
      render: (a) => (
        <Button
          type="button"
          variant="danger"
          size="sm"
          disabled={deleteMutation.isPending}
          onClick={() => requestDelete('account', getId(a), `account ${getId(a)}`)}
        >
          {t('delete')}
        </Button>
      ),
    },
  ];

  const cardColumns = [
    {
      key: 'id',
      header: t('common:col_id', { defaultValue: 'ID' }),
      render: (c) => formatValue(getId(c)),
    },
    {
      key: 'brand',
      header: t('common:col_brand_type', { defaultValue: 'Brand / Type' }),
      render: (c) => `${formatValue(c.brand)} · ${formatValue(c.type)}`,
    },
    {
      key: 'panMasked',
      header: t('common:col_card_number', { defaultValue: 'Card number' }),
      render: (c) => formatValue(c.panMasked),
    },
    {
      key: 'spendingLimit',
      header: t('common:col_limit'),
      render: (c) => formatValue(c.spendingLimit),
    },
    {
      key: 'status',
      header: t('customer:col_status', { defaultValue: 'Status' }),
      render: (c) => <StatusBadge active={getActiveValue(c)} />,
    },
    {
      key: 'actions',
      header: t('common:col_actions', { defaultValue: 'Actions' }),
      align: 'right',
      render: (c) => (
        <Button
          type="button"
          variant="danger"
          size="sm"
          disabled={deleteMutation.isPending}
          onClick={() => requestDelete('card', getId(c), `card ${getId(c)}`)}
        >
          {t('delete')}
        </Button>
      ),
    },
  ];

  const accountCustomerColumns = [
    {
      key: 'id',
      header: t('common:col_id', { defaultValue: 'ID' }),
      render: (c) => formatValue(getId(c)),
    },
    {
      key: 'name',
      header: t('common:label_name', { defaultValue: 'Name' }),
      render: (c) => getCustomerName(c),
    },
    {
      key: 'email',
      header: t('customer:label_email', { defaultValue: 'Email' }),
      render: (c) => formatValue(c.email),
    },
    {
      key: 'phoneNumber',
      header: t('common:col_phone', { defaultValue: 'Phone' }),
      render: (c) => formatValue(c.phoneNumber),
    },
    {
      key: 'status',
      header: t('customer:col_status', { defaultValue: 'Status' }),
      render: (c) => <StatusBadge active={getActiveValue(c)} />,
    },
    {
      key: 'actions',
      header: t('common:col_actions', { defaultValue: 'Actions' }),
      align: 'right',
      render: (c) => (
        <div className={styles.tableActions}>
          <Button type="button" size="sm" onClick={() => selectCustomer(c)}>
            {t('common:view', { defaultValue: 'View' })}
          </Button>
          <Button
            type="button"
            variant="danger"
            size="sm"
            disabled={deleteMutation.isPending}
            onClick={() => requestDelete('customer', getId(c), getCustomerName(c))}
          >
            {t('delete')}
          </Button>
        </div>
      ),
    },
  ];

  const balanceColumns = [
    {
      key: 'currencyCode',
      header: t('common:col_currency'),
      render: (b) => formatValue(b.currencyCode),
    },
    {
      key: 'amount',
      header: t('common:col_amount'),
      align: 'right',
      render: (b) => formatValue(b.amount),
    },
  ];

  const transactionColumns = [
    {
      key: 'timeStamp',
      header: t('common:col_date', { defaultValue: 'Date' }),
      render: (t) => formatValue(t.timeStamp),
    },
    {
      key: 'transactionType',
      header: t('common:col_type', { defaultValue: 'Type' }),
      render: (t) => formatValue(t.transactionType),
    },
    {
      key: 'amount',
      header: t('common:col_amount'),
      align: 'right',
      render: (t) => formatValue(t.amount),
    },
    {
      key: 'currencyCode',
      header: t('common:col_currency'),
      render: (t) => formatValue(t.currencyCode),
    },
    {
      key: 'status',
      header: t('customer:col_status', { defaultValue: 'Status' }),
      render: (t) => formatValue(t.status),
    },
    {
      key: 'description',
      header: t('common:col_description', { defaultValue: 'Description' }),
      render: (t) => formatValue(t.description),
    },
  ];

  // Constants for select options
  const ACCOUNT_CATEGORIES = [
    { value: 'CHECKING', label: t('customer:category_checking') },
    { value: 'SAVINGS', label: t('customer:category_savings') },
    { value: 'CREDIT', label: t('customer:category_credit') },
  ];

  const CARD_TYPES = [
    { value: 'DEBIT', label: t('customer:type_debit') },
    { value: 'CREDIT', label: t('customer:type_credit') },
  ];

  const CARD_BRANDS = [
    { value: 'VISA', label: t('customer:brand_visa') },
    { value: 'MASTERCARD', label: t('customer:brand_mastercard') },
  ];

  const ACTIVE_STATUS_OPTIONS = [
    { value: 'true', label: t('common:status_active') },
    { value: 'false', label: t('common:status_inactive') },
  ];

  return (
    <div className={styles.page}>
      <header className={styles.pageHeader}>
        <p className={styles.kicker}>{t('customer:manager')}</p>
        <h1 className={styles.title}>{t('customer:manager')}</h1>
      </header>

      {/* ---- Search customers ---- */}
      <Card title={t('common:search_customers')} subtitle={t('common:search_customers_subtitle')}>
        <div className={styles.stack}>
          <form
            className={styles.editForm}
            onSubmit={handleCustomerSearchSubmit(submitCustomerSearch)}
          >
            <div className={styles.editFields}>
              <TextField
                id="mgr-customer-firstname"
                label={t('customer:first_name')}
                {...registerCustomerSearch('firstName')}
              />
              <TextField
                id="mgr-customer-lastname"
                label={t('customer:last_name')}
                {...registerCustomerSearch('lastName')}
              />
              <TextField
                id="mgr-customer-email"
                label={t('customer:label_email', { defaultValue: 'Email' })}
                type="email"
                {...registerCustomerSearch('email')}
              />
            </div>
            <div className={styles.actionsRow}>
              <Button type="submit" size="sm" isLoading={customerSearchQuery.isFetching}>
                {t('common:search')}
              </Button>
            </div>
          </form>

          <Table
            columns={customerSearchColumns}
            data={customerResults}
            getRowKey={(c, index) => getId(c) ?? c.email ?? index}
            emptyMessage={
              customerFilters
                ? t('common:no_customers_matched')
                : t('common:enter_search_and_press_search')
            }
            isLoading={customerSearchQuery.isLoading}
          />

          {customerFilters && (
            <PaginationControls
              page={customerPage}
              onPrevious={() => setCustomerPage((p) => Math.max(0, p - 1))}
              onNext={() => setCustomerPage((p) => p + 1)}
              canGoNext={customerResults.length === DEFAULT_PAGE_SIZE}
              isFetching={customerSearchQuery.isFetching}
            />
          )}
        </div>
      </Card>

      {/* ---- Customer detail (drilled in) ---- */}
      {selectedCustomerId && (
        <Card
          id="mgr-customer-detail"
          title={t('common:customer_detail', { defaultValue: 'Customer detail' })}
        >
          {customerDetailQuery.isLoading && (
            <div className={styles.centerState}>
              <Spinner
                label={t('common:loading_customer', { defaultValue: 'Loading customer...' })}
              />
            </div>
          )}
          {customerDetailQuery.isError && (
            <Toast
              variant="danger"
              message={t('common:could_not_load_customer', {
                defaultValue: "We couldn't load that customer.",
              })}
            />
          )}
          {customerDetail && (
            <div className={styles.stack}>
              <dl className={styles.profileGrid}>
                <DetailItem label={t('customer:label_name')}>
                  {getCustomerName(customerDetail)}
                </DetailItem>
                <DetailItem label={t('customer:col_status', { defaultValue: 'Status' })}>
                  <StatusBadge active={getActiveValue(customerDetail)} />
                </DetailItem>
                <DetailItem label={t('customer:label_email')}>
                  {formatValue(customerDetail.email)}
                </DetailItem>
                <DetailItem label={t('customer:label_phone')}>
                  {formatValue(customerDetail.phoneNumber)}
                </DetailItem>
                <DetailItem label={t('customer:label_dob')}>
                  {formatValue(customerDetail.dateOfBirth)}
                </DetailItem>
                <DetailItem label={t('customer:label_address')}>
                  {formatValue(customerDetail.address)}
                </DetailItem>
              </dl>

              <StatusActions
                kind="customer"
                id={selectedCustomerId}
                isActive={getActiveValue(customerDetail)}
                statusMutation={statusMutation}
                onDelete={() =>
                  requestDelete('customer', selectedCustomerId, getCustomerName(customerDetail))
                }
              />

              <ScrollStrip
                items={customerDetail.accounts ?? []}
                getKey={(item) => getId(item)}
                renderItem={renderAccountChip}
                selectedKey={selectedAccountId}
                onSelect={selectAccount}
                emptyMessage={t('accounts:no_accounts', {
                  defaultValue: 'This customer has no accounts.',
                })}
                ariaLabel={t('common:customer_accounts', { defaultValue: 'Customer accounts' })}
              />
            </div>
          )}
        </Card>
      )}

      {/* ---- Account detail (drilled in) ---- */}
      {selectedAccountId && (
        <Card title={t('accounts:account_detail')}>
          {accountDetailQuery.isLoading && (
            <div className={styles.centerState}>
              <Spinner
                label={t('common:loading_account', { defaultValue: 'Loading account...' })}
              />
            </div>
          )}
          {accountDetailQuery.isError && (
            <Toast
              variant="danger"
              message={t('common:could_not_load_account', {
                defaultValue: "We couldn't load that account.",
              })}
            />
          )}
          {accountDetail && (
            <div className={styles.stack}>
              <dl className={styles.profileGrid}>
                <DetailItem label={t('accounts:label_name')}>
                  {formatValue(accountDetail.name)}
                </DetailItem>
                <DetailItem label={t('customer:col_status', { defaultValue: 'Status' })}>
                  <StatusBadge active={getActiveValue(accountDetail)} />
                </DetailItem>
                <DetailItem label={t('accounts:label_category')}>
                  {formatValue(accountDetail.category)}
                </DetailItem>
                <DetailItem label={t('accounts:label_opened')}>
                  {formatValue(accountDetail.dateOpened)}
                </DetailItem>
              </dl>

              <StatusActions
                kind="account"
                id={selectedAccountId}
                isActive={getActiveValue(accountDetail)}
                statusMutation={statusMutation}
                onDelete={() =>
                  requestDelete('account', selectedAccountId, `account ${selectedAccountId}`)
                }
              />

              <div className={styles.actionsRow}>
                <Button
                  type="button"
                  variant="secondary"
                  size="sm"
                  aria-expanded={showTransactions}
                  onClick={() => setShowTransactions((visible) => !visible)}
                >
                  {showTransactions
                    ? t('customer:hide_transactions')
                    : t('customer:show_transactions')}
                </Button>
              </div>

              {showTransactions && (
                <div className={styles.scrollTableWrap}>
                  <Table
                    columns={transactionColumns}
                    data={sortTransactionsDesc(accountDetail.transactions)}
                    getRowKey={(t, index) => `${t.timeStamp ?? 'txn'}-${index}`}
                    emptyMessage={t('accounts:no_transactions', {
                      defaultValue: 'No transactions yet.',
                    })}
                    caption={t('common:transactions', { defaultValue: 'Transactions' })}
                  />
                </div>
              )}

              <ScrollStrip
                items={accountDetail.cards ?? []}
                getKey={(item) => getId(item)}
                renderItem={renderCardChip}
                selectedKey={selectedCardId}
                onSelect={selectCard}
                emptyMessage={t('accounts:no_cards', {
                  defaultValue: 'This account has no cards.',
                })}
                ariaLabel={t('common:account_cards', { defaultValue: 'Account cards' })}
              />

              <form
                className={styles.editForm}
                onSubmit={handleJointOwnerSubmit(submitJointOwner)}
                noValidate
              >
                <p className={styles.mutedText}>
                  {t('accounts:add_joint_owner_hint', {
                    defaultValue:
                      'Link another customer to this account by their email to make it a joint account.',
                  })}
                </p>
                <div className={styles.editFields}>
                  <TextField
                    id="mgr-joint-owner-email"
                    label={t('accounts:joint_owner_email', { defaultValue: 'Joint owner email' })}
                    type="email"
                    required
                    error={jointOwnerErrors.email?.message}
                    {...registerJointOwner('email', {
                      required: t('accounts:email_required', {
                        defaultValue: 'Email is required.',
                      }),
                    })}
                  />
                </div>
                <div className={styles.actionsRow}>
                  <Button
                    type="submit"
                    size="sm"
                    isLoading={addJointOwnerMutation.isPending || isJointOwnerSubmitting}
                  >
                    {t('accounts:add_joint_owner', { defaultValue: 'Add joint owner' })}
                  </Button>
                </div>
              </form>
            </div>
          )}
        </Card>
      )}

      {/* ---- Card detail (drilled in) ---- */}
      {selectedCardId && (
        <Card title={t('common:card_detail', { defaultValue: 'Card detail' })}>
          {cardDetailQuery.isLoading && (
            <div className={styles.centerState}>
              <Spinner label={t('common:loading_card', { defaultValue: 'Loading card...' })} />
            </div>
          )}
          {cardDetailQuery.isError && (
            <Toast
              variant="danger"
              message={t('common:could_not_load_card', {
                defaultValue: "We couldn't load that card.",
              })}
            />
          )}
          {cardDetail && (
            <div className={styles.stack}>
              <dl className={styles.profileGrid}>
                <DetailItem label={t('common:col_brand', { defaultValue: 'Brand' })}>
                  {formatValue(cardDetail.brand)}
                </DetailItem>
                <DetailItem label={t('common:col_type', { defaultValue: 'Type' })}>
                  {formatValue(cardDetail.type)}
                </DetailItem>
                <DetailItem label={t('customer:col_status', { defaultValue: 'Status' })}>
                  <StatusBadge active={getActiveValue(cardDetail)} />
                </DetailItem>
                <DetailItem label={t('customer:label_spending_limit')}>
                  {formatValue(cardDetail.spendingLimit)}
                </DetailItem>
                <DetailItem label={t('customer:label_expiration')}>
                  {formatValue(cardDetail.expirationDate)}
                </DetailItem>
                <DetailItem label={t('customer:label_card_number')}>
                  {formatValue(cardDetail.panToken)}
                </DetailItem>
              </dl>

              <StatusActions
                kind="card"
                id={selectedCardId}
                isActive={getActiveValue(cardDetail)}
                statusMutation={statusMutation}
                onDelete={() => requestDelete('card', selectedCardId, `card ${selectedCardId}`)}
              />

              <Table
                columns={balanceColumns}
                data={cardDetail.cardBalances ?? []}
                getRowKey={(b, index) => b.currencyCode ?? index}
                emptyMessage={t('accounts:no_balances', {
                  defaultValue: 'No balances on this card.',
                })}
                caption={t('common:card_balances', { defaultValue: 'Card balances' })}
              />
            </div>
          )}
        </Card>
      )}

      {/* ---- Filter accounts (independent of any specific customer) ---- */}
      <Card title={t('common:filter_accounts', { defaultValue: 'Filter accounts' })}>
        <div className={styles.stack}>
          <form
            className={styles.editForm}
            onSubmit={handleAccountFilterSubmit(submitAccountFilter)}
          >
            <div className={styles.editFields}>
              <TextField
                id="mgr-account-name"
                label={t('accounts:account_name')}
                {...registerAccountFilter('name')}
              />
              <Select
                id="mgr-account-category"
                label={t('accounts:category')}
                placeholder={t('common:any_category', { defaultValue: 'Any category' })}
                options={ACCOUNT_CATEGORIES}
                {...registerAccountFilter('category')}
              />
              <TextField
                id="mgr-account-date-opened"
                label={t('accounts:label_opened')}
                type="date"
                {...registerAccountFilter('dateOpened')}
              />
              <Select
                id="mgr-account-status"
                label={t('customer:col_status', { defaultValue: 'Status' })}
                placeholder={t('common:any_status', { defaultValue: 'Any status' })}
                options={ACTIVE_STATUS_OPTIONS}
                {...registerAccountFilter('isActive')}
              />
            </div>
            <div className={styles.actionsRow}>
              <Button type="submit" size="sm" isLoading={accountFilterQuery.isFetching}>
                {t('common:search')}
              </Button>
            </div>
          </form>

          <Table
            columns={accountColumns}
            data={accountResults}
            getRowKey={(a, index) => getId(a) ?? index}
            emptyMessage={
              accountFilters
                ? t('common:no_accounts_matched', { defaultValue: 'No accounts matched.' })
                : t('common:enter_search_and_press_search', {
                    defaultValue: 'Enter a search and press Search.',
                  })
            }
            isLoading={accountFilterQuery.isLoading}
          />

          {accountFilters && (
            <PaginationControls
              page={accountPage}
              onPrevious={() => setAccountPage((p) => Math.max(0, p - 1))}
              onNext={() => setAccountPage((p) => p + 1)}
              canGoNext={accountResults.length === DEFAULT_PAGE_SIZE}
              isFetching={accountFilterQuery.isFetching}
            />
          )}
        </div>
      </Card>

      {/* ---- Filter cards (independent of any specific account) ---- */}
      <Card title={t('common:filter_cards', { defaultValue: 'Filter cards' })}>
        <div className={styles.stack}>
          <form className={styles.editForm} onSubmit={handleCardFilterSubmit(submitCardFilter)}>
            <div className={styles.editFields}>
              <Select
                id="mgr-card-type"
                label={t('accounts:card_type')}
                placeholder={t('common:any_type', { defaultValue: 'Any type' })}
                options={CARD_TYPES}
                {...registerCardFilter('type')}
              />
              <Select
                id="mgr-card-brand"
                label={t('accounts:card_brand')}
                placeholder={t('common:any_brand', { defaultValue: 'Any brand' })}
                options={CARD_BRANDS}
                {...registerCardFilter('brand')}
              />
              <TextField
                id="mgr-card-spending-limit"
                label={t('customer:label_spending_limit')}
                type="number"
                step="0.01"
                {...registerCardFilter('spendingLimit')}
              />
              <TextField
                id="mgr-card-expiration"
                label={t('accounts:label_opened', { defaultValue: 'Expiration date' })}
                type="date"
                {...registerCardFilter('expirationDate')}
              />
            </div>
            <div className={styles.actionsRow}>
              <Button type="submit" size="sm" isLoading={cardFilterQuery.isFetching}>
                {t('common:search')}
              </Button>
            </div>
          </form>

          <Table
            columns={cardColumns}
            data={cardResults}
            getRowKey={(c, index) => getId(c) ?? index}
            emptyMessage={
              cardFilters
                ? t('common:no_cards_matched', { defaultValue: 'No cards matched.' })
                : t('common:enter_search_and_press_search', {
                    defaultValue: 'Enter a search and press Search.',
                  })
            }
            isLoading={cardFilterQuery.isLoading}
          />

          {cardFilters && (
            <PaginationControls
              page={cardPage}
              onPrevious={() => setCardPage((p) => Math.max(0, p - 1))}
              onNext={() => setCardPage((p) => p + 1)}
              canGoNext={cardResults.length === DEFAULT_PAGE_SIZE}
              isFetching={cardFilterQuery.isFetching}
            />
          )}
        </div>
      </Card>

      {/* ---- Existing: customers on a specific account ---- */}
      <Card title={t('accounts:linked_customers', { defaultValue: 'Account customers' })}>
        <div className={styles.stack}>
          <form
            className={styles.inlineForm}
            onSubmit={handleAccountLookupSubmit(submitAccountLookup)}
          >
            <TextField
              id="mgr-account-lookup-id"
              label={t('common:account_id')}
              type="number"
              min="1"
              error={accountLookupErrors.accountId?.message}
              required
              {...registerAccountLookup('accountId', {
                required: t('common:account_id_required'),
                validate: (value) =>
                  /^\d+$/.test(trimValue(value)) || t('common:account_id_positive'),
              })}
            />
            <Button type="submit" isLoading={accountCustomersQuery.isFetching}>
              {t('customer:load_customers', { defaultValue: 'Load customers' })}
            </Button>
          </form>

          <Table
            columns={accountCustomerColumns}
            data={accountCustomers}
            getRowKey={(c, index) => getId(c) ?? c.email ?? index}
            emptyMessage={
              lookupAccountId
                ? t('common:no_customers_for_account', {
                    defaultValue: 'No customers found for this account.',
                  })
                : t('accounts:no_account_selected', {
                    defaultValue: 'No account loaded.',
                  })
            }
            isLoading={accountCustomersQuery.isLoading}
          />
        </div>
      </Card>

      {/* ---- Manual delete-by-id ---- */}
      <Card title={t('common:delete_by_id', { defaultValue: 'Delete by ID' })}>
        <div className={styles.stack}>
          <form
            className={styles.inlineForm}
            onSubmit={handleDeleteCustomerSubmit((values) =>
              requestManualDelete('customer', values.customerId, resetDeleteCustomerForm)
            )}
          >
            <TextField
              id="mgr-delete-customer-id"
              label={t('common:customer_id')}
              type="number"
              min="1"
              {...registerDeleteCustomer('customerId', { required: true })}
            />
            <Button type="submit" variant="danger" disabled={deleteMutation.isPending}>
              {t('customer:delete_customer')}
            </Button>
          </form>

          <form
            className={styles.inlineForm}
            onSubmit={handleDeleteAccountSubmit((values) =>
              requestManualDelete('account', values.accountId, resetDeleteAccountForm)
            )}
          >
            <TextField
              id="mgr-delete-account-id"
              label={t('common:account_id')}
              type="number"
              min="1"
              {...registerDeleteAccount('accountId', { required: true })}
            />
            <Button type="submit" variant="danger" disabled={deleteMutation.isPending}>
              {t('accounts:delete_account')}
            </Button>
          </form>

          <form
            className={styles.inlineForm}
            onSubmit={handleDeleteCardSubmit((values) =>
              requestManualDelete('card', values.cardId, resetDeleteCardForm)
            )}
          >
            <TextField
              id="mgr-delete-card-id"
              label={t('cards:card_id')}
              type="number"
              min="1"
              {...registerDeleteCard('cardId', { required: true })}
            />
            <Button type="submit" variant="danger" disabled={deleteMutation.isPending}>
              {t('cards:op_delete_card')}
            </Button>
          </form>
        </div>
      </Card>

      <Modal
        open={Boolean(pendingDelete)}
        title={t('common:confirm_delete')}
        onClose={() => setPendingDelete(null)}
        footer={
          <>
            <Button
              type="button"
              variant="secondary"
              disabled={deleteMutation.isPending}
              onClick={() => setPendingDelete(null)}
            >
              {t('common:cancel')}
            </Button>
            <Button
              type="button"
              variant="danger"
              isLoading={deleteMutation.isPending}
              onClick={confirmDelete}
            >
              {t('delete')}
            </Button>
          </>
        }
      >
        <p className={styles.modalText}>
          {t('common:delete_confirm', {
            defaultValue: 'Delete {{label}}? This action cannot be undone.',
            label: pendingDelete?.label ?? t('common:this_record', { defaultValue: 'this record' }),
          })}
        </p>
      </Modal>
    </div>
  );
}
