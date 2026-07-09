import { useEffect, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
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

const balanceColumns = [
  {
    key: 'currencyCode',
    header: 'Currency',
    render: (balance) => formatValue(balance.currencyCode),
  },
  {
    key: 'amount',
    header: 'Amount',
    align: 'right',
    render: (balance) => formatValue(balance.amount),
  },
];

const transactionColumns = [
  {
    key: 'timeStamp',
    header: 'Date',
    render: (transaction) => formatValue(transaction.timeStamp),
  },
  {
    key: 'transactionType',
    header: 'Type',
    render: (transaction) => formatValue(transaction.transactionType),
  },
  {
    key: 'amount',
    header: 'Amount',
    align: 'right',
    render: (transaction) => formatValue(transaction.amount),
  },
  {
    key: 'currencyCode',
    header: 'Currency',
    render: (transaction) => formatValue(transaction.currencyCode),
  },
  {
    key: 'status',
    header: 'Status',
    render: (transaction) => formatValue(transaction.status),
  },
  {
    key: 'description',
    header: 'Description',
    render: (transaction) => formatValue(transaction.description),
  },
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
  return (
    <DetailItem label={label}>
      {isEditing ? (
        <form className={styles.editForm} onSubmit={onSubmit} noValidate>
          {children}
          <div className={styles.actionsRow}>
            <Button type="submit" size="sm" isLoading={isSaving}>
              Save
            </Button>
            <Button
              type="button"
              variant="secondary"
              size="sm"
              disabled={isSaving}
              onClick={onCancel}
            >
              Cancel
            </Button>
          </div>
        </form>
      ) : (
        <div className={styles.viewRow}>
          <span>{viewValue}</span>
          <Button type="button" variant="ghost" size="sm" disabled={editDisabled} onClick={onEdit}>
            Edit
          </Button>
        </div>
      )}
    </DetailItem>
  );
}

export default function CustomersPage() {
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
  );
  const cardOwnedCurrencyOptions = CURRENCY_OPTIONS.filter((option) =>
    cardCurrencyCodes.has(option.value)
  );

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
      showToast({ title: 'Profile updated.', variant: 'success' });
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
      showToast({ title: 'Account created and added to your profile.', variant: 'success' });
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
      showToast({ title: 'Card created for this account.', variant: 'success' });
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
          message: 'The account was created, but its ID was not returned.',
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
      showToast({ title: 'Currency balance added.', variant: 'success' });
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

  // Deposit/withdraw only return a plain confirmation string, not the updated
  // balance, so refetch the card rather than trying to patch the cache by hand.
  const depositMutation = useMutation({
    mutationFn: ({ cardId, payload }) => cardApi.deposit(cardId, payload),
    retry: false,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: cardKeys.byId(selectedCardId) });
      showToast({ title: 'Deposit successful.', variant: 'success' });
      resetDepositForm();
      setActiveMoneyAction(null);
    },
  });

  const withdrawMutation = useMutation({
    mutationFn: ({ cardId, payload }) => cardApi.withdraw(cardId, payload),
    retry: false,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: cardKeys.byId(selectedCardId) });
      showToast({ title: 'Withdrawal successful.', variant: 'success' });
      resetWithdrawForm();
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
          ? 'Recipient has no active card to receive funds.'
          : 'No customer found with that email.',
      });
    },
  });

  // Reuses the existing transfer endpoint -- no new backend call needed here.
  const transferMutation = useMutation({
    mutationFn: (payload) => cardApi.transfer(payload),
    retry: false,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: cardKeys.byId(selectedCardId) });
      showToast({ title: 'Transfer successful.', variant: 'success' });
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
      setTransferError('receiverEmail', { type: 'manual', message: 'Enter an email to search.' });
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
      <span className={styles.chipMeta}>Limit {formatValue(item.spendingLimit)}</span>
    </>
  );

  const renderRecipientCardChip = (item) => (
    <>
      <span className={styles.chipTitle}>
        {formatValue(item.brand)} &middot; {formatValue(item.type)}
      </span>
      <span className={styles.chipMeta}>{formatValue(item.accountName)}</span>
      <span className={styles.chipMeta}>{formatValue(item.panMasked)}</span>
    </>
  );

  return (
    <div className={styles.page}>
      <header className={styles.pageHeader}>
        <p className={styles.kicker}>Profile</p>
        <h1 className={styles.title}>Your banking</h1>
      </header>

      {/* ---- Profile ---- */}
      <Card title="Your profile">
        {profileQuery.isLoading && (
          <div className={styles.centerState}>
            <Spinner label="Loading your profile..." />
          </div>
        )}

        {profileQuery.isError && (
          <Toast variant="danger" message="We couldn't load your profile. Try again in a moment." />
        )}

        {profile && (
          <dl className={styles.profileGrid}>
            <EditableDetail
              label="Name"
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
                  label="First name"
                  required
                  error={nameErrors.firstName?.message}
                  {...registerName('firstName', { required: 'First name is required.' })}
                />
                <TextField
                  id="profile-last-name"
                  label="Last name"
                  required
                  error={nameErrors.lastName?.message}
                  {...registerName('lastName', { required: 'Last name is required.' })}
                />
              </div>
              {nameErrors.root && <Toast variant="danger" message={nameErrors.root.message} />}
            </EditableDetail>

            <DetailItem label="Email">{formatValue(profile.email)}</DetailItem>

            <EditableDetail
              label="Phone"
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
                label="Phone number"
                type="tel"
                required
                error={phoneErrors.phoneNumber?.message}
                {...registerPhone('phoneNumber', {
                  required: 'Phone number is required.',
                  pattern: { value: /^\d+$/, message: 'Phone number must contain only digits.' },
                })}
              />
              {phoneErrors.root && <Toast variant="danger" message={phoneErrors.root.message} />}
            </EditableDetail>

            <DetailItem label="Date of birth">{formatValue(profile.dateOfBirth)}</DetailItem>

            <EditableDetail
              label="Address"
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
                label="Address"
                required
                error={addressErrors.address?.message}
                {...registerAddress('address', { required: 'Address is required.' })}
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
        <Card title="Accounts" subtitle="Select an account to see its details.">
          <div className={styles.stack}>
            <ScrollStrip
              items={profile.accounts ?? []}
              getKey={(item) => getId(item)}
              renderItem={renderAccountChip}
              selectedKey={selectedAccountId}
              onSelect={selectAccount}
              emptyMessage="You don't have any accounts yet."
              ariaLabel="Your accounts"
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
                    {...registerCreateAccount('category', { required: 'Category is required.' })}
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
                    Save
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
                    Cancel
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
                  + New account
                </Button>
              </div>
            )}
          </div>
        </Card>
      )}

      {/* ---- Account detail ---- */}
      {selectedAccountId && (
        <Card title="Account detail">
          {accountQuery.isLoading && (
            <div className={styles.centerState}>
              <Spinner label="Loading account..." />
            </div>
          )}
          {accountQuery.isError && (
            <Toast variant="danger" message="We couldn't load that account." />
          )}
          {account && (
            <div className={styles.stack}>
              <dl className={styles.profileGrid}>
                <DetailItem label="Name">{formatValue(account.name)}</DetailItem>
                <DetailItem label="Status">
                  <StatusBadge active={getActiveValue(account)} />
                </DetailItem>
                <DetailItem label="Category">{formatValue(account.category)}</DetailItem>
                <DetailItem label="Opened">{formatValue(account.dateOpened)}</DetailItem>
              </dl>

              <div className={styles.actionsRow}>
                <Button
                  type="button"
                  variant="secondary"
                  size="sm"
                  aria-expanded={showTransactions}
                  onClick={() => setShowTransactions((visible) => !visible)}
                >
                  {showTransactions ? 'Hide transactions' : 'Show transactions'}
                </Button>
              </div>

              {showTransactions && (
                <div className={styles.scrollTableWrap}>
                  <Table
                    columns={transactionColumns}
                    data={sortTransactionsDesc(account.transactions)}
                    getRowKey={(transaction, index) => `${transaction.timeStamp ?? 'txn'}-${index}`}
                    emptyMessage="No transactions yet."
                    caption="Transactions"
                  />
                </div>
              )}
            </div>
          )}
        </Card>
      )}

      {/* ---- Cards strip (belongs to the selected account) ---- */}
      {account && (
        <Card title="Cards" subtitle="Select a card to see its details.">
          <div className={styles.stack}>
            <ScrollStrip
              items={account.cards ?? []}
              getKey={(item) => getId(item)}
              renderItem={renderCardChip}
              selectedKey={selectedCardId}
              onSelect={selectCard}
              emptyMessage="This account has no cards."
              ariaLabel="Cards in this account"
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
                  <TextField
                    id="create-card-pan"
                    label="PAN"
                    inputMode="numeric"
                    error={createCardErrors.pan?.message}
                    required
                    {...registerCreateCard('pan', {
                      required: 'PAN is required.',
                      minLength: { value: 16, message: 'PAN must be exactly 16 digits.' },
                      maxLength: { value: 16, message: 'PAN must be exactly 16 digits.' },
                      pattern: { value: /^\d+$/, message: 'PAN must contain only digits.' },
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
                    Save
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
                    Cancel
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
                  + New card
                </Button>
              </div>
            )}
          </div>
        </Card>
      )}

      {/* ---- Card detail ---- */}
      {selectedCardId && (
        <Card title="Card detail">
          {cardQuery.isLoading && (
            <div className={styles.centerState}>
              <Spinner label="Loading card..." />
            </div>
          )}
          {cardQuery.isError && <Toast variant="danger" message="We couldn't load that card." />}
          {card && (
            <div className={styles.stack}>
              <dl className={styles.profileGrid}>
                <DetailItem label="Brand">{formatValue(card.brand)}</DetailItem>
                <DetailItem label="Type">{formatValue(card.type)}</DetailItem>
                <DetailItem label="Status">
                  <StatusBadge active={getActiveValue(card)} />
                </DetailItem>
                <DetailItem label="Spending limit">{formatValue(card.spendingLimit)}</DetailItem>
                <DetailItem label="Expiration">{formatValue(card.expirationDate)}</DetailItem>
                <DetailItem label="Card number">{formatValue(card.panToken)}</DetailItem>
              </dl>

              <Table
                columns={balanceColumns}
                data={card.cardBalances ?? []}
                getRowKey={(balance, index) => balance.currencyCode ?? index}
                emptyMessage="No balances on this card."
                caption="Card balances"
              />

              <div className={styles.actionsRow}>
                <Button
                  type="button"
                  variant="secondary"
                  size="sm"
                  onClick={() => setActiveMoneyAction('deposit')}
                >
                  + Deposit
                </Button>
                <Button
                  type="button"
                  variant="secondary"
                  size="sm"
                  onClick={() => setActiveMoneyAction('withdraw')}
                >
                  + Withdraw
                </Button>
                <Button
                  type="button"
                  variant="secondary"
                  size="sm"
                  onClick={() => setActiveMoneyAction('transfer')}
                >
                  + Transfer
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
                      label="Amount"
                      type="number"
                      step="0.01"
                      required
                      error={depositErrors.amountToDeposit?.message}
                      {...registerDeposit('amountToDeposit', {
                        required: 'Amount is required.',
                        min: { value: 0.01, message: 'Amount must be greater than 0.' },
                      })}
                    />
                    <Select
                      id="deposit-currency"
                      label="Currency"
                      placeholder="Choose currency"
                      options={cardOwnedCurrencyOptions}
                      error={depositErrors.currencyCode?.message}
                      required
                      {...registerDeposit('currencyCode', { required: 'Currency is required.' })}
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
                      Confirm deposit
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
                      Cancel
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
                      label="Amount"
                      type="number"
                      step="0.01"
                      required
                      error={withdrawErrors.amountToWithdraw?.message}
                      {...registerWithdraw('amountToWithdraw', {
                        required: 'Amount is required.',
                        min: { value: 0.01, message: 'Amount must be greater than 0.' },
                      })}
                    />
                    <Select
                      id="withdraw-currency"
                      label="Currency"
                      placeholder="Choose currency"
                      options={cardOwnedCurrencyOptions}
                      error={withdrawErrors.currencyCode?.message}
                      required
                      {...registerWithdraw('currencyCode', { required: 'Currency is required.' })}
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
                      Confirm withdrawal
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
                      Cancel
                    </Button>
                  </div>
                </form>
              )}

              {activeMoneyAction === 'transfer' && (
                <div className={styles.editForm}>
                  <div className={styles.editFields}>
                    <TextField
                      id="transfer-receiver-email"
                      label="Recipient email"
                      type="email"
                      required
                      error={transferErrors.receiverEmail?.message}
                      {...registerTransfer('receiverEmail', {
                        required: 'Enter an email to search.',
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
                      Search
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
                      Cancel
                    </Button>
                  </div>

                  {transferRecipient && (
                    <div className={styles.editForm}>
                      <p className={styles.mutedText}>
                        Sending to: {getCustomerName(transferRecipient)} -- choose a card to receive
                        the funds.
                      </p>

                      <ScrollStrip
                        items={recipientCards}
                        getKey={(item) => getId(item)}
                        renderItem={renderRecipientCardChip}
                        selectedKey={resolvedReceiverCardId}
                        onSelect={(pickedCard) => setResolvedReceiverCardId(getId(pickedCard))}
                        emptyMessage="This customer has no active cards."
                        ariaLabel="Recipient's cards"
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
                              label="Amount"
                              type="number"
                              step="0.01"
                              required
                              error={transferErrors.amount?.message}
                              {...registerTransfer('amount', {
                                required: 'Amount is required.',
                                min: { value: 0.01, message: 'Amount must be greater than 0.' },
                              })}
                            />
                            <Select
                              id="transfer-currency"
                              label="Currency"
                              placeholder="Choose currency"
                              options={cardOwnedCurrencyOptions}
                              error={transferErrors.currencyCode?.message}
                              required
                              {...registerTransfer('currencyCode', {
                                required: 'Currency is required.',
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
                              Confirm transfer
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
                    label="Currency"
                    placeholder="Choose currency"
                    options={availableCurrencyOptions}
                    error={addCurrencyErrors.currencyCode?.message}
                    required
                    {...registerAddCurrency('currencyCode', { required: 'Currency is required.' })}
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
                      Save
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
                      Cancel
                    </Button>
                  </div>
                </form>
              ) : availableCurrencyOptions.length === 0 ? (
                <p className={styles.mutedText}>All available currencies have been added.</p>
              ) : (
                <div className={styles.actionsRow}>
                  <Button
                    type="button"
                    variant="secondary"
                    size="sm"
                    onClick={() => setShowAddCurrency(true)}
                  >
                    + Add currency
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
