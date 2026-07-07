import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { ToastProvider } from '../components/ToastProvider';
import CustomersPage from '../pages/CustomersPage';
import ManagerCustomersPage from '../pages/ManagerCustomersPage';

vi.mock('../api/customerApi', () => ({
  customerApi: {
    getById: vi.fn(),
    getByEmail: vi.fn(),
    getByAccount: vi.fn(),
    update: vi.fn(),
    activate: vi.fn(),
    deactivate: vi.fn(),
    delete: vi.fn(),
  },
}));

import { customerApi } from '../api/customerApi';

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

describe('customer pages', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('loads a customer by ID and updates editable profile fields', async () => {
    const user = userEvent.setup();
    customerApi.getById.mockResolvedValue({
      firstName: 'Nino',
      lastName: 'Tester',
      email: 'nino@test.com',
      phoneNumber: '5551234',
      address: 'Old address',
      dateOfBirth: '2000-01-01',
      accounts: [],
    });
    customerApi.update.mockResolvedValue({
      firstName: 'Nina',
      lastName: 'Tester',
      email: 'nino@test.com',
      phoneNumber: null,
      address: 'Tbilisi',
      dateOfBirth: '2000-01-01',
      accounts: [],
    });

    renderWithProviders(<CustomersPage />);

    await user.type(screen.getByLabelText(/customer id/i), '42');
    await user.click(screen.getByRole('button', { name: 'Search ID' }));

    expect(await screen.findByText('nino@test.com')).toBeInTheDocument();

    await user.clear(screen.getByLabelText(/first name/i));
    await user.type(screen.getByLabelText(/first name/i), 'Nina');
    await user.clear(screen.getByLabelText(/phone number/i));
    await user.clear(screen.getByLabelText(/address/i));
    await user.type(screen.getByLabelText(/address/i), 'Tbilisi');
    await user.click(screen.getByRole('button', { name: 'Save changes' }));

    await waitFor(() =>
      expect(customerApi.update).toHaveBeenCalledWith('42', {
        firstName: 'Nina',
        lastName: 'Tester',
        phoneNumber: null,
        address: 'Tbilisi',
      })
    );
  });

  it('loads manager account customers', async () => {
    const user = userEvent.setup();
    customerApi.getByAccount.mockResolvedValue([
      {
        id: 5,
        firstName: 'Giorgi',
        lastName: 'Manager',
        email: 'giorgi@test.com',
        phoneNumber: '555',
        active: true,
      },
    ]);

    renderWithProviders(<ManagerCustomersPage />);

    await user.type(screen.getByLabelText(/account id/i), '9');
    await user.click(screen.getByRole('button', { name: 'Load customers' }));

    await waitFor(() => expect(customerApi.getByAccount).toHaveBeenCalledWith('9'));
    expect(await screen.findByText('giorgi@test.com')).toBeInTheDocument();
  });

  it('confirms manager deletes before calling the delete endpoint', async () => {
    const user = userEvent.setup();
    customerApi.getByAccount.mockResolvedValue([]);
    customerApi.delete.mockResolvedValue('ok');

    renderWithProviders(<ManagerCustomersPage />);

    await user.type(screen.getByLabelText(/customer id/i), '77');
    await user.click(screen.getByRole('button', { name: 'Delete' }));

    expect(customerApi.delete).not.toHaveBeenCalled();
    expect(screen.getByRole('dialog', { name: 'Delete customer' })).toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: 'Delete customer' }));

    await waitFor(() => expect(customerApi.delete).toHaveBeenCalledWith('77'));
  });
});
