import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { customerApi } from '../api/customerApi';
import { accountApi } from '../api/accountApi';
import { cardApi } from '../api/cardApi';
import { customerKeys } from '../api/customerQueryKeys';
import { accountKeys } from '../api/accountQueryKeys';
import { cardKeys } from '../api/cardQueryKeys';
import { Button, Card, Modal, Select, Table, TextField, useToast } from '../components/ui';
import {
  StatusBadge,
  formatValue,
  getActiveValue,
  getCustomerName,
  getId,
  trimValue,
} from '../components/customer/shared';
import styles from './CustomerPage.module.css';

const DEFAULT_PAGE_SIZE = 20;

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

const ACTIVE_STATUS_OPTIONS = [
  { value: 'true', label: 'Active' },
  { value: 'false', label: 'Inactive' },
];

// Blank string means "don't filter on this field" -- convert to undefined so
// it's dropped from the querystring rather than sent as a literal empty value.
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
  return (
    <div className={styles.actionsRow}>
      <Button
        type="button"
        variant="secondary"
        size="sm"
        disabled={page === 0 || isFetching}
        onClick={onPrevious}
      >
        Previous
      </Button>
      <span className={styles.mutedText}>Page {page + 1}</span>
      <Button
        type="button"
        variant="secondary"
        size="sm"
        disabled={!canGoNext || isFetching}
        onClick={onNext}
      >
        Next
      </Button>
    </div>
  );
}

export default function ManagerPage() {
  const queryClient = useQueryClient();
  const { showToast } = useToast();

  // Shared delete-confirmation state -- one modal, parameterized by which
  // kind of record is being deleted.
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
      } else if (variables.kind === 'account') {
        queryClient.invalidateQueries({ queryKey: accountKeys.all });
      } else {
        queryClient.invalidateQueries({ queryKey: cardKeys.all });
      }
      setPendingDelete(null);
      showToast({ title: 'Deleted successfully.', variant: 'success' });
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

  // ---- Search customers by name (read-only: CustomerSummaryResponse has no id) ----
  const [customerPage, setCustomerPage] = useState(0);
  const [customerFilters, setCustomerFilters] = useState(null);

  const { register: registerCustomerSearch, handleSubmit: handleCustomerSearchSubmit } = useForm({
    defaultValues: { firstName: '', lastName: '' },
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
    });
  };

  const customerResults = customerSearchQuery.data ?? [];

  // ---- Filter accounts (actionable: AccountSummaryResponse has id) ----
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

  // ---- Filter cards (actionable: CardSummaryResponse has id) ----
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

  // ---- Table column definitions ----
  const customerSearchColumns = [
    { key: 'id', header: 'ID', render: (c) => formatValue(getId(c)) },
    { key: 'name', header: 'Name', render: (c) => getCustomerName(c) },
    { key: 'email', header: 'Email', render: (c) => formatValue(c.email) },
    {
      key: 'actions',
      header: 'Actions',
      align: 'right',
      render: (c) => (
        <Button
          type="button"
          variant="danger"
          size="sm"
          disabled={deleteMutation.isPending}
          onClick={() => requestDelete('customer', getId(c), getCustomerName(c))}
        >
          Delete
        </Button>
      ),
    },
  ];

  const accountColumns = [
    { key: 'id', header: 'ID', render: (a) => formatValue(getId(a)) },
    { key: 'name', header: 'Name', render: (a) => formatValue(a.name) },
    { key: 'category', header: 'Category', render: (a) => formatValue(a.category) },
    { key: 'status', header: 'Status', render: (a) => <StatusBadge active={getActiveValue(a)} /> },
    {
      key: 'actions',
      header: 'Actions',
      align: 'right',
      render: (a) => (
        <Button
          type="button"
          variant="danger"
          size="sm"
          disabled={deleteMutation.isPending}
          onClick={() => requestDelete('account', getId(a), `account ${getId(a)}`)}
        >
          Delete
        </Button>
      ),
    },
  ];

  const cardColumns = [
    { key: 'id', header: 'ID', render: (c) => formatValue(getId(c)) },
    {
      key: 'brand',
      header: 'Brand / Type',
      render: (c) => `${formatValue(c.brand)} · ${formatValue(c.type)}`,
    },
    { key: 'panMasked', header: 'Card number', render: (c) => formatValue(c.panMasked) },
    { key: 'spendingLimit', header: 'Limit', render: (c) => formatValue(c.spendingLimit) },
    { key: 'status', header: 'Status', render: (c) => <StatusBadge active={getActiveValue(c)} /> },
    {
      key: 'actions',
      header: 'Actions',
      align: 'right',
      render: (c) => (
        <Button
          type="button"
          variant="danger"
          size="sm"
          disabled={deleteMutation.isPending}
          onClick={() => requestDelete('card', getId(c), `card ${getId(c)}`)}
        >
          Delete
        </Button>
      ),
    },
  ];

  const accountCustomerColumns = [
    { key: 'id', header: 'ID', render: (c) => formatValue(getId(c)) },
    { key: 'name', header: 'Name', render: (c) => getCustomerName(c) },
    { key: 'email', header: 'Email', render: (c) => formatValue(c.email) },
    { key: 'phoneNumber', header: 'Phone', render: (c) => formatValue(c.phoneNumber) },
    { key: 'status', header: 'Status', render: (c) => <StatusBadge active={getActiveValue(c)} /> },
    {
      key: 'actions',
      header: 'Actions',
      align: 'right',
      render: (c) => (
        <Button
          type="button"
          variant="danger"
          size="sm"
          disabled={deleteMutation.isPending}
          onClick={() => requestDelete('customer', getId(c), getCustomerName(c))}
        >
          Delete
        </Button>
      ),
    },
  ];

  return (
    <div className={styles.page}>
      <header className={styles.pageHeader}>
        <p className={styles.kicker}>Manager</p>
        <h1 className={styles.title}>Manager</h1>
      </header>

      {/* ---- Search customers by name (read-only) ---- */}
      <Card title="Search customers" subtitle="Search by first or last name.">
        <div className={styles.stack}>
          <form
            className={styles.editForm}
            onSubmit={handleCustomerSearchSubmit(submitCustomerSearch)}
          >
            <div className={styles.editFields}>
              <TextField
                id="mgr-customer-firstname"
                label="First name"
                {...registerCustomerSearch('firstName')}
              />
              <TextField
                id="mgr-customer-lastname"
                label="Last name"
                {...registerCustomerSearch('lastName')}
              />
            </div>
            <div className={styles.actionsRow}>
              <Button type="submit" size="sm" isLoading={customerSearchQuery.isFetching}>
                Search
              </Button>
            </div>
          </form>

          <Table
            columns={customerSearchColumns}
            data={customerResults}
            getRowKey={(c, index) => getId(c) ?? c.email ?? index}
            emptyMessage={
              customerFilters ? 'No customers matched.' : 'Enter a search and press Search.'
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

      {/* ---- Filter accounts ---- */}
      <Card title="Filter accounts">
        <div className={styles.stack}>
          <form
            className={styles.editForm}
            onSubmit={handleAccountFilterSubmit(submitAccountFilter)}
          >
            <div className={styles.editFields}>
              <TextField
                id="mgr-account-name"
                label="Account name"
                {...registerAccountFilter('name')}
              />
              <Select
                id="mgr-account-category"
                label="Category"
                placeholder="Any category"
                options={ACCOUNT_CATEGORIES}
                {...registerAccountFilter('category')}
              />
              <TextField
                id="mgr-account-date-opened"
                label="Date opened"
                type="date"
                {...registerAccountFilter('dateOpened')}
              />
              <Select
                id="mgr-account-status"
                label="Status"
                placeholder="Any status"
                options={ACTIVE_STATUS_OPTIONS}
                {...registerAccountFilter('isActive')}
              />
            </div>
            <div className={styles.actionsRow}>
              <Button type="submit" size="sm" isLoading={accountFilterQuery.isFetching}>
                Search
              </Button>
            </div>
          </form>

          <Table
            columns={accountColumns}
            data={accountResults}
            getRowKey={(a, index) => getId(a) ?? index}
            emptyMessage={
              accountFilters ? 'No accounts matched.' : 'Enter a search and press Search.'
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

      {/* ---- Filter cards ---- */}
      <Card title="Filter cards">
        <div className={styles.stack}>
          <form className={styles.editForm} onSubmit={handleCardFilterSubmit(submitCardFilter)}>
            <div className={styles.editFields}>
              <Select
                id="mgr-card-type"
                label="Card type"
                placeholder="Any type"
                options={CARD_TYPES}
                {...registerCardFilter('type')}
              />
              <Select
                id="mgr-card-brand"
                label="Card brand"
                placeholder="Any brand"
                options={CARD_BRANDS}
                {...registerCardFilter('brand')}
              />
              <TextField
                id="mgr-card-spending-limit"
                label="Spending limit"
                type="number"
                step="0.01"
                {...registerCardFilter('spendingLimit')}
              />
              <TextField
                id="mgr-card-expiration"
                label="Expiration date"
                type="date"
                {...registerCardFilter('expirationDate')}
              />
            </div>
            <div className={styles.actionsRow}>
              <Button type="submit" size="sm" isLoading={cardFilterQuery.isFetching}>
                Search
              </Button>
            </div>
          </form>

          <Table
            columns={cardColumns}
            data={cardResults}
            getRowKey={(c, index) => getId(c) ?? index}
            emptyMessage={cardFilters ? 'No cards matched.' : 'Enter a search and press Search.'}
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
      <Card title="Account customers">
        <div className={styles.stack}>
          <form
            className={styles.inlineForm}
            onSubmit={handleAccountLookupSubmit(submitAccountLookup)}
          >
            <TextField
              id="mgr-account-lookup-id"
              label="Account ID"
              type="number"
              min="1"
              error={accountLookupErrors.accountId?.message}
              required
              {...registerAccountLookup('accountId', {
                required: 'Account ID is required.',
                validate: (value) =>
                  /^\d+$/.test(trimValue(value)) || 'Account ID must be a positive number.',
              })}
            />
            <Button type="submit" isLoading={accountCustomersQuery.isFetching}>
              Load customers
            </Button>
          </form>

          <Table
            columns={accountCustomerColumns}
            data={accountCustomers}
            getRowKey={(c, index) => getId(c) ?? c.email ?? index}
            emptyMessage={
              lookupAccountId ? 'No customers found for this account.' : 'No account loaded.'
            }
            isLoading={accountCustomersQuery.isLoading}
          />
        </div>
      </Card>

      {/* ---- Manual delete-by-id ---- */}
      <Card title="Delete by ID">
        <div className={styles.stack}>
          <form
            className={styles.inlineForm}
            onSubmit={handleDeleteCustomerSubmit((values) =>
              requestManualDelete('customer', values.customerId, resetDeleteCustomerForm)
            )}
          >
            <TextField
              id="mgr-delete-customer-id"
              label="Customer ID"
              type="number"
              min="1"
              {...registerDeleteCustomer('customerId', { required: true })}
            />
            <Button type="submit" variant="danger" disabled={deleteMutation.isPending}>
              Delete customer
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
              label="Account ID"
              type="number"
              min="1"
              {...registerDeleteAccount('accountId', { required: true })}
            />
            <Button type="submit" variant="danger" disabled={deleteMutation.isPending}>
              Delete account
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
              label="Card ID"
              type="number"
              min="1"
              {...registerDeleteCard('cardId', { required: true })}
            />
            <Button type="submit" variant="danger" disabled={deleteMutation.isPending}>
              Delete card
            </Button>
          </form>
        </div>
      </Card>

      <Modal
        open={Boolean(pendingDelete)}
        title="Confirm delete"
        onClose={() => setPendingDelete(null)}
        footer={
          <>
            <Button
              type="button"
              variant="secondary"
              disabled={deleteMutation.isPending}
              onClick={() => setPendingDelete(null)}
            >
              Cancel
            </Button>
            <Button
              type="button"
              variant="danger"
              isLoading={deleteMutation.isPending}
              onClick={confirmDelete}
            >
              Delete
            </Button>
          </>
        }
      >
        <p className={styles.modalText}>
          Delete {pendingDelete?.label ?? 'this record'}? This action cannot be undone.
        </p>
      </Modal>
    </div>
  );
}
