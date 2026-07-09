import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { useAuth } from '../components/AuthContext';
import { customerApi } from '../api/customerApi';
import { accountApi } from '../api/accountApi';
import { cardApi } from '../api/cardApi';
import { customerKeys } from '../api/customerQueryKeys';
import { accountKeys } from '../api/accountQueryKeys';
import { cardKeys } from '../api/cardQueryKeys';
import { Card, Spinner, Table, Toast } from '../components/ui';
import ScrollStrip from '../components/customer/ScrollStrip';
import {
  DetailItem,
  StatusBadge,
  formatValue,
  getActiveValue,
  getCustomerName,
  getId,
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

export default function CustomersPage() {
  const { email } = useAuth();

  const [selectedAccountId, setSelectedAccountId] = useState(null);
  const [selectedCardId, setSelectedCardId] = useState(null);

  // My profile (email comes from the JWT via AuthContext). The response already
  // embeds account summaries, so this one call also feeds the accounts strip.
  const profileQuery = useQuery({
    queryKey: email ? customerKeys.byEmail(email) : [...customerKeys.all, IDLE],
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
            <DetailItem label="Name">{getCustomerName(profile)}</DetailItem>
            <DetailItem label="Email">{formatValue(profile.email)}</DetailItem>
            <DetailItem label="Phone">{formatValue(profile.phoneNumber)}</DetailItem>
            <DetailItem label="Date of birth">{formatValue(profile.dateOfBirth)}</DetailItem>
            <DetailItem label="Address">{formatValue(profile.address)}</DetailItem>
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
            <dl className={styles.profileGrid}>
              <DetailItem label="Name">{formatValue(account.name)}</DetailItem>
              <DetailItem label="Status">
                <StatusBadge active={getActiveValue(account)} />
              </DetailItem>
              <DetailItem label="Category">{formatValue(account.category)}</DetailItem>
              <DetailItem label="Opened">{formatValue(account.dateOpened)}</DetailItem>
            </dl>
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
