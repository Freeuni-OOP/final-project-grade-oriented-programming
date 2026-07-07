import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { ToastProvider } from '../components/ToastProvider';
import AccountsPage from '../pages/AccountsPage';

const mockAuth = vi.hoisted(() => ({
  authority: 'STANDARD',
}));

vi.mock('../components/AuthContext', () => ({
  useAuth: () => mockAuth,
}));

vi.mock('../api/accountApi', () => ({
  accountApi: {
    getByEmail: vi.fn(),
    getByCustomerId: vi.fn(),
    getById: vi.fn(),
    getBalanceByCurrency: vi.fn(),
    create: vi.fn(),
    updateName: vi.fn(),
    activate: vi.fn(),
    deactivate: vi.fn(),
    registerCustomer: vi.fn(),
    createCard: vi.fn(),
    delete: vi.fn(),
  },
}));

import { accountApi } from '../api/accountApi';

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

describe('AccountsPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockAuth.authority = 'STANDARD';
  });

  it('loads account summaries by customer email', async () => {
    const user = userEvent.setup();
    accountApi.getByEmail.mockResolvedValue([
      {
        name: 'Daily account',
        category: 'CHECKING',
        dateOpened: '2025-01-10',
        active: true,
      },
    ]);

    renderWithProviders(<AccountsPage />);

    await user.type(screen.getByLabelText(/customer email/i), 'nino@test.com');
    await user.click(screen.getByRole('button', { name: 'Load summaries' }));

    await waitFor(() => expect(accountApi.getByEmail).toHaveBeenCalledWith('nino@test.com'));
    expect(await screen.findByText('Daily account')).toBeInTheDocument();
    expect(screen.getByText('CHECKING')).toBeInTheDocument();
    expect(screen.getByText('2025-01-10')).toBeInTheDocument();
  });

  it('loads full account profiles by customer ID and renders profile-only counts', async () => {
    const user = userEvent.setup();
    accountApi.getByCustomerId.mockResolvedValue([
      {
        name: 'Family account',
        category: 'SAVING',
        dateOpened: '2025-03-01',
        active: false,
        customers: [{ firstName: 'Nino', lastName: 'Tester', email: 'nino@test.com' }],
        cards: [{ panMasked: '**** 4242' }, { panMasked: '**** 1111' }],
        transactions: [{ transactionType: 'DEPOSIT' }],
      },
    ]);

    renderWithProviders(<AccountsPage />);

    const section = within(getSectionByHeading('Find accounts'));
    await user.type(section.getByLabelText(/customer id/i), '42');
    await user.click(section.getByRole('button', { name: 'Load profiles' }));

    await waitFor(() => expect(accountApi.getByCustomerId).toHaveBeenCalledWith('42'));
    expect(await screen.findByText('Family account')).toBeInTheDocument();
    expect(screen.getAllByRole('cell', { name: '1' })).toHaveLength(2);
    expect(screen.getByRole('cell', { name: '2' })).toBeInTheDocument();
  });

  it('loads account detail by ID and renders linked customers, cards, and transactions', async () => {
    const user = userEvent.setup();
    accountApi.getById.mockResolvedValue({
      name: 'Travel account',
      category: 'CHECKING',
      dateOpened: '2025-05-15',
      active: true,
      customers: [{ firstName: 'Giorgi', lastName: 'Tester', email: 'giorgi@test.com' }],
      cards: [
        {
          panMasked: '**** 5555',
          type: 'DEBIT',
          brand: 'VISA',
          spendingLimit: '1000',
          expirationDate: '2029-01-01',
          active: true,
        },
      ],
      transactions: [
        {
          transactionType: 'WITHDRAW',
          amount: '25.50',
          currencyCode: 'GEL',
          status: 'SUCCESS',
          timeStamp: '2026-01-01T12:00:00',
          description: 'ATM withdrawal',
        },
      ],
    });

    renderWithProviders(<AccountsPage />);

    const section = within(getSectionByHeading('Find accounts'));
    await user.type(section.getByLabelText(/account id/i), '10');
    await user.click(section.getByRole('button', { name: 'Load detail' }));

    await waitFor(() => expect(accountApi.getById).toHaveBeenCalledWith('10'));
    expect(await screen.findByText('Travel account')).toBeInTheDocument();
    expect(screen.getByText('giorgi@test.com')).toBeInTheDocument();
    expect(screen.getByText('**** 5555')).toBeInTheDocument();
    expect(screen.getByText('25.50 GEL')).toBeInTheDocument();
    expect(screen.getByText('ATM withdrawal')).toBeInTheDocument();
  });

  it('creates an account', async () => {
    const user = userEvent.setup();
    accountApi.create.mockResolvedValue('ok');

    renderWithProviders(<AccountsPage />);

    const section = within(getSectionByHeading('Create account'));
    await user.type(section.getByLabelText(/^account name/i), 'Daily');
    await user.selectOptions(section.getByLabelText(/category/i), 'CHECKING');
    await user.click(section.getByRole('button', { name: 'Create account' }));

    await waitFor(() =>
      expect(accountApi.create).toHaveBeenCalledWith({
        accountName: 'Daily',
        category: 'CHECKING',
      })
    );
  });

  it('updates account name', async () => {
    const user = userEvent.setup();
    accountApi.updateName.mockResolvedValue('ok');

    renderWithProviders(<AccountsPage />);

    const section = within(getSectionByHeading('Update account name'));
    await user.type(section.getByLabelText(/account id/i), '10');
    await user.type(section.getByLabelText(/new account name/i), 'Travel');
    await user.click(section.getByRole('button', { name: 'Save name' }));

    await waitFor(() => expect(accountApi.updateName).toHaveBeenCalledWith('10', 'Travel'));
  });

  it('activates and deactivates an account', async () => {
    const user = userEvent.setup();
    accountApi.activate.mockResolvedValue('ok');
    accountApi.deactivate.mockResolvedValue('ok');

    renderWithProviders(<AccountsPage />);

    const section = within(getSectionByHeading('Account status'));
    await user.type(section.getByLabelText(/account id/i), '10');
    await user.click(section.getByRole('button', { name: 'Activate' }));
    await waitFor(() => expect(accountApi.activate).toHaveBeenCalledWith('10'));

    await user.click(section.getByRole('button', { name: 'Deactivate' }));
    await waitFor(() => expect(accountApi.deactivate).toHaveBeenCalledWith('10'));
  });

  it('registers an existing customer to an account', async () => {
    const user = userEvent.setup();
    accountApi.registerCustomer.mockResolvedValue('ok');

    renderWithProviders(<AccountsPage />);

    const section = within(getSectionByHeading('Register customer'));
    await user.type(section.getByLabelText(/account id/i), '10');
    await user.type(section.getByLabelText(/customer id/i), '42');
    await user.click(section.getByRole('button', { name: 'Register customer' }));

    await waitFor(() => expect(accountApi.registerCustomer).toHaveBeenCalledWith('10', '42'));
  });

  it('loads converted balance by currency', async () => {
    const user = userEvent.setup();
    accountApi.getBalanceByCurrency.mockResolvedValue('125.30');

    renderWithProviders(<AccountsPage />);

    const section = within(getSectionByHeading('Balance by currency'));
    await user.type(section.getByLabelText(/account id/i), '10');
    await user.clear(section.getByLabelText(/currency code/i));
    await user.type(section.getByLabelText(/currency code/i), 'usd');
    await user.click(section.getByRole('button', { name: 'View balance' }));

    await waitFor(() => expect(accountApi.getBalanceByCurrency).toHaveBeenCalledWith('10', 'USD'));
    expect(await screen.findByText('125.30 USD')).toBeInTheDocument();
  });

  it('creates a card for an account', async () => {
    const user = userEvent.setup();
    accountApi.createCard.mockResolvedValue('ok');

    renderWithProviders(<AccountsPage />);

    const section = within(getSectionByHeading('Create card'));
    await user.type(section.getByLabelText(/account id/i), '10');
    await user.type(section.getByLabelText(/spending limit/i), '1000');
    await user.selectOptions(section.getByLabelText(/card type/i), 'DEBIT');
    await user.selectOptions(section.getByLabelText(/card brand/i), 'VISA');
    await user.type(section.getByLabelText(/pan/i), '123456789012');
    await user.click(section.getByRole('button', { name: 'Create card' }));

    await waitFor(() =>
      expect(accountApi.createCard).toHaveBeenCalledWith('10', {
        cardType: 'DEBIT',
        cardBrand: 'VISA',
        spendingLimit: '1000',
        pan: '123456789012',
      })
    );
  });

  it('hides account deletion from standard users', () => {
    renderWithProviders(<AccountsPage />);

    expect(screen.queryByRole('heading', { name: 'Delete account' })).not.toBeInTheDocument();
  });

  it('confirms manager account deletion before calling the API', async () => {
    const user = userEvent.setup();
    mockAuth.authority = 'MANAGER';
    accountApi.delete.mockResolvedValue('ok');

    renderWithProviders(<AccountsPage />);

    const section = within(getSectionByHeading('Delete account'));
    await user.type(section.getByLabelText(/account id/i), '10');
    await user.click(section.getByRole('button', { name: 'Delete account' }));

    expect(accountApi.delete).not.toHaveBeenCalled();
    const dialog = screen.getByRole('dialog', { name: 'Delete account' });
    expect(dialog).toBeInTheDocument();

    await user.click(within(dialog).getByRole('button', { name: 'Delete account' }));

    await waitFor(() => expect(accountApi.delete).toHaveBeenCalledWith('10'));
  });
});
