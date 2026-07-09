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
import { Button, Card, Spinner, Table, TextField, Toast, useToast } from '../components/ui';
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
  const [editingSection, setEditingSection] = useState(null); // null | 'name' | 'phone' | 'address'
  const [showTransactions, setShowTransactions] = useState(false);

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

  const selectAccount = (nextAccount) => {
    const id = getId(nextAccount);
    // clicking the account that's already open closes it (and its cards/detail)
    setSelectedAccountId((current) => (String(current) === String(id) ? null : id));
    setSelectedCardId(null);
  };

  const selectCard = (nextCard) => {
    const id = getId(nextCard);
    setSelectedCardId((current) => (String(current) === String(id) ? null : id));
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
          <ScrollStrip
            items={profile.accounts ?? []}
            getKey={(item) => getId(item)}
            renderItem={renderAccountChip}
            selectedKey={selectedAccountId}
            onSelect={selectAccount}
            emptyMessage="You don't have any accounts yet."
            ariaLabel="Your accounts"
          />
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
          <ScrollStrip
            items={account.cards ?? []}
            getKey={(item) => getId(item)}
            renderItem={renderCardChip}
            selectedKey={selectedCardId}
            onSelect={selectCard}
            emptyMessage="This account has no cards."
            ariaLabel="Cards in this account"
          />
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
            </div>
          )}
        </Card>
      )}
    </div>
  );
}
