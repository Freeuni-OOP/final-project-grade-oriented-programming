import { useMemo, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { cardApi } from '../api/cardApi';
import { cardKeys } from '../api/cardQueryKeys';
import { applyBackendFormErrors } from '../api/formErrors';
import { useAuth } from '../components/AuthContext';
import { Button, Card, Modal, Select, Table, TextField, Toast, useToast } from '../components/ui';
import styles from './CardsPage.module.css';

const CURRENCY_OPTIONS = [
  { value: 'GEL', label: 'GEL' },
  { value: 'USD', label: 'USD' },
  { value: 'EUR', label: 'EUR' },
  { value: 'GBP', label: 'GBP' },
];

const DEPOSIT_FIELDS = ['amountToDeposit', 'currencyCode'];
const WITHDRAW_FIELDS = ['amountToWithdraw', 'currencyCode'];
const TRANSFER_FIELDS = ['receiverCardId', 'amount', 'currencyCode'];
const EXCHANGE_FIELDS = ['amount', 'fromCurrencyCode', 'toCurrencyCode'];
const ADD_CURRENCY_FIELDS = ['currencyCode'];

function trimValue(value) {
  return String(value ?? '').trim();
}

function normalizeCurrency(value) {
  return trimValue(value).toUpperCase();
}

function formatValue(value) {
  return value === null || value === undefined || value === '' ? 'Not provided' : String(value);
}

function validatePositiveAmount(value) {
  return Number(value) > 0 || 'Amount must be positive.';
}

function validatePositiveId(value, label) {
  const valueText = trimValue(value);
  return (
    (/^\d+$/.test(valueText) && Number(valueText) > 0) || `${label} must be a positive number.`
  );
}

function getActiveValue(record) {
  if (typeof record?.active === 'boolean') return record.active;
  if (typeof record?.isActive === 'boolean') return record.isActive;
  return null;
}

function buildDepositPayload(values) {
  return {
    amountToDeposit: trimValue(values.amountToDeposit),
    currencyCode: normalizeCurrency(values.currencyCode),
  };
}

function buildWithdrawPayload(values) {
  return {
    amountToWithdraw: trimValue(values.amountToWithdraw),
    currencyCode: normalizeCurrency(values.currencyCode),
  };
}

function buildTransferPayload(values, senderCardId) {
  return {
    senderCardId: Number(senderCardId),
    receiverCardId: Number(trimValue(values.receiverCardId)),
    amount: trimValue(values.amount),
    currencyCode: normalizeCurrency(values.currencyCode),
  };
}

function buildExchangePayload(values) {
  return {
    amount: trimValue(values.amount),
    fromCurrencyCode: normalizeCurrency(values.fromCurrencyCode),
    toCurrencyCode: normalizeCurrency(values.toCurrencyCode),
  };
}

function DetailItem({ label, children }) {
  return (
    <div className={styles.detailItem}>
      <dt className={styles.detailLabel}>{label}</dt>
      <dd className={styles.detailValue}>{children}</dd>
    </div>
  );
}

function StatusBadge({ active }) {
  const className =
    active === true ? styles.active : active === false ? styles.inactive : styles.unknown;
  const label = active === true ? 'Active' : active === false ? 'Inactive' : 'Unknown';

  return <span className={`${styles.badge} ${className}`}>{label}</span>;
}

function ExpirationBadge({ expired }) {
  const className =
    expired === true ? styles.expired : expired === false ? styles.valid : styles.unknown;
  const label = expired === true ? 'Expired' : expired === false ? 'Valid' : 'Unknown';

  return <span className={`${styles.badge} ${className}`}>{label}</span>;
}

export default function CardsPage() {
  const queryClient = useQueryClient();
  const { authority } = useAuth();
  const { showToast } = useToast();
  const [selectedCardId, setSelectedCardId] = useState(null);
  const [pendingDelete, setPendingDelete] = useState(false);
  const isManager = authority === 'MANAGER';
  const hasSelectedCard = Boolean(selectedCardId);

  const {
    register: registerLookup,
    handleSubmit: handleLookupSubmit,
    formState: { errors: lookupErrors },
  } = useForm({ defaultValues: { cardId: '' } });

  const {
    register: registerDeposit,
    handleSubmit: handleDepositSubmit,
    reset: resetDeposit,
    setError: setDepositError,
    formState: { errors: depositErrors, isSubmitting: isDepositSubmitting },
  } = useForm({ defaultValues: { amountToDeposit: '', currencyCode: '' } });

  const {
    register: registerWithdraw,
    handleSubmit: handleWithdrawSubmit,
    reset: resetWithdraw,
    setError: setWithdrawError,
    formState: { errors: withdrawErrors, isSubmitting: isWithdrawSubmitting },
  } = useForm({ defaultValues: { amountToWithdraw: '', currencyCode: '' } });

  const {
    register: registerTransfer,
    handleSubmit: handleTransferSubmit,
    reset: resetTransfer,
    setError: setTransferError,
    formState: { errors: transferErrors, isSubmitting: isTransferSubmitting },
  } = useForm({ defaultValues: { receiverCardId: '', amount: '', currencyCode: '' } });

  const {
    register: registerExchange,
    handleSubmit: handleExchangeSubmit,
    reset: resetExchange,
    setError: setExchangeError,
    formState: { errors: exchangeErrors, isSubmitting: isExchangeSubmitting },
  } = useForm({ defaultValues: { amount: '', fromCurrencyCode: '', toCurrencyCode: '' } });

  const {
    register: registerAddCurrency,
    handleSubmit: handleAddCurrencySubmit,
    reset: resetAddCurrency,
    setError: setAddCurrencyError,
    formState: { errors: addCurrencyErrors, isSubmitting: isAddCurrencySubmitting },
  } = useForm({ defaultValues: { currencyCode: '' } });

  const cardQuery = useQuery({
    queryKey: selectedCardId ? cardKeys.byId(selectedCardId) : [...cardKeys.all, 'id', 'idle'],
    queryFn: () => cardApi.getById(selectedCardId),
    enabled: hasSelectedCard,
    retry: false,
  });

  const accountQuery = useQuery({
    queryKey: selectedCardId
      ? cardKeys.account(selectedCardId)
      : [...cardKeys.all, 'account', 'idle'],
    queryFn: () => cardApi.getLinkedAccount(selectedCardId),
    enabled: hasSelectedCard,
    retry: false,
  });

  const balancesQuery = useQuery({
    queryKey: selectedCardId
      ? cardKeys.balances(selectedCardId)
      : [...cardKeys.all, 'balances', 'idle'],
    queryFn: () => cardApi.getBalances(selectedCardId),
    enabled: hasSelectedCard,
    retry: false,
  });

  const expirationQuery = useQuery({
    queryKey: selectedCardId
      ? cardKeys.expiration(selectedCardId)
      : [...cardKeys.all, 'expiration', 'idle'],
    queryFn: () => cardApi.checkExpiration(selectedCardId),
    enabled: hasSelectedCard,
    retry: false,
  });

  const refreshCard = (cardId) => {
    queryClient.invalidateQueries({ queryKey: cardKeys.byId(cardId) });
    queryClient.invalidateQueries({ queryKey: cardKeys.balances(cardId) });
    queryClient.invalidateQueries({ queryKey: cardKeys.expiration(cardId) });
  };

  const depositMutation = useMutation({
    mutationFn: ({ cardId, payload }) => cardApi.deposit(cardId, payload),
    retry: false,
    onSuccess: (_result, variables) => {
      refreshCard(variables.cardId);
      resetDeposit();
      showToast({ title: 'Deposit completed.', variant: 'success' });
    },
  });

  const withdrawMutation = useMutation({
    mutationFn: ({ cardId, payload }) => cardApi.withdraw(cardId, payload),
    retry: false,
    onSuccess: (_result, variables) => {
      refreshCard(variables.cardId);
      resetWithdraw();
      showToast({ title: 'Withdrawal completed.', variant: 'success' });
    },
  });

  const transferMutation = useMutation({
    mutationFn: (payload) => cardApi.transfer(payload),
    retry: false,
    onSuccess: () => {
      refreshCard(selectedCardId);
      resetTransfer();
      showToast({ title: 'Transfer completed.', variant: 'success' });
    },
  });

  const exchangeMutation = useMutation({
    mutationFn: ({ cardId, payload }) => cardApi.exchangeCurrency(cardId, payload),
    retry: false,
    onSuccess: (_result, variables) => {
      refreshCard(variables.cardId);
      resetExchange();
      showToast({ title: 'Currency exchanged.', variant: 'success' });
    },
  });

  const addCurrencyMutation = useMutation({
    mutationFn: ({ cardId, currencyCode }) => cardApi.addCurrency(cardId, currencyCode),
    retry: false,
    onSuccess: (_result, variables) => {
      refreshCard(variables.cardId);
      resetAddCurrency();
      showToast({ title: 'Currency added to card.', variant: 'success' });
    },
  });

  const statusMutation = useMutation({
    mutationFn: ({ cardId, action }) =>
      action === 'activate' ? cardApi.activate(cardId) : cardApi.deactivate(cardId),
    retry: false,
    onSuccess: (_result, variables) => {
      refreshCard(variables.cardId);
      showToast({
        title: variables.action === 'activate' ? 'Card activated.' : 'Card deactivated.',
        variant: 'success',
      });
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (cardId) => cardApi.delete(cardId),
    retry: false,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: cardKeys.all });
      setPendingDelete(false);
      setSelectedCardId(null);
      showToast({ title: 'Card deleted.', variant: 'success' });
    },
  });

  const balanceColumns = useMemo(
    () => [
      {
        key: 'currencyCode',
        header: 'Currency',
        render: (balance) => formatValue(balance.currencyCode),
      },
      { key: 'amount', header: 'Amount', render: (balance) => formatValue(balance.amount) },
    ],
    []
  );

  const submitLookup = ({ cardId }) => {
    setSelectedCardId(trimValue(cardId));
  };

  const submitDeposit = async (values) => {
    try {
      await depositMutation.mutateAsync({
        cardId: selectedCardId,
        payload: buildDepositPayload(values),
      });
    } catch (error) {
      applyBackendFormErrors(error, setDepositError, DEPOSIT_FIELDS);
    }
  };

  const submitWithdraw = async (values) => {
    try {
      await withdrawMutation.mutateAsync({
        cardId: selectedCardId,
        payload: buildWithdrawPayload(values),
      });
    } catch (error) {
      applyBackendFormErrors(error, setWithdrawError, WITHDRAW_FIELDS);
    }
  };

  const submitTransfer = async (values) => {
    try {
      await transferMutation.mutateAsync(buildTransferPayload(values, selectedCardId));
    } catch (error) {
      applyBackendFormErrors(error, setTransferError, TRANSFER_FIELDS);
    }
  };

  const submitExchange = async (values) => {
    try {
      await exchangeMutation.mutateAsync({
        cardId: selectedCardId,
        payload: buildExchangePayload(values),
      });
    } catch (error) {
      applyBackendFormErrors(error, setExchangeError, EXCHANGE_FIELDS);
    }
  };

  const submitAddCurrency = async (values) => {
    try {
      await addCurrencyMutation.mutateAsync({
        cardId: selectedCardId,
        currencyCode: normalizeCurrency(values.currencyCode),
      });
    } catch (error) {
      applyBackendFormErrors(error, setAddCurrencyError, ADD_CURRENCY_FIELDS);
    }
  };

  const submitStatus = async (action) => {
    try {
      await statusMutation.mutateAsync({ cardId: selectedCardId, action });
    } catch {
      // The shared Axios error bridge shows the backend message for this button action.
    }
  };

  const confirmDelete = () => {
    if (!selectedCardId) {
      return;
    }

    deleteMutation.mutate(selectedCardId);
  };

  const card = cardQuery.data;
  const account = accountQuery.data;
  const balances = balancesQuery.data ?? [];
  const cardActive = getActiveValue(card);
  const statusAction = statusMutation.isPending ? statusMutation.variables?.action : null;

  return (
    <div className={styles.page}>
      <header className={styles.pageHeader}>
        <p className={styles.kicker}>Card area</p>
        <h1 className={styles.title}>Cards</h1>
      </header>

      <Card title="Find card">
        <form className={styles.lookupForm} onSubmit={handleLookupSubmit(submitLookup)} noValidate>
          <TextField
            id="card-id-lookup"
            label="Card ID"
            type="number"
            min="1"
            error={lookupErrors.cardId?.message}
            required
            {...registerLookup('cardId', {
              required: 'Card ID is required.',
              validate: (value) => validatePositiveId(value, 'Card ID'),
            })}
          />
          <Button type="submit" isLoading={cardQuery.isFetching}>
            Load card
          </Button>
        </form>
      </Card>

      <div className={styles.grid}>
        <Card
          title="Card detail"
          actions={
            hasSelectedCard && (
              <div className={styles.actionsRow}>
                <Button
                  type="button"
                  variant="secondary"
                  disabled={statusMutation.isPending || cardActive === true}
                  isLoading={statusAction === 'activate'}
                  onClick={() => submitStatus('activate')}
                >
                  Activate
                </Button>
                <Button
                  type="button"
                  variant="danger"
                  disabled={statusMutation.isPending || cardActive === false}
                  isLoading={statusAction === 'deactivate'}
                  onClick={() => submitStatus('deactivate')}
                >
                  Deactivate
                </Button>
              </div>
            )
          }
        >
          {!hasSelectedCard && <p className={styles.mutedText}>No card selected.</p>}
          {cardQuery.isError && (
            <Toast variant="danger" message="Card detail could not be loaded." />
          )}
          {card && (
            <dl className={styles.profileGrid}>
              <DetailItem label="Card ID">{selectedCardId}</DetailItem>
              <DetailItem label="Card">{formatValue(card.panMasked)}</DetailItem>
              <DetailItem label="Type">{formatValue(card.type)}</DetailItem>
              <DetailItem label="Brand">{formatValue(card.brand)}</DetailItem>
              <DetailItem label="Limit">{formatValue(card.spendingLimit)}</DetailItem>
              <DetailItem label="Expires">{formatValue(card.expirationDate)}</DetailItem>
              <DetailItem label="Status">
                <StatusBadge active={cardActive} />
              </DetailItem>
              <DetailItem label="Expiration check">
                <ExpirationBadge expired={expirationQuery.data} />
              </DetailItem>
            </dl>
          )}
        </Card>

        <Card title="Linked account">
          {!hasSelectedCard && <p className={styles.mutedText}>No card selected.</p>}
          {accountQuery.isError && (
            <Toast variant="danger" message="Linked account could not be loaded." />
          )}
          {account && (
            <dl className={styles.profileGrid}>
              <DetailItem label="Name">{formatValue(account.name)}</DetailItem>
              <DetailItem label="Category">{formatValue(account.category)}</DetailItem>
              <DetailItem label="Opened">{formatValue(account.dateOpened)}</DetailItem>
              <DetailItem label="Status">
                <StatusBadge active={getActiveValue(account)} />
              </DetailItem>
            </dl>
          )}
        </Card>
      </div>

      <Card title="Currency balances">
        {balancesQuery.isError && (
          <Toast variant="danger" message="Balances could not be loaded." />
        )}
        <Table
          columns={balanceColumns}
          data={balances}
          getRowKey={(balance, index) => balance.currencyCode ?? index}
          emptyMessage={hasSelectedCard ? 'No balances found for this card.' : 'No card selected.'}
          loadingMessage="Loading balances..."
          isLoading={balancesQuery.isLoading}
        />
      </Card>

      <div className={styles.operationsGrid}>
        <Card title="Deposit">
          <form
            className={styles.formStack}
            onSubmit={handleDepositSubmit(submitDeposit)}
            noValidate
          >
            <div className={styles.twoColumnForm}>
              <TextField
                id="deposit-amount"
                label="Amount"
                type="number"
                min="0.01"
                step="0.01"
                error={depositErrors.amountToDeposit?.message}
                required
                {...registerDeposit('amountToDeposit', {
                  required: 'Amount is required.',
                  validate: validatePositiveAmount,
                })}
              />
              <Select
                id="deposit-currency"
                label="Currency"
                placeholder="Choose currency"
                options={CURRENCY_OPTIONS}
                error={depositErrors.currencyCode?.message}
                required
                {...registerDeposit('currencyCode', { required: 'Currency is required.' })}
              />
            </div>
            {depositErrors.root && <Toast variant="danger" message={depositErrors.root.message} />}
            <Button
              type="submit"
              disabled={!hasSelectedCard}
              isLoading={depositMutation.isPending || isDepositSubmitting}
            >
              Deposit
            </Button>
          </form>
        </Card>

        <Card title="Withdraw">
          <form
            className={styles.formStack}
            onSubmit={handleWithdrawSubmit(submitWithdraw)}
            noValidate
          >
            <div className={styles.twoColumnForm}>
              <TextField
                id="withdraw-amount"
                label="Amount"
                type="number"
                min="0.01"
                step="0.01"
                error={withdrawErrors.amountToWithdraw?.message}
                required
                {...registerWithdraw('amountToWithdraw', {
                  required: 'Amount is required.',
                  validate: validatePositiveAmount,
                })}
              />
              <Select
                id="withdraw-currency"
                label="Currency"
                placeholder="Choose currency"
                options={CURRENCY_OPTIONS}
                error={withdrawErrors.currencyCode?.message}
                required
                {...registerWithdraw('currencyCode', { required: 'Currency is required.' })}
              />
            </div>
            {withdrawErrors.root && (
              <Toast variant="danger" message={withdrawErrors.root.message} />
            )}
            <Button
              type="submit"
              disabled={!hasSelectedCard}
              isLoading={withdrawMutation.isPending || isWithdrawSubmitting}
            >
              Withdraw
            </Button>
          </form>
        </Card>

        <Card title="Transfer">
          <form
            className={styles.formStack}
            onSubmit={handleTransferSubmit(submitTransfer)}
            noValidate
          >
            <div className={styles.threeColumnForm}>
              <TextField
                id="transfer-receiver-card-id"
                label="Receiver card ID"
                type="number"
                min="1"
                error={transferErrors.receiverCardId?.message}
                required
                {...registerTransfer('receiverCardId', {
                  required: 'Receiver card ID is required.',
                  validate: (value) => validatePositiveId(value, 'Receiver card ID'),
                })}
              />
              <TextField
                id="transfer-amount"
                label="Amount"
                type="number"
                min="0.01"
                step="0.01"
                error={transferErrors.amount?.message}
                required
                {...registerTransfer('amount', {
                  required: 'Amount is required.',
                  validate: validatePositiveAmount,
                })}
              />
              <Select
                id="transfer-currency"
                label="Currency"
                placeholder="Choose currency"
                options={CURRENCY_OPTIONS}
                error={transferErrors.currencyCode?.message}
                required
                {...registerTransfer('currencyCode', { required: 'Currency is required.' })}
              />
            </div>
            {transferErrors.root && (
              <Toast variant="danger" message={transferErrors.root.message} />
            )}
            <Button
              type="submit"
              disabled={!hasSelectedCard}
              isLoading={transferMutation.isPending || isTransferSubmitting}
            >
              Transfer
            </Button>
          </form>
        </Card>

        <Card title="Exchange currency">
          <form
            className={styles.formStack}
            onSubmit={handleExchangeSubmit(submitExchange)}
            noValidate
          >
            <div className={styles.threeColumnForm}>
              <TextField
                id="exchange-amount"
                label="Amount"
                type="number"
                min="0.01"
                step="0.01"
                error={exchangeErrors.amount?.message}
                required
                {...registerExchange('amount', {
                  required: 'Amount is required.',
                  validate: validatePositiveAmount,
                })}
              />
              <Select
                id="exchange-from-currency"
                label="From currency"
                placeholder="Choose currency"
                options={CURRENCY_OPTIONS}
                error={exchangeErrors.fromCurrencyCode?.message}
                required
                {...registerExchange('fromCurrencyCode', {
                  required: 'From currency is required.',
                })}
              />
              <Select
                id="exchange-to-currency"
                label="To currency"
                placeholder="Choose currency"
                options={CURRENCY_OPTIONS}
                error={exchangeErrors.toCurrencyCode?.message}
                required
                {...registerExchange('toCurrencyCode', { required: 'To currency is required.' })}
              />
            </div>
            {exchangeErrors.root && (
              <Toast variant="danger" message={exchangeErrors.root.message} />
            )}
            <Button
              type="submit"
              disabled={!hasSelectedCard}
              isLoading={exchangeMutation.isPending || isExchangeSubmitting}
            >
              Exchange
            </Button>
          </form>
        </Card>

        <Card title="Add currency">
          <form
            className={styles.formStack}
            onSubmit={handleAddCurrencySubmit(submitAddCurrency)}
            noValidate
          >
            <Select
              id="add-card-currency"
              label="Currency"
              placeholder="Choose currency"
              options={CURRENCY_OPTIONS}
              error={addCurrencyErrors.currencyCode?.message}
              required
              {...registerAddCurrency('currencyCode', { required: 'Currency is required.' })}
            />
            {addCurrencyErrors.root && (
              <Toast variant="danger" message={addCurrencyErrors.root.message} />
            )}
            <Button
              type="submit"
              disabled={!hasSelectedCard}
              isLoading={addCurrencyMutation.isPending || isAddCurrencySubmitting}
            >
              Add currency
            </Button>
          </form>
        </Card>

        {isManager && (
          <Card title="Delete card" subtitle="Manager-only action. Confirmation is required.">
            <div className={styles.formStack}>
              {!hasSelectedCard && (
                <p className={styles.mutedText}>Load a card before deleting it.</p>
              )}
              <Button
                type="button"
                variant="danger"
                disabled={!hasSelectedCard || deleteMutation.isPending}
                onClick={() => setPendingDelete(true)}
              >
                Delete card
              </Button>
            </div>
          </Card>
        )}
      </div>

      <Modal
        open={pendingDelete}
        title="Delete card"
        onClose={() => setPendingDelete(false)}
        footer={
          <>
            <Button
              type="button"
              variant="secondary"
              disabled={deleteMutation.isPending}
              onClick={() => setPendingDelete(false)}
            >
              Cancel
            </Button>
            <Button
              type="button"
              variant="danger"
              isLoading={deleteMutation.isPending}
              onClick={confirmDelete}
            >
              Delete card
            </Button>
          </>
        }
      >
        <p className={styles.modalText}>
          Delete card {selectedCardId}? This action cannot be undone.
        </p>
      </Modal>
    </div>
  );
}
