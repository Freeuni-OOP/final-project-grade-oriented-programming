import { useEffect, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { useTranslation } from 'react-i18next';
import { useAuth } from '../components/AuthContext';
import { customerApi } from '../api/customerApi';
import { accountApi } from '../api/accountApi';
import { cardApi } from '../api/cardApi';
import { customerKeys } from '../api/customerQueryKeys';
import { accountKeys } from '../api/accountQueryKeys';
import { cardKeys } from '../api/cardQueryKeys';
import { applyBackendFormErrors } from '../api/formErrors';
import { Button, Card, Select, Spinner, Table, TextField, Toast, useToast } from '../components/ui';
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

const IDLE = 'idle';

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

const CURRENCY_OPTIONS = [
  { value: 'GEL', label: 'GEL — Georgian Lari' },
  { value: 'USD', label: 'USD — US Dollar' },
  { value: 'EUR', label: 'EUR — Euro' },
  { value: 'GBP', label: 'GBP — British Pound' },
];

// Account.transactions has no @OrderBy on the backend, so the order isn't
// guaranteed -- sort newest first on the client.
function sortTransactionsDesc(transactions) {
  return [...(transactions ?? [])].sort((a, b) => {
    const timeA = new Date(a?.timeStamp ?? 0).getTime();
    const timeB = new Date(b?.timeStamp ?? 0).getTime();
    return timeB - timeA;
  });
}

function buildCreateAccountPayload(values) {
  return {
    accountName: trimValue(values.accountName),
    category: values.category,
  };
}

// Backend sends createdAccountId, but this keeps it safe if the shape ever changes.
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

// Small chrome shared by the three editable profile fields: a read-only
// view with an "Edit" trigger, or an inline form with Save/Cancel.
function EditableDetail({
  label,
  isEditing,
  onEdit,
  onCancel,
  isSaving,
  editDisabled,
  onSubmit,
  viewValue,
  children,
}) {
  const { t } = useTranslation(['common']);
  return (
    <DetailItem label={label}>
      {isEditing ? (
        <form className={styles.editForm} onSubmit={onSubmit} noValidate>
          {children}
          <div className={styles.actionsRow}>
            <Button type="submit" size="sm" isLoading={isSaving}>
              {t('common:save')}
            </Button>
            <Button
              type="button"
              variant="secondary"
              size="sm"
              disabled={isSaving}
              onClick={onCancel}
            >
              {t('common:cancel')}
            </Button>
          </div>
        </form>
      ) : (
        <div className={styles.viewRow}>
          <span>{viewValue}</span>
          <Button type="button" variant="ghost" size="sm" disabled={editDisabled} onClick={onEdit}>
            {t('common:edit')}
          </Button>
        </div>
      )}
    </DetailItem>
  );
}

export default function CustomersPage() {
  const { t } = useTranslation(['common', 'customer', 'cards', 'accounts']);
  const { email } = useAuth();
  const queryClient = useQueryClient();
  const { showToast } = useToast();

  const [selectedAccountId, setSelectedAccountId] = useState(null);
  const [selectedCardId, setSelectedCardId] = useState(null);
  const [editingSection, setEditingSection] = useState(null);
  const [showTransactions, setShowTransactions] = useState(false);
  const [showCreateAccount, setShowCreateAccount] = useState(false);
  const [showCreateCard, setShowCreateCard] = useState(false);
  const [showAddCurrency, setShowAddCurrency] = useState(false);
  const [activeMoneyAction, setActiveMoneyAction] = useState(null);
  const [transferRecipient, setTransferRecipient] = useState(null);
  const [resolvedReceiverCardId, setResolvedReceiverCardId] = useState(null);
  const [searchedEmail, setSearchedEmail] = useState(null);
  const [recipientCards, setRecipientCards] = useState([]);

  const profileQueryKey = email ? customerKeys.byEmail(email) : [...customerKeys.all, IDLE];

  // My profile (email comes from the JWT via AuthContext). The response already
  // embeds account summaries, so this one call also feeds the accounts strip.
  const profileQuery = useQuery({
    queryKey: profileQueryKey,
    queryFn: () => customerApi.getByEmail(email),
    enabled: Boolean(email),
    retry: false,
  });

  // Selected account detail. Response embeds card summaries -> feeds the cards strip.
  const accountQuery = useQuery({
    queryKey: selectedAccountId ? accountKeys.byId(selectedAccountId) : [...accountKeys.all, IDLE],
    queryFn: () => accountApi.getById(selectedAccountId),
    enabled: Boolean(selectedAccountId),
    retry: false,
  });

  // Selected card detail. Response embeds card balances.
  const cardQuery = useQuery({
    queryKey: selectedCardId ? cardKeys.byId(selectedCardId) : [...cardKeys.all, IDLE],
    queryFn: () => cardApi.getById(selectedCardId),
    enabled: Boolean(selectedCardId),
    retry: false,
  });

  const profile = profileQuery.data;
  const account = accountQuery.data;
  const card = cardQuery.data;
  const customerId = getId(profile);
  const cardCurrencyCodes = new Set(
    (card?.cardBalances ?? []).map((balance) => balance.currencyCode)
  );

  const availableCurrencyOptions = CURRENCY_OPTIONS.filter(
    (option) => !cardCurrencyCodes.has(option.value)
  ).map((o) => ({ value: o.value, label: t(`customer:currency_${o.value.toLowerCase()}`) }));

  const cardOwnedCurrencyOptions = CURRENCY_OPTIONS.filter((option) =>
    cardCurrencyCodes.has(option.value)
  ).map((o) => ({ value: o.value, label: t(`customer:currency_${o.value.toLowerCase()}`) }));

  // table columns need t so define them here
  const balanceColumns = [
    {
      key: 'currencyCode',
      header: t('common:currency'),
      render: (balance) => formatValue(balance.currencyCode),
    },
    {
      key: 'amount',
      header: t('common:amount'),
      align: 'right',
      render: (balance) => formatValue(balance.amount),
    },
  ];

  const transactionColumns = [
    {
      key: 'timeStamp',
      header: t('customer:col_date'),
      render: (transaction) => formatValue(transaction.timeStamp),
    },
    {
      key: 'transactionType',
      header: t('customer:col_type'),
      render: (transaction) => formatValue(transaction.transactionType),
    },
    {
      key: 'amount',
      header: t('customer:col_amount'),
      align: 'right',
      render: (transaction) => formatValue(transaction.amount),
    },
    {
      key: 'currencyCode',
      header: t('customer:col_currency'),
      render: (transaction) => formatValue(transaction.currencyCode),
    },
    {
      key: 'status',
      header: t('customer:label_status'),
      render: (transaction) => formatValue(transaction.status),
    },
    {
      key: 'description',
      header: t('customer:col_description'),
      render: (transaction) => formatValue(transaction.description),
    },
  ];

  // ---- profile edit forms (one per section, so each can be edited independently) ----
  const {
    register: registerName,
    handleSubmit: handleNameSubmit,
    reset: resetNameForm,
    setError: setNameError,
    formState: { errors: nameErrors, isSubmitting: isNameSubmitting },
  } = useForm({ defaultValues: { firstName: '', lastName: '' } });

  const {
    register: registerPhone,
    handleSubmit: handlePhoneSubmit,
    reset: resetPhoneForm,
    setError: setPhoneError,
    formState: { errors: phoneErrors, isSubmitting: isPhoneSubmitting },
  } = useForm({ defaultValues: { phoneNumber: '' } });

  const {
    register: registerAddress,
    handleSubmit: handleAddressSubmit,
    reset: resetAddressForm,
    setError: setAddressError,
    formState: { errors: addressErrors, isSubmitting: isAddressSubmitting },
  } = useForm({ defaultValues: { address: '' } });

  // Keep the (currently closed) edit forms in sync with the latest server data,
  // so opening a section always starts from the true current value.
  useEffect(() => {
    if (!profile) return;
    resetNameForm({ firstName: profile.firstName ?? '', lastName: profile.lastName ?? '' });
    resetPhoneForm({ phoneNumber: profile.phoneNumber ?? '' });
    resetAddressForm({ address: profile.address ?? '' });
  }, [profile, resetNameForm, resetPhoneForm, resetAddressForm]);

  // One mutation for all three sections. The backend only overwrites fields
  // that are non-null, so each submit handler sends null for the untouched ones.
  const updateMutation = useMutation({
    mutationFn: ({ id, payload }) => customerApi.update(id, payload),
    retry: false,
    onSuccess: (updatedProfile) => {
      queryClient.setQueryData(profileQueryKey, updatedProfile);
      showToast({ title: t('customer:profile_updated'), variant: 'success' });
      setEditingSection(null);
    },
  });

  const resetSectionForm = (section) => {
    if (!profile) return;
    if (section === 'name') {
      resetNameForm({ firstName: profile.firstName ?? '', lastName: profile.lastName ?? '' });
    } else if (section === 'phone') {
      resetPhoneForm({ phoneNumber: profile.phoneNumber ?? '' });
    } else if (section === 'address') {
      resetAddressForm({ address: profile.address ?? '' });
    }
  };

  const openEdit = (section) => {
    resetSectionForm(section);
    setEditingSection(section);
  };

  const cancelEdit = () => {
    resetSectionForm(editingSection);
    setEditingSection(null);
  };

  const submitName = async (values) => {
    try {
      await updateMutation.mutateAsync({
        id: customerId,
        payload: {
          firstName: trimValue(values.firstName),
          lastName: trimValue(values.lastName),
          phoneNumber: null,
          address: null,
        },
      });
    } catch (error) {
      applyBackendFormErrors(error, setNameError, ['firstName', 'lastName']);
    }
  };

  const submitPhone = async (values) => {
    try {
      await updateMutation.mutateAsync({
        id: customerId,
        payload: {
          firstName: null,
          lastName: null,
          phoneNumber: trimValue(values.phoneNumber),
          address: null,
        },
      });
    } catch (error) {
      applyBackendFormErrors(error, setPhoneError, ['phoneNumber']);
    }
  };

  const submitAddress = async (values) => {
    try {
      await updateMutation.mutateAsync({
        id: customerId,
        payload: {
          firstName: null,
          lastName: null,
          phoneNumber: null,
          address: trimValue(values.address),
        },
      });
    } catch (error) {
      applyBackendFormErrors(error, setAddressError, ['address']);
    }
  };

  const isNameSaving = updateMutation.isPending || isNameSubmitting;
  const isPhoneSaving = updateMutation.isPending || isPhoneSubmitting;
  const isAddressSaving = updateMutation.isPending || isAddressSubmitting;
  // Disable every Edit trigger while any save is in flight, so a section can't
  // be swapped out from under an in-progress submit.
  const editDisabled = !customerId || updateMutation.isPending;

  // ---- create account / create card forms ----
  const {
    register: registerCreateAccount,
    handleSubmit: handleCreateAccountSubmit,
    reset: resetCreateAccountForm,
    setError: setCreateAccountError,
    formState: { errors: createAccountErrors, isSubmitting: isCreateAccountSubmitting },
  } = useForm({ defaultValues: { accountName: '', category: '' } });

  const {
    register: registerCreateCard,
    handleSubmit: handleCreateCardSubmit,
    reset: resetCreateCardForm,
    setError: setCreateCardError,
    formState: { errors: createCardErrors, isSubmitting: isCreateCardSubmitting },
  } = useForm({ defaultValues: { cardType: '', cardBrand: '', spendingLimit: '', pan: '' } });

  // Creating an account is two backend calls: create the (unlinked) account,
  // then link it to the current customer. That link is what "attaches" it in
  // the database -- AccountCreationRequest itself has no customer field.
  const createAccountMutation = useMutation({
    mutationFn: async ({ payload, ownerId }) => {
      const createdAccount = await accountApi.create(payload);
      const createdAccountId = getCreatedAccountId(createdAccount);

      if (!createdAccountId) {
        const error = new Error('Account id was not returned.');
        error.missingCreatedAccountId = true;
        throw error;
      }

      await accountApi.registerCustomer(createdAccountId, ownerId);
      return createdAccountId;
    },
    retry: false,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: profileQueryKey });
      showToast({ title: t('customer:account_created'), variant: 'success' });
      resetCreateAccountForm();
      setShowCreateAccount(false);
    },
  });

  // Cards attach directly to an account via the URL -- one call is enough.
  const createCardMutation = useMutation({
    mutationFn: ({ accountId, payload }) => accountApi.createCard(accountId, payload),
    retry: false,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: accountKeys.byId(selectedAccountId) });
      showToast({ title: t('customer:card_created'), variant: 'success' });
      resetCreateCardForm();
      setShowCreateCard(false);
    },
  });

  const submitCreateAccount = async (values) => {
    try {
      await createAccountMutation.mutateAsync({
        payload: buildCreateAccountPayload(values),
        ownerId: customerId,
      });
    } catch (error) {
      if (error.missingCreatedAccountId) {
        setCreateAccountError('root', {
          type: 'server',
          message: t('customer:account_id_not_returned'),
        });
        return;
      }
      applyBackendFormErrors(error, setCreateAccountError, ['accountName', 'category']);
    }
  };

  const submitCreateCard = async (values) => {
    try {
      await createCardMutation.mutateAsync({
        accountId: selectedAccountId,
        payload: buildCreateCardPayload(values),
      });
    } catch (error) {
      applyBackendFormErrors(error, setCreateCardError, [
        'cardType',
        'cardBrand',
        'spendingLimit',
        'pan',
      ]);
    }
  };

  const {
    register: registerAddCurrency,
    handleSubmit: handleAddCurrencySubmit,
    reset: resetAddCurrencyForm,
    setError: setAddCurrencyError,
    formState: { errors: addCurrencyErrors, isSubmitting: isAddCurrencySubmitting },
  } = useForm({ defaultValues: { currencyCode: '' } });

  // Endpoint returns the full updated card, so we can write it straight into
  // the cache instead of refetching.
  const addCurrencyMutation = useMutation({
    mutationFn: ({ cardId, currencyCode }) => cardApi.addCurrency(cardId, currencyCode),
    retry: false,
    onSuccess: (updatedCard) => {
      queryClient.setQueryData(cardKeys.byId(selectedCardId), updatedCard);
      showToast({ title: t('customer:currency_added'), variant: 'success' });
      resetAddCurrencyForm();
      setShowAddCurrency(false);
    },
  });

  const submitAddCurrency = async (values) => {
    try {
      await addCurrencyMutation.mutateAsync({
        cardId: selectedCardId,
        currencyCode: values.currencyCode,
      });
    } catch (error) {
      applyBackendFormErrors(error, setAddCurrencyError, ['currencyCode']);
    }
  };

  const {
    register: registerDeposit,
    handleSubmit: handleDepositSubmit,
    reset: resetDepositForm,
    setError: setDepositError,
    formState: { errors: depositErrors, isSubmitting: isDepositSubmitting },
  } = useForm({ defaultValues: { amountToDeposit: '', currencyCode: '' } });

  const {
    register: registerWithdraw,
    handleSubmit: handleWithdrawSubmit,
    reset: resetWithdrawForm,
    setError: setWithdrawError,
    formState: { errors: withdrawErrors, isSubmitting: isWithdrawSubmitting },
  } = useForm({ defaultValues: { amountToWithdraw: '', currencyCode: '' } });

  const {
    register: registerExchange,
    handleSubmit: handleExchangeSubmit,
    reset: resetExchangeForm,
    setError: setExchangeError,
    formState: { errors: exchangeErrors, isSubmitting: isExchangeSubmitting },
  } = useForm({ defaultValues: { amount: '', fromCurrencyCode: '', toCurrencyCode: '' } });

  // Deposit/withdraw only return a plain confirmation string, not the updated
  // balance, so refetch the card rather than trying to patch the cache by hand.
  const depositMutation = useMutation({
    mutationFn: ({ cardId, payload }) => cardApi.deposit(cardId, payload),
    retry: false,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: cardKeys.byId(selectedCardId) });
      showToast({ title: t('customer:deposit_success'), variant: 'success' });
      resetDepositForm();
      setActiveMoneyAction(null);
    },
  });

  const withdrawMutation = useMutation({
    mutationFn: ({ cardId, payload }) => cardApi.withdraw(cardId, payload),
    retry: false,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: cardKeys.byId(selectedCardId) });
      showToast({ title: t('customer:withdrawal_success'), variant: 'success' });
      resetWithdrawForm();
      setActiveMoneyAction(null);
    },
  });

  const exchangeMutation = useMutation({
    mutationFn: ({ cardId, payload }) => cardApi.exchangeCurrency(cardId, payload),
    retry: false,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: cardKeys.byId(selectedCardId) });
      showToast({ title: t('customer:exchange_successful'), variant: 'success' });
      resetExchangeForm();
      setActiveMoneyAction(null);
    },
  });

  const submitDeposit = async (values) => {
    try {
      await depositMutation.mutateAsync({
        cardId: selectedCardId,
        payload: {
          amountToDeposit: trimValue(values.amountToDeposit),
          currencyCode: values.currencyCode,
        },
      });
    } catch (error) {
      applyBackendFormErrors(error, setDepositError, ['amountToDeposit', 'currencyCode']);
    }
  };

  const submitWithdraw = async (values) => {
    try {
      await withdrawMutation.mutateAsync({
        cardId: selectedCardId,
        payload: {
          amountToWithdraw: trimValue(values.amountToWithdraw),
          currencyCode: values.currencyCode,
        },
      });
    } catch (error) {
      applyBackendFormErrors(error, setWithdrawError, ['amountToWithdraw', 'currencyCode']);
    }
  };

  const submitExchange = async (values) => {
    try {
      await exchangeMutation.mutateAsync({
        cardId: selectedCardId,
        payload: {
          amount: trimValue(values.amount),
          fromCurrencyCode: values.fromCurrencyCode,
          toCurrencyCode: values.toCurrencyCode,
        },
      });
    } catch (error) {
      applyBackendFormErrors(error, setExchangeError, [
        'amount',
        'fromCurrencyCode',
        'toCurrencyCode',
      ]);
    }
  };

  const {
    register: registerTransfer,
    handleSubmit: handleTransferSubmit,
    reset: resetTransferForm,
    getValues: getTransferValues,
    watch: watchTransfer,
    setError: setTransferError,
    clearErrors: clearTransferErrors,
    formState: { errors: transferErrors, isSubmitting: isTransferSubmitting },
  } = useForm({ defaultValues: { receiverEmail: '', amount: '', currencyCode: '' } });

  const watchedReceiverEmail = watchTransfer('receiverEmail');

  // If the email is edited after a match was found, drop the stale match so a
  // transfer can never be sent to someone other than who's currently confirmed.
  useEffect(() => {
    if (transferRecipient && watchedReceiverEmail !== searchedEmail) {
      setTransferRecipient(null);
      setRecipientCards([]);
      setResolvedReceiverCardId(null);
    }
  }, [watchedReceiverEmail, searchedEmail, transferRecipient]);

  // Search step: find the customer by email, then collect every active card
  // across all of their accounts (fetched in parallel) so the sender can pick
  // which one to send to, instead of one being picked automatically.
  const searchRecipientMutation = useMutation({
    mutationFn: async (searchEmail) => {
      const foundCustomer = await customerApi.getByEmail(searchEmail);
      const accounts = foundCustomer.accounts ?? [];

      const accountDetails = await Promise.all(
        accounts.map((acc) => accountApi.getById(getId(acc)))
      );

      const activeCards = accountDetails.flatMap((accountDetail) =>
        (accountDetail.cards ?? [])
          .filter((cardItem) => getActiveValue(cardItem) === true)
          .map((cardItem) => ({ ...cardItem, accountName: accountDetail.name }))
      );

      if (activeCards.length === 0) {
        const error = new Error('Recipient has no active card to receive funds.');
        error.noActiveCard = true;
        throw error;
      }

      return { customer: foundCustomer, cards: activeCards };
    },
    retry: false,
    onSuccess: ({ customer, cards }) => {
      clearTransferErrors('receiverEmail');
      setTransferRecipient(customer);
      setRecipientCards(cards);
      setResolvedReceiverCardId(null);
    },
    onError: (error) => {
      setTransferRecipient(null);
      setRecipientCards([]);
      setResolvedReceiverCardId(null);
      setTransferError('receiverEmail', {
        type: 'server',
        message: error?.noActiveCard
          ? t('customer:no_active_card_recipient')
          : t('customer:no_customer_found'),
      });
    },
  });

  // Reuses the existing transfer endpoint -- no new backend call needed here.
  const transferMutation = useMutation({
    mutationFn: (payload) => cardApi.transfer(payload),
    retry: false,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: cardKeys.byId(selectedCardId) });
      showToast({ title: t('customer:transfer_successful'), variant: 'success' });
      resetTransferForm();
      setTransferRecipient(null);
      setRecipientCards([]);
      setResolvedReceiverCardId(null);
      setSearchedEmail(null);
      setActiveMoneyAction(null);
    },
  });

  const searchRecipient = () => {
    const searchEmail = getTransferValues('receiverEmail');
    if (!searchEmail) {
      setTransferError('receiverEmail', {
        type: 'manual',
        message: t('customer:recipient_email_required'),
      });
      return;
    }
    setTransferRecipient(null);
    setRecipientCards([]);
    setResolvedReceiverCardId(null);
    setSearchedEmail(searchEmail);
    searchRecipientMutation.mutate(searchEmail);
  };

  const submitTransfer = async (values) => {
    try {
      await transferMutation.mutateAsync({
        senderCardId: selectedCardId,
        receiverCardId: resolvedReceiverCardId,
        amount: trimValue(values.amount),
        currencyCode: values.currencyCode,
      });
    } catch (error) {
      applyBackendFormErrors(error, setTransferError, ['amount', 'currencyCode']);
    }
  };

  const selectAccount = (nextAccount) => {
    const id = getId(nextAccount);
    // clicking the account that's already open closes it (and its cards/detail)
    setSelectedAccountId((current) => (String(current) === String(id) ? null : id));
    setSelectedCardId(null);
    setShowCreateCard(false);
  };

  const selectCard = (nextCard) => {
    const id = getId(nextCard);
    setSelectedCardId((current) => (String(current) === String(id) ? null : id));
    setShowAddCurrency(false);
    setActiveMoneyAction(null);
    setTransferRecipient(null);
    setRecipientCards([]);
    setResolvedReceiverCardId(null);
    setSearchedEmail(null);
  };

  const renderAccountChip = (item) => (
    <>
      <span className={styles.chipTitle}>{formatValue(item.name)}</span>
      <span className={styles.chipMeta}>
        {t(`customer:category_${String(item.category ?? '').toLowerCase()}`) ||
          formatValue(item.category)}
      </span>
      <StatusBadge active={getActiveValue(item)} />
    </>
  );

  const renderCardChip = (item) => (
    <>
      <span className={styles.chipTitle}>
        {t(`customer:brand_${String(item.brand ?? '').toLowerCase()}`) || formatValue(item.brand)}{' '}
        &middot;{' '}
        {t(`customer:type_${String(item.type ?? '').toLowerCase()}`) || formatValue(item.type)}
      </span>
      <span className={styles.chipMeta}>{formatValue(item.panMasked)}</span>
      <span className={styles.chipMeta}>
        {t('customer:label_spending_limit')} {formatValue(item.spendingLimit)}
      </span>
    </>
  );

  const renderRecipientCardChip = (item) => (
    <>
      <span className={styles.chipTitle}>
        {t(`customer:brand_${String(item.brand ?? '').toLowerCase()}`) || formatValue(item.brand)}{' '}
        &middot;{' '}
        {t(`customer:type_${String(item.type ?? '').toLowerCase()}`) || formatValue(item.type)}
      </span>
      <span className={styles.chipMeta}>{formatValue(item.accountName)}</span>
      <span className={styles.chipMeta}>{formatValue(item.panMasked)}</span>
    </>
  );

  return (
    <div className={styles.page}>
      <header className={styles.pageHeader}>
        <p className={styles.kicker}>{t('customer:page_kicker')}</p>
        <h1 className={styles.title}>{t('customer:page_title')}</h1>
      </header>

      {/* ---- Profile ---- */}
      <Card title={t('customer:your_profile')}>
        {profileQuery.isLoading && (
          <div className={styles.centerState}>
            <Spinner label={t('customer:loading_profile')} />
          </div>
        )}

        {profileQuery.isError && <Toast variant="danger" message={t('customer:profile_error')} />}

        {profile && (
          <dl className={styles.profileGrid}>
            <EditableDetail
              label={t('customer:label_name')}
              isEditing={editingSection === 'name'}
              onEdit={() => openEdit('name')}
              onCancel={cancelEdit}
              isSaving={isNameSaving}
              editDisabled={editDisabled}
              onSubmit={handleNameSubmit(submitName)}
              viewValue={getCustomerName(profile)}
            >
              <div className={styles.editFields}>
                <TextField
                  id="profile-first-name"
                  label={t('customer:first_name')}
                  required
                  error={nameErrors.firstName?.message}
                  {...registerName('firstName', { required: t('customer:first_name_required') })}
                />
                <TextField
                  id="profile-last-name"
                  label={t('customer:last_name')}
                  required
                  error={nameErrors.lastName?.message}
                  {...registerName('lastName', { required: t('customer:last_name_required') })}
                />
              </div>
              {nameErrors.root && <Toast variant="danger" message={nameErrors.root.message} />}
            </EditableDetail>

            <DetailItem label={t('customer:label_email')}>{formatValue(profile.email)}</DetailItem>

            <EditableDetail
              label={t('customer:label_phone')}
              isEditing={editingSection === 'phone'}
              onEdit={() => openEdit('phone')}
              onCancel={cancelEdit}
              isSaving={isPhoneSaving}
              editDisabled={editDisabled}
              onSubmit={handlePhoneSubmit(submitPhone)}
              viewValue={formatValue(profile.phoneNumber)}
            >
              <TextField
                id="profile-phone-number"
                label={t('customer:phone_number')}
                type="tel"
                required
                error={phoneErrors.phoneNumber?.message}
                {...registerPhone('phoneNumber', {
                  required: t('customer:phone_required'),
                  pattern: { value: /^\d+$/, message: t('customer:phone_digits_only') },
                })}
              />
              {phoneErrors.root && <Toast variant="danger" message={phoneErrors.root.message} />}
            </EditableDetail>

            <DetailItem label={t('customer:label_dob')}>
              {formatValue(profile.dateOfBirth)}
            </DetailItem>

            <EditableDetail
              label={t('customer:label_address')}
              isEditing={editingSection === 'address'}
              onEdit={() => openEdit('address')}
              onCancel={cancelEdit}
              isSaving={isAddressSaving}
              editDisabled={editDisabled}
              onSubmit={handleAddressSubmit(submitAddress)}
              viewValue={formatValue(profile.address)}
            >
              <TextField
                id="profile-address"
                label={t('customer:address')}
                required
                error={addressErrors.address?.message}
                {...registerAddress('address', { required: t('customer:address_required') })}
              />
              {addressErrors.root && (
                <Toast variant="danger" message={addressErrors.root.message} />
              )}
            </EditableDetail>
          </dl>
        )}
      </Card>

      {/* ---- Accounts strip ---- */}
      {profile && (
        <Card title={t('customer:accounts')} subtitle={t('customer:accounts_subtitle')}>
          <div className={styles.stack}>
            <ScrollStrip
              items={profile.accounts ?? []}
              getKey={(item) => getId(item)}
              renderItem={renderAccountChip}
              selectedKey={selectedAccountId}
              onSelect={selectAccount}
              emptyMessage={t('customer:no_accounts')}
              ariaLabel={t('customer:accounts')}
            />

            {showCreateAccount ? (
              <form
                className={styles.editForm}
                onSubmit={handleCreateAccountSubmit(submitCreateAccount)}
                noValidate
              >
                <div className={styles.editFields}>
                  <TextField
                    id="create-account-name"
                    label={t('customer:account_name')}
                    error={createAccountErrors.accountName?.message}
                    required
                    {...registerCreateAccount('accountName', {
                      required: t('customer:account_name_required'),
                      minLength: { value: 3, message: t('customer:account_name_min') },
                      maxLength: { value: 20, message: t('customer:account_name_max') },
                    })}
                  />
                  <Select
                    id="create-account-category"
                    label={t('customer:category')}
                    placeholder={t('customer:choose_category')}
                    options={ACCOUNT_CATEGORIES.map((c) => ({
                      value: c.value,
                      label: t(`customer:category_${c.value.toLowerCase()}`),
                    }))}
                    error={createAccountErrors.category?.message}
                    required
                    {...registerCreateAccount('category', {
                      required: t('customer:category_required'),
                    })}
                  />
                </div>

                {createAccountErrors.root && (
                  <Toast variant="danger" message={createAccountErrors.root.message} />
                )}

                <div className={styles.actionsRow}>
                  <Button
                    type="submit"
                    size="sm"
                    isLoading={createAccountMutation.isPending || isCreateAccountSubmitting}
                  >
                    {t('common:save')}
                  </Button>
                  <Button
                    type="button"
                    variant="secondary"
                    size="sm"
                    disabled={createAccountMutation.isPending}
                    onClick={() => {
                      resetCreateAccountForm();
                      setShowCreateAccount(false);
                    }}
                  >
                    {t('common:cancel')}
                  </Button>
                </div>
              </form>
            ) : (
              <div className={styles.actionsRow}>
                <Button
                  type="button"
                  variant="secondary"
                  size="sm"
                  disabled={!customerId}
                  onClick={() => setShowCreateAccount(true)}
                >
                  {t('customer:new_account')}
                </Button>
              </div>
            )}
          </div>
        </Card>
      )}

      {/* ---- Account detail ---- */}
      {selectedAccountId && (
        <Card title={t('customer:account_detail')}>
          {accountQuery.isLoading && (
            <div className={styles.centerState}>
              <Spinner label={t('customer:loading_account')} />
            </div>
          )}
          {accountQuery.isError && <Toast variant="danger" message={t('customer:account_error')} />}
          {account && (
            <div className={styles.stack}>
              <dl className={styles.profileGrid}>
                <DetailItem label={t('customer:label_name')}>
                  {formatValue(account.name)}
                </DetailItem>
                <DetailItem label={t('customer:col_status')}>
                  <StatusBadge active={getActiveValue(account)} />
                </DetailItem>
                <DetailItem label={t('customer:label_category')}>
                  {formatValue(account.category)}
                </DetailItem>
                <DetailItem label={t('customer:label_opened')}>
                  {formatValue(account.dateOpened)}
                </DetailItem>
              </dl>

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
                    data={sortTransactionsDesc(account.transactions)}
                    getRowKey={(transaction, index) => `${transaction.timeStamp ?? 'txn'}-${index}`}
                    emptyMessage={t('customer:no_transactions')}
                    caption={t('accounts:label_transactions')}
                  />
                </div>
              )}
            </div>
          )}
        </Card>
      )}

      {/* ---- Cards strip (belongs to the selected account) ---- */}
      {account && (
        <Card title={t('customer:cards')} subtitle={t('customer:cards_subtitle')}>
          <div className={styles.stack}>
            <ScrollStrip
              items={account.cards ?? []}
              getKey={(item) => getId(item)}
              renderItem={renderCardChip}
              selectedKey={selectedCardId}
              onSelect={selectCard}
              emptyMessage={t('customer:no_cards')}
              ariaLabel={t('customer:cards')}
            />

            {showCreateCard ? (
              <form
                className={styles.editForm}
                onSubmit={handleCreateCardSubmit(submitCreateCard)}
                noValidate
              >
                <div className={styles.editFields}>
                  <Select
                    id="create-card-type"
                    label={t('customer:card_type')}
                    placeholder={t('customer:choose_type')}
                    options={CARD_TYPES.map((c) => ({
                      value: c.value,
                      label: t(`customer:type_${c.value.toLowerCase()}`),
                    }))}
                    error={createCardErrors.cardType?.message}
                    required
                    {...registerCreateCard('cardType', {
                      required: t('customer:card_type_required'),
                    })}
                  />
                  <Select
                    id="create-card-brand"
                    label={t('customer:card_brand')}
                    placeholder={t('customer:choose_brand')}
                    options={CARD_BRANDS.map((c) => ({
                      value: c.value,
                      label: t(`customer:brand_${c.value.toLowerCase()}`),
                    }))}
                    error={createCardErrors.cardBrand?.message}
                    required
                    {...registerCreateCard('cardBrand', {
                      required: t('customer:card_brand_required'),
                    })}
                  />
                  <TextField
                    id="create-card-spending-limit"
                    label={t('customer:label_spending_limit')}
                    type="number"
                    min="100"
                    max="100000"
                    step="0.01"
                    error={createCardErrors.spendingLimit?.message}
                    required
                    {...registerCreateCard('spendingLimit', {
                      required: t('customer:spending_limit_required'),
                      min: { value: 100, message: t('customer:spending_limit_min') },
                      max: { value: 100000, message: t('customer:spending_limit_max') },
                    })}
                  />
                  <TextField
                    id="create-card-pan"
                    label={t('customer:pan')}
                    inputMode="numeric"
                    error={createCardErrors.pan?.message}
                    required
                    {...registerCreateCard('pan', {
                      required: t('customer:pan_required'),
                      minLength: { value: 16, message: t('customer:pan_length') },
                      maxLength: { value: 16, message: t('customer:pan_length') },
                      pattern: { value: /^\d+$/, message: t('customer:pan_digits_only') },
                    })}
                  />
                </div>

                {createCardErrors.root && (
                  <Toast variant="danger" message={createCardErrors.root.message} />
                )}

                <div className={styles.actionsRow}>
                  <Button
                    type="submit"
                    size="sm"
                    isLoading={createCardMutation.isPending || isCreateCardSubmitting}
                  >
                    {t('common:save')}
                  </Button>
                  <Button
                    type="button"
                    variant="secondary"
                    size="sm"
                    disabled={createCardMutation.isPending}
                    onClick={() => {
                      resetCreateCardForm();
                      setShowCreateCard(false);
                    }}
                  >
                    {t('common:cancel')}
                  </Button>
                </div>
              </form>
            ) : (
              <div className={styles.actionsRow}>
                <Button
                  type="button"
                  variant="secondary"
                  size="sm"
                  onClick={() => setShowCreateCard(true)}
                >
                  {t('customer:new_card')}
                </Button>
              </div>
            )}
          </div>
        </Card>
      )}

      {/* ---- Card detail ---- */}
      {selectedCardId && (
        <Card title={t('customer:card_detail')}>
          {cardQuery.isLoading && (
            <div className={styles.centerState}>
              <Spinner label={t('customer:loading_card')} />
            </div>
          )}
          {cardQuery.isError && <Toast variant="danger" message={t('customer:card_error')} />}
          {card && (
            <div className={styles.stack}>
              <dl className={styles.profileGrid}>
                <DetailItem label={t('customer:label_brand')}>{formatValue(card.brand)}</DetailItem>
                <DetailItem label={t('customer:label_type')}>{formatValue(card.type)}</DetailItem>
                <DetailItem label={t('customer:col_status')}>
                  <StatusBadge active={getActiveValue(card)} />
                </DetailItem>
                <DetailItem label={t('customer:label_spending_limit')}>
                  {formatValue(card.spendingLimit)}
                </DetailItem>
                <DetailItem label={t('customer:label_expiration')}>
                  {formatValue(card.expirationDate)}
                </DetailItem>
                <DetailItem label={t('customer:label_card_number')}>
                  {formatValue(card.panToken)}
                </DetailItem>
              </dl>

              <Table
                columns={balanceColumns}
                data={card.cardBalances ?? []}
                getRowKey={(balance, index) => balance.currencyCode ?? index}
                emptyMessage={t('customer:no_balances') ?? t('cards:no_balances')}
                caption={t('cards:currency_balances')}
              />

              <div className={styles.actionsRow}>
                <Button
                  type="button"
                  variant="secondary"
                  size="sm"
                  onClick={() => setActiveMoneyAction('deposit')}
                >
                  {t('customer:deposit')}
                </Button>
                <Button
                  type="button"
                  variant="secondary"
                  size="sm"
                  onClick={() => setActiveMoneyAction('withdraw')}
                >
                  {t('customer:withdraw')}
                </Button>
                <Button
                  type="button"
                  variant="secondary"
                  size="sm"
                  onClick={() => setActiveMoneyAction('transfer')}
                >
                  {t('customer:transfer')}
                </Button>
                <Button
                  type="button"
                  variant="secondary"
                  size="sm"
                  disabled={cardOwnedCurrencyOptions.length < 2}
                  onClick={() => setActiveMoneyAction('exchange')}
                >
                  {t('customer:exchange')}
                </Button>
              </div>

              {activeMoneyAction === 'deposit' && (
                <form
                  className={styles.editForm}
                  onSubmit={handleDepositSubmit(submitDeposit)}
                  noValidate
                >
                  <div className={styles.editFields}>
                    <TextField
                      id="deposit-amount"
                      label={t('common:amount')}
                      type="number"
                      step="0.01"
                      required
                      error={depositErrors.amountToDeposit?.message}
                      {...registerDeposit('amountToDeposit', {
                        required: t('common:amount_required'),
                        min: { value: 0.01, message: t('common:amount_positive') },
                      })}
                    />
                    <Select
                      id="deposit-currency"
                      label={t('common:currency')}
                      placeholder={t('common:choose_currency')}
                      options={cardOwnedCurrencyOptions}
                      error={depositErrors.currencyCode?.message}
                      required
                      {...registerDeposit('currencyCode', {
                        required: t('common:currency_required'),
                      })}
                    />
                  </div>

                  {depositErrors.root && (
                    <Toast variant="danger" message={depositErrors.root.message} />
                  )}

                  <div className={styles.actionsRow}>
                    <Button
                      type="submit"
                      size="sm"
                      isLoading={depositMutation.isPending || isDepositSubmitting}
                    >
                      {t('customer:confirm_deposit')}
                    </Button>
                    <Button
                      type="button"
                      variant="secondary"
                      size="sm"
                      disabled={depositMutation.isPending}
                      onClick={() => {
                        resetDepositForm();
                        setActiveMoneyAction(null);
                      }}
                    >
                      {t('common:cancel')}
                    </Button>
                  </div>
                </form>
              )}

              {activeMoneyAction === 'withdraw' && (
                <form
                  className={styles.editForm}
                  onSubmit={handleWithdrawSubmit(submitWithdraw)}
                  noValidate
                >
                  <div className={styles.editFields}>
                    <TextField
                      id="withdraw-amount"
                      label={t('common:amount')}
                      type="number"
                      step="0.01"
                      required
                      error={withdrawErrors.amountToWithdraw?.message}
                      {...registerWithdraw('amountToWithdraw', {
                        required: t('common:amount_required'),
                        min: { value: 0.01, message: t('common:amount_positive') },
                      })}
                    />
                    <Select
                      id="withdraw-currency"
                      label={t('common:currency')}
                      placeholder={t('common:choose_currency')}
                      options={cardOwnedCurrencyOptions}
                      error={withdrawErrors.currencyCode?.message}
                      required
                      {...registerWithdraw('currencyCode', {
                        required: t('common:currency_required'),
                      })}
                    />
                  </div>

                  {withdrawErrors.root && (
                    <Toast variant="danger" message={withdrawErrors.root.message} />
                  )}

                  <div className={styles.actionsRow}>
                    <Button
                      type="submit"
                      size="sm"
                      isLoading={withdrawMutation.isPending || isWithdrawSubmitting}
                    >
                      {t('customer:confirm_withdrawal')}
                    </Button>
                    <Button
                      type="button"
                      variant="secondary"
                      size="sm"
                      disabled={withdrawMutation.isPending}
                      onClick={() => {
                        resetWithdrawForm();
                        setActiveMoneyAction(null);
                      }}
                    >
                      {t('common:cancel')}
                    </Button>
                  </div>
                </form>
              )}

              {activeMoneyAction === 'exchange' && (
                <form
                  className={styles.editForm}
                  onSubmit={handleExchangeSubmit(submitExchange)}
                  noValidate
                >
                  <div className={styles.editFields}>
                    <TextField
                      id="exchange-amount"
                      label={t('common:amount')}
                      type="number"
                      step="0.01"
                      required
                      error={exchangeErrors.amount?.message}
                      {...registerExchange('amount', {
                        required: t('common:amount_required'),
                        min: { value: 0.01, message: t('common:amount_positive') },
                      })}
                    />
                    <Select
                      id="exchange-from-currency"
                      label={t('cards:lbl_from_currency') || t('customer:from_currency')}
                      placeholder={t('common:choose_currency')}
                      options={cardOwnedCurrencyOptions}
                      error={exchangeErrors.fromCurrencyCode?.message}
                      required
                      {...registerExchange('fromCurrencyCode', {
                        required: t('customer:from_currency_required'),
                      })}
                    />
                    <Select
                      id="exchange-to-currency"
                      label={t('cards:lbl_to_currency') || t('customer:to_currency')}
                      placeholder={t('common:choose_currency')}
                      options={cardOwnedCurrencyOptions}
                      error={exchangeErrors.toCurrencyCode?.message}
                      required
                      {...registerExchange('toCurrencyCode', {
                        required: t('customer:to_currency_required'),
                      })}
                    />
                  </div>

                  {exchangeErrors.root && (
                    <Toast variant="danger" message={exchangeErrors.root.message} />
                  )}

                  <div className={styles.actionsRow}>
                    <Button
                      type="submit"
                      size="sm"
                      isLoading={exchangeMutation.isPending || isExchangeSubmitting}
                    >
                      {t('customer:confirm_exchange')}
                    </Button>
                    <Button
                      type="button"
                      variant="secondary"
                      size="sm"
                      disabled={exchangeMutation.isPending}
                      onClick={() => {
                        resetExchangeForm();
                        setActiveMoneyAction(null);
                      }}
                    >
                      {t('common:cancel')}
                    </Button>
                  </div>
                </form>
              )}

              {activeMoneyAction === 'transfer' && (
                <div className={styles.editForm}>
                  <div className={styles.editFields}>
                    <TextField
                      id="transfer-receiver-email"
                      label={t('customer:recipient_email')}
                      type="email"
                      required
                      error={transferErrors.receiverEmail?.message}
                      {...registerTransfer('receiverEmail', {
                        required: t('customer:recipient_email_required'),
                      })}
                    />
                  </div>

                  <div className={styles.actionsRow}>
                    <Button
                      type="button"
                      size="sm"
                      isLoading={searchRecipientMutation.isPending}
                      onClick={searchRecipient}
                    >
                      {t('common:search')}
                    </Button>
                    <Button
                      type="button"
                      variant="secondary"
                      size="sm"
                      onClick={() => {
                        resetTransferForm();
                        setTransferRecipient(null);
                        setResolvedReceiverCardId(null);
                        setSearchedEmail(null);
                        setActiveMoneyAction(null);
                      }}
                    >
                      {t('common:cancel')}
                    </Button>
                  </div>

                  {transferRecipient && (
                    <div className={styles.editForm}>
                      <p className={styles.mutedText}>
                        {t('customer:sending_to', { name: getCustomerName(transferRecipient) })}
                      </p>

                      <ScrollStrip
                        items={recipientCards}
                        getKey={(item) => getId(item)}
                        renderItem={renderRecipientCardChip}
                        selectedKey={resolvedReceiverCardId}
                        onSelect={(pickedCard) => setResolvedReceiverCardId(getId(pickedCard))}
                        emptyMessage={t('customer:no_active_cards')}
                        ariaLabel={t('customer:cards')}
                      />

                      {resolvedReceiverCardId && (
                        <form
                          className={styles.editForm}
                          onSubmit={handleTransferSubmit(submitTransfer)}
                          noValidate
                        >
                          <div className={styles.editFields}>
                            <TextField
                              id="transfer-amount"
                              label={t('common:amount')}
                              type="number"
                              step="0.01"
                              required
                              error={transferErrors.amount?.message}
                              {...registerTransfer('amount', {
                                required: t('common:amount_required'),
                                min: { value: 0.01, message: t('common:amount_positive') },
                              })}
                            />
                            <Select
                              id="transfer-currency"
                              label={t('common:currency')}
                              placeholder={t('common:choose_currency')}
                              options={cardOwnedCurrencyOptions}
                              error={transferErrors.currencyCode?.message}
                              required
                              {...registerTransfer('currencyCode', {
                                required: t('common:currency_required'),
                              })}
                            />
                          </div>

                          {transferErrors.root && (
                            <Toast variant="danger" message={transferErrors.root.message} />
                          )}

                          <div className={styles.actionsRow}>
                            <Button
                              type="submit"
                              size="sm"
                              isLoading={transferMutation.isPending || isTransferSubmitting}
                            >
                              {t('customer:confirm_transfer')}
                            </Button>
                          </div>
                        </form>
                      )}
                    </div>
                  )}
                </div>
              )}

              {showAddCurrency ? (
                <form
                  className={styles.editForm}
                  onSubmit={handleAddCurrencySubmit(submitAddCurrency)}
                  noValidate
                >
                  <Select
                    id="add-currency-code"
                    label={t('common:currency')}
                    placeholder={t('common:choose_currency')}
                    options={availableCurrencyOptions}
                    error={addCurrencyErrors.currencyCode?.message}
                    required
                    {...registerAddCurrency('currencyCode', {
                      required: t('common:currency_required'),
                    })}
                  />

                  {addCurrencyErrors.root && (
                    <Toast variant="danger" message={addCurrencyErrors.root.message} />
                  )}

                  <div className={styles.actionsRow}>
                    <Button
                      type="submit"
                      size="sm"
                      isLoading={addCurrencyMutation.isPending || isAddCurrencySubmitting}
                    >
                      {t('common:save')}
                    </Button>
                    <Button
                      type="button"
                      variant="secondary"
                      size="sm"
                      disabled={addCurrencyMutation.isPending}
                      onClick={() => {
                        resetAddCurrencyForm();
                        setShowAddCurrency(false);
                      }}
                    >
                      {t('common:cancel')}
                    </Button>
                  </div>
                </form>
              ) : availableCurrencyOptions.length === 0 ? (
                <p className={styles.mutedText}>{t('customer:all_currencies_added')}</p>
              ) : (
                <div className={styles.actionsRow}>
                  <Button
                    type="button"
                    variant="secondary"
                    size="sm"
                    onClick={() => setShowAddCurrency(true)}
                  >
                    {t('customer:add_currency')}
                  </Button>
                </div>
              )}
            </div>
          )}
        </Card>
      )}
    </div>
  );
}
