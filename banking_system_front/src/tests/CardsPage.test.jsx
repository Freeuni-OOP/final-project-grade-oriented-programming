import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { ToastProvider } from '../components/ToastProvider';
import CardsPage from '../pages/CardsPage';

const mockAuth = vi.hoisted(() => ({
  authority: 'STANDARD',
}));

vi.mock('../components/AuthContext', () => ({
  useAuth: () => mockAuth,
}));

vi.mock('../api/cardApi', () => ({
  cardApi: {
    getById: vi.fn(),
    getLinkedAccount: vi.fn(),
    getBalances: vi.fn(),
    checkExpiration: vi.fn(),
    deposit: vi.fn(),
    withdraw: vi.fn(),
    transfer: vi.fn(),
    exchangeCurrency: vi.fn(),
    addCurrency: vi.fn(),
    activate: vi.fn(),
    deactivate: vi.fn(),
    delete: vi.fn(),
  },
}));

import { cardApi } from '../api/cardApi';

function renderWithProviders(ui) {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false },
      mutations: { retry: false },
    },
  });

  return render(
    <QueryClientProvider client={queryClient}>
      <ToastProvider>{ui}</ToastProvider>
    </QueryClientProvider>
  );
}

function getSectionByHeading(name) {
  return screen.getByRole('heading', { name }).closest('section');
}

function mockReadEndpoints({ active = true, expired = false } = {}) {
  cardApi.getById.mockResolvedValue({
    type: 'DEBIT',
    brand: 'VISA',
    spendingLimit: '1000',
    expirationDate: '2029-01-01',
    panMasked: '**** 1234',
    isActive: active,
    cardBalances: [],
  });
  cardApi.getLinkedAccount.mockResolvedValue({
    name: 'Daily account',
    category: 'CHECKING',
    dateOpened: '2025-01-10',
    isActive: true,
  });
  cardApi.getBalances.mockResolvedValue([
    { amount: '50.00', currencyCode: 'GEL' },
    { amount: '12.50', currencyCode: 'USD' },
  ]);
  cardApi.checkExpiration.mockResolvedValue(expired);
}

async function loadCard(user, id = '10', options) {
  mockReadEndpoints(options);

  renderWithProviders(<CardsPage />);

  const section = within(getSectionByHeading('Find card'));
  await user.type(section.getByLabelText(/card id/i), id);
  await user.click(section.getByRole('button', { name: 'Load card' }));

  await screen.findByText('**** 1234');
}

describe('CardsPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockAuth.authority = 'STANDARD';
  });

  it('loads card detail, linked account, balances, and expiration state', async () => {
    const user = userEvent.setup();

    await loadCard(user);

    await waitFor(() => expect(cardApi.getById).toHaveBeenCalledWith('10'));
    expect(cardApi.getLinkedAccount).toHaveBeenCalledWith('10');
    expect(cardApi.getBalances).toHaveBeenCalledWith('10');
    expect(cardApi.checkExpiration).toHaveBeenCalledWith('10');
    expect(screen.getByText('Daily account')).toBeInTheDocument();
    expect(screen.getByText('50.00')).toBeInTheDocument();
    expect(screen.getByText('12.50')).toBeInTheDocument();
    expect(screen.getByText('Valid')).toBeInTheDocument();
  });

  it('validates deposit amount and submits a valid deposit', async () => {
    const user = userEvent.setup();
    cardApi.deposit.mockResolvedValue('ok');

    await loadCard(user);

    const section = within(getSectionByHeading('Deposit'));
    await user.type(section.getByLabelText(/amount/i), '-5');
    await user.selectOptions(section.getByLabelText(/currency/i), 'GEL');
    await user.click(section.getByRole('button', { name: 'Deposit' }));

    expect(await section.findByText('Amount must be positive.')).toBeInTheDocument();
    expect(cardApi.deposit).not.toHaveBeenCalled();

    await user.clear(section.getByLabelText(/amount/i));
    await user.type(section.getByLabelText(/amount/i), '25');
    await user.click(section.getByRole('button', { name: 'Deposit' }));

    await waitFor(() =>
      expect(cardApi.deposit).toHaveBeenCalledWith('10', {
        amountToDeposit: '25',
        currencyCode: 'GEL',
      })
    );
    await waitFor(() => expect(cardApi.getBalances).toHaveBeenCalledTimes(2));
  });

  it('submits withdraw, transfer, exchange, and add-currency operations', async () => {
    const user = userEvent.setup();
    cardApi.withdraw.mockResolvedValue('ok');
    cardApi.transfer.mockResolvedValue('ok');
    cardApi.exchangeCurrency.mockResolvedValue('ok');
    cardApi.addCurrency.mockResolvedValue({ ok: true });

    await loadCard(user);

    const withdraw = within(getSectionByHeading('Withdraw'));
    await user.type(withdraw.getByLabelText(/amount/i), '10');
    await user.selectOptions(withdraw.getByLabelText(/currency/i), 'USD');
    await user.click(withdraw.getByRole('button', { name: 'Withdraw' }));
    await waitFor(() =>
      expect(cardApi.withdraw).toHaveBeenCalledWith('10', {
        amountToWithdraw: '10',
        currencyCode: 'USD',
      })
    );

    const transfer = within(getSectionByHeading('Transfer'));
    await user.type(transfer.getByLabelText(/receiver card id/i), '11');
    await user.type(transfer.getByLabelText(/amount/i), '5');
    await user.selectOptions(transfer.getByLabelText(/currency/i), 'EUR');
    await user.click(transfer.getByRole('button', { name: 'Transfer' }));
    await waitFor(() =>
      expect(cardApi.transfer).toHaveBeenCalledWith({
        senderCardId: 10,
        receiverCardId: 11,
        amount: '5',
        currencyCode: 'EUR',
      })
    );

    const exchange = within(getSectionByHeading('Exchange currency'));
    await user.type(exchange.getByLabelText(/amount/i), '7');
    await user.selectOptions(exchange.getByLabelText(/from currency/i), 'USD');
    await user.selectOptions(exchange.getByLabelText(/to currency/i), 'GEL');
    await user.click(exchange.getByRole('button', { name: 'Exchange' }));
    await waitFor(() =>
      expect(cardApi.exchangeCurrency).toHaveBeenCalledWith('10', {
        amount: '7',
        fromCurrencyCode: 'USD',
        toCurrencyCode: 'GEL',
      })
    );

    const addCurrency = within(getSectionByHeading('Add currency'));
    await user.selectOptions(addCurrency.getByLabelText(/currency/i), 'GBP');
    await user.click(addCurrency.getByRole('button', { name: 'Add currency' }));
    await waitFor(() => expect(cardApi.addCurrency).toHaveBeenCalledWith('10', 'GBP'));
  });

  it('deactivates an active card', async () => {
    const user = userEvent.setup();
    cardApi.deactivate.mockResolvedValue('ok');

    await loadCard(user);

    const detail = within(getSectionByHeading('Card detail'));
    await user.click(detail.getByRole('button', { name: 'Deactivate' }));
    await waitFor(() => expect(cardApi.deactivate).toHaveBeenCalledWith('10'));
  });

  it('activates an inactive card', async () => {
    const user = userEvent.setup();
    cardApi.activate.mockResolvedValue('ok');

    await loadCard(user, '10', { active: false });

    const detail = within(getSectionByHeading('Card detail'));
    await user.click(detail.getByRole('button', { name: 'Activate' }));
    await waitFor(() => expect(cardApi.activate).toHaveBeenCalledWith('10'));
  });

  it('hides delete from standard users', async () => {
    const standardUser = userEvent.setup();
    await loadCard(standardUser);

    expect(screen.queryByRole('heading', { name: 'Delete card' })).not.toBeInTheDocument();
  });

  it('confirms manager delete before calling the API', async () => {
    mockAuth.authority = 'MANAGER';
    cardApi.delete.mockResolvedValue('ok');
    const manager = userEvent.setup();

    await loadCard(manager);

    const section = within(getSectionByHeading('Delete card'));
    await manager.click(section.getByRole('button', { name: 'Delete card' }));

    expect(cardApi.delete).not.toHaveBeenCalled();
    const dialog = screen.getByRole('dialog', { name: 'Delete card' });
    await manager.click(within(dialog).getByRole('button', { name: 'Delete card' }));

    await waitFor(() => expect(cardApi.delete).toHaveBeenCalledWith('10'));
  });
});
