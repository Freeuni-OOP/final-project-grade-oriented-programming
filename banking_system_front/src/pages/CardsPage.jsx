import { useMemo, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { useTranslation } from 'react-i18next';
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

function formatValue(value, fallbackText) {
  return value === null || value === undefined || value === '' ? fallbackText : String(value);
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
  const { t } = useTranslation('cards');
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

function ExpirationBadge({ expired }) {
  const { t } = useTranslation('cards');
  const className =
    expired === true ? styles.expired : expired === false ? styles.valid : styles.unknown;
  const label =
    expired === true
      ? t('status_expired')
      : expired === false
        ? t('status_valid')
        : t('status_unknown');

  return <span className={`${styles.badge} ${className}`}>{label}</span>;
}

export default function CardsPage() {
  const { t } = useTranslation('cards');
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

  // Validation Helpers utilizing translations
  const validatePositiveAmount = (value) => Number(value) > 0 || t('error_positive_amount');
  const validatePositiveId = (value, label) => {
    const valueText = trimValue(value);
    return (/^\d+$/.test(valueText) && Number(valueText) > 0) || t('error_positive_id', { label });
  };

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
      showToast({ title: t('toast_deposit_success'), variant: 'success' });
    },
  });

  const withdrawMutation = useMutation({
    mutationFn: ({ cardId, payload }) => cardApi.withdraw(cardId, payload),
    retry: false,
    onSuccess: (_result, variables) => {
      refreshCard(variables.cardId);
      resetWithdraw();
      showToast({ title: t('toast_withdraw_success'), variant: 'success' });
    },
  });

  const transferMutation = useMutation({
    mutationFn: (payload) => cardApi.transfer(payload),
    retry: false,
    onSuccess: () => {
      refreshCard(selectedCardId);
      resetTransfer();
      showToast({ title: t('toast_transfer_success'), variant: 'success' });
    },
  });

  const exchangeMutation = useMutation({
    mutationFn: ({ cardId, payload }) => cardApi.exchangeCurrency(cardId, payload),
    retry: false,
    onSuccess: (_result, variables) => {
      refreshCard(variables.cardId);
      resetExchange();
      showToast({ title: t('toast_exchange_success'), variant: 'success' });
    },
  });

  const addCurrencyMutation = useMutation({
    mutationFn: ({ cardId, currencyCode }) => cardApi.addCurrency(cardId, currencyCode),
    retry: false,
    onSuccess: (_result, variables) => {
      refreshCard(variables.cardId);
      resetAddCurrency();
      showToast({ title: t('toast_add_currency_success'), variant: 'success' });
    },
  });

  const statusMutation = useMutation({
    mutationFn: ({ cardId, action }) =>
      action === 'activate' ? cardApi.activate(cardId) : cardApi.deactivate(cardId),
    retry: false,
    onSuccess: (_result, variables) => {
      refreshCard(variables.cardId);
      showToast({
        title: variables.action === 'activate' ? t('toast_activated') : t('toast_deactivated'),
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
      showToast({ title: t('toast_deleted'), variant: 'success' });
    },
  });

  const balanceColumns = useMemo(
    () => [
      {
        key: 'currencyCode',
        header: t('col_currency'),
        render: (balance) => formatValue(balance.currencyCode, t('not_provided')),
      },
      {
        key: 'amount',
        header: t('col_amount'),
        render: (balance) => formatValue(balance.amount, t('not_provided')),
      },
    ],
    [t]
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
    } catch {}
  };

  const confirmDelete = () => {
    if (!selectedCardId) return;
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
        <p className={styles.kicker}>{t('page_kicker')}</p>
        <h1 className={styles.title}>{t('page_title')}</h1>
      </header>

      <Card title={t('find_card')}>
        <form className={styles.lookupForm} onSubmit={handleLookupSubmit(submitLookup)} noValidate>
          <TextField
            id="card-id-lookup"
            label={t('card_id')}
            type="number"
            min="1"
            error={lookupErrors.cardId?.message}
            required
            {...registerLookup('cardId', {
              required: t('error_card_id_required'),
              validate: (value) => validatePositiveId(value, t('card_id')),
            })}
          />
          <Button type="submit" isLoading={cardQuery.isFetching}>
            {t('load_card')}
          </Button>
        </form>
      </Card>

      <div className={styles.grid}>
        <Card
          title={t('card_detail')}
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
                  {t('action_activate')}
                </Button>
                <Button
                  type="button"
                  variant="danger"
                  disabled={statusMutation.isPending || cardActive === false}
                  isLoading={statusAction === 'deactivate'}
                  onClick={() => submitStatus('deactivate')}
                >
                  {t('action_deactivate')}
                </Button>
              </div>
            )
          }
        >
          {!hasSelectedCard && <p className={styles.mutedText}>{t('no_card_selected')}</p>}
          {cardQuery.isError && <Toast variant="danger" message={t('error_card_load')} />}
          {card && (
            <dl className={styles.profileGrid}>
              <DetailItem label={t('card_id')}>{selectedCardId}</DetailItem>
              <DetailItem label={t('lbl_card')}>
                {formatValue(card.panMasked, t('not_provided'))}
              </DetailItem>
              <DetailItem label={t('lbl_type')}>
                {formatValue(card.type, t('not_provided'))}
              </DetailItem>
              <DetailItem label={t('lbl_brand')}>
                {formatValue(card.brand, t('not_provided'))}
              </DetailItem>
              <DetailItem label={t('lbl_limit')}>
                {formatValue(card.spendingLimit, t('not_provided'))}
              </DetailItem>
              <DetailItem label={t('lbl_expires')}>
                {formatValue(card.expirationDate, t('not_provided'))}
              </DetailItem>
              <DetailItem label={t('lbl_status')}>
                <StatusBadge active={cardActive} />
              </DetailItem>
              <DetailItem label={t('lbl_expiration_check')}>
                <ExpirationBadge expired={expirationQuery.data} />
              </DetailItem>
            </dl>
          )}
        </Card>

        <Card title={t('linked_account')}>
          {!hasSelectedCard && <p className={styles.mutedText}>{t('no_card_selected')}</p>}
          {accountQuery.isError && <Toast variant="danger" message={t('error_account_load')} />}
          {account && (
            <dl className={styles.profileGrid}>
              <DetailItem label={t('lbl_name')}>
                {formatValue(account.name, t('not_provided'))}
              </DetailItem>
              <DetailItem label={t('lbl_category')}>
                {formatValue(account.category, t('not_provided'))}
              </DetailItem>
              <DetailItem label={t('lbl_opened')}>
                {formatValue(account.dateOpened, t('not_provided'))}
              </DetailItem>
              <DetailItem label={t('lbl_status')}>
                <StatusBadge active={getActiveValue(account)} />
              </DetailItem>
            </dl>
          )}
        </Card>
      </div>

      <Card title={t('currency_balances')}>
        {balancesQuery.isError && <Toast variant="danger" message={t('error_balances_load')} />}
        <Table
          columns={balanceColumns}
          data={balances}
          getRowKey={(balance, index) => balance.currencyCode ?? index}
          emptyMessage={hasSelectedCard ? t('no_balances') : t('no_card_selected')}
          loadingMessage={t('loading_balances')}
          isLoading={balancesQuery.isLoading}
        />
      </Card>

      <div className={styles.operationsGrid}>
        <Card title={t('op_deposit')}>
          <form
            className={styles.formStack}
            onSubmit={handleDepositSubmit(submitDeposit)}
            noValidate
          >
            <div className={styles.twoColumnForm}>
              <TextField
                id="deposit-amount"
                label={t('lbl_amount')}
                type="number"
                min="0.01"
                step="0.01"
                error={depositErrors.amountToDeposit?.message}
                required
                {...registerDeposit('amountToDeposit', {
                  required: t('error_amount_required'),
                  validate: validatePositiveAmount,
                })}
              />
              <Select
                id="deposit-currency"
                label={t('lbl_currency')}
                placeholder={t('choose_currency')}
                options={CURRENCY_OPTIONS}
                error={depositErrors.currencyCode?.message}
                required
                {...registerDeposit('currencyCode', { required: t('error_currency_required') })}
              />
            </div>
            {depositErrors.root && <Toast variant="danger" message={depositErrors.root.message} />}
            <Button
              type="submit"
              disabled={!hasSelectedCard}
              isLoading={depositMutation.isPending || isDepositSubmitting}
            >
              {t('op_deposit')}
            </Button>
          </form>
        </Card>

        <Card title={t('op_withdraw')}>
          <form
            className={styles.formStack}
            onSubmit={handleWithdrawSubmit(submitWithdraw)}
            noValidate
          >
            <div className={styles.twoColumnForm}>
              <TextField
                id="withdraw-amount"
                label={t('lbl_amount')}
                type="number"
                min="0.01"
                step="0.01"
                error={withdrawErrors.amountToWithdraw?.message}
                required
                {...registerWithdraw('amountToWithdraw', {
                  required: t('error_amount_required'),
                  validate: validatePositiveAmount,
                })}
              />
              <Select
                id="withdraw-currency"
                label={t('lbl_currency')}
                placeholder={t('choose_currency')}
                options={CURRENCY_OPTIONS}
                error={withdrawErrors.currencyCode?.message}
                required
                {...registerWithdraw('currencyCode', { required: t('error_currency_required') })}
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
              {t('op_withdraw')}
            </Button>
          </form>
        </Card>

        <Card title={t('op_transfer')}>
          <form
            className={styles.formStack}
            onSubmit={handleTransferSubmit(submitTransfer)}
            noValidate
          >
            <div className={styles.threeColumnForm}>
              <TextField
                id="transfer-receiver-card-id"
                label={t('lbl_receiver_id')}
                type="number"
                min="1"
                error={transferErrors.receiverCardId?.message}
                required
                {...registerTransfer('receiverCardId', {
                  required: t('error_receiver_required'),
                  validate: (value) => validatePositiveId(value, t('lbl_receiver_id')),
                })}
              />
              <TextField
                id="transfer-amount"
                label={t('lbl_amount')}
                type="number"
                min="0.01"
                step="0.01"
                error={transferErrors.amount?.message}
                required
                {...registerTransfer('amount', {
                  required: t('error_amount_required'),
                  validate: validatePositiveAmount,
                })}
              />
              <Select
                id="transfer-currency"
                label={t('lbl_currency')}
                placeholder={t('choose_currency')}
                options={CURRENCY_OPTIONS}
                error={transferErrors.currencyCode?.message}
                required
                {...registerTransfer('currencyCode', { required: t('error_currency_required') })}
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
              {t('op_transfer')}
            </Button>
          </form>
        </Card>

        <Card title={t('op_exchange')}>
          <form
            className={styles.formStack}
            onSubmit={handleExchangeSubmit(submitExchange)}
            noValidate
          >
            <div className={styles.threeColumnForm}>
              <TextField
                id="exchange-amount"
                label={t('lbl_amount')}
                type="number"
                min="0.01"
                step="0.01"
                error={exchangeErrors.amount?.message}
                required
                {...registerExchange('amount', {
                  required: t('error_amount_required'),
                  validate: validatePositiveAmount,
                })}
              />
              <Select
                id="exchange-from-currency"
                label={t('lbl_from_currency')}
                placeholder={t('choose_currency')}
                options={CURRENCY_OPTIONS}
                error={exchangeErrors.fromCurrencyCode?.message}
                required
                {...registerExchange('fromCurrencyCode', {
                  required: t('error_from_currency_required'),
                })}
              />
              <Select
                id="exchange-to-currency"
                label={t('lbl_to_currency')}
                placeholder={t('choose_currency')}
                options={CURRENCY_OPTIONS}
                error={exchangeErrors.toCurrencyCode?.message}
                required
                {...registerExchange('toCurrencyCode', {
                  required: t('error_to_currency_required'),
                })}
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
              {t('btn_exchange')}
            </Button>
          </form>
        </Card>

        <Card title={t('op_add_currency')}>
          <form
            className={styles.formStack}
            onSubmit={handleAddCurrencySubmit(submitAddCurrency)}
            noValidate
          >
            <Select
              id="add-card-currency"
              label={t('lbl_currency')}
              placeholder={t('choose_currency')}
              options={CURRENCY_OPTIONS}
              error={addCurrencyErrors.currencyCode?.message}
              required
              {...registerAddCurrency('currencyCode', { required: t('error_currency_required') })}
            />
            {addCurrencyErrors.root && (
              <Toast variant="danger" message={addCurrencyErrors.root.message} />
            )}
            <Button
              type="submit"
              disabled={!hasSelectedCard}
              isLoading={addCurrencyMutation.isPending || isAddCurrencySubmitting}
            >
              {t('op_add_currency')}
            </Button>
          </form>
        </Card>

        {isManager && (
          <Card title={t('op_delete_card')} subtitle={t('delete_card_subtitle')}>
            <div className={styles.formStack}>
              {!hasSelectedCard && <p className={styles.mutedText}>{t('delete_card_prompt')}</p>}
              <Button
                type="button"
                variant="danger"
                disabled={!hasSelectedCard || deleteMutation.isPending}
                onClick={() => setPendingDelete(true)}
              >
                {t('op_delete_card')}
              </Button>
            </div>
          </Card>
        )}
      </div>

      <Modal
        open={pendingDelete}
        title={t('op_delete_card')}
        onClose={() => setPendingDelete(false)}
        footer={
          <>
            <Button
              type="button"
              variant="secondary"
              disabled={deleteMutation.isPending}
              onClick={() => setPendingDelete(false)}
            >
              {t('btn_cancel')}
            </Button>
            <Button
              type="button"
              variant="danger"
              isLoading={deleteMutation.isPending}
              onClick={confirmDelete}
            >
              {t('op_delete_card')}
            </Button>
          </>
        }
      >
        <p className={styles.modalText}>{t('delete_confirm_text', { id: selectedCardId })}</p>
      </Modal>
    </div>
  );
}
