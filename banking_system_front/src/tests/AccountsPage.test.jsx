import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { ToastProvider } from '../components/ToastProvider';
import AccountsPage from '../pages/AccountsPage';

vi.mock('../api/accountApi', () => ({
  accountApi: {
    getByEmail: vi.fn(),
    getByCustomerId: vi.fn(),
    getById: vi.fn(),
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

describe('AccountsPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
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

    await user.type(screen.getByLabelText(/customer id/i), '42');
    await user.click(screen.getByRole('button', { name: 'Load profiles' }));

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

    await user.type(screen.getByLabelText(/account id/i), '10');
    await user.click(screen.getByRole('button', { name: 'Load detail' }));

    await waitFor(() => expect(accountApi.getById).toHaveBeenCalledWith('10'));
    expect(await screen.findByText('Travel account')).toBeInTheDocument();
    expect(screen.getByText('giorgi@test.com')).toBeInTheDocument();
    expect(screen.getByText('**** 5555')).toBeInTheDocument();
    expect(screen.getByText('25.50 GEL')).toBeInTheDocument();
    expect(screen.getByText('ATM withdrawal')).toBeInTheDocument();
  });
});
