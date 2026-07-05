import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import LoginPage from '../pages/LoginPage';
import RegisterPage from '../pages/RegisterPage';

const mockAuth = vi.hoisted(() => ({
  login: vi.fn(),
}));

vi.mock('../components/AuthContext', () => ({
  useAuth: () => mockAuth,
}));

vi.mock('../api/authApi', () => ({
  authApi: {
    login: vi.fn(),
    register: vi.fn(),
  },
}));

import { authApi } from '../api/authApi';

function renderAuthPage(page) {
  return render(<MemoryRouter>{page}</MemoryRouter>);
}

describe('auth pages backend errors', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('shows the shared login error and stops submitting', async () => {
    const user = userEvent.setup();
    authApi.login.mockRejectedValue({ response: { status: 401 } });

    renderAuthPage(<LoginPage />);

    await user.type(screen.getByLabelText(/email/i), 'bad@test.com');
    await user.type(screen.getByLabelText(/password/i), 'wrong-password');
    await user.click(screen.getByRole('button', { name: 'Sign in' }));

    expect(await screen.findByText('Invalid email or password.')).toBeInTheDocument();
    await waitFor(() => expect(screen.getByRole('button', { name: 'Sign in' })).not.toBeDisabled());
  });

  it('shows the shared register conflict error and stops submitting', async () => {
    const user = userEvent.setup();
    authApi.register.mockRejectedValue({ response: { status: 409 } });

    renderAuthPage(<RegisterPage />);

    await user.type(screen.getByLabelText(/first name/i), 'Nino');
    await user.type(screen.getByLabelText(/last name/i), 'Test');
    await user.type(screen.getByLabelText(/email/i), 'nino@test.com');
    await user.type(screen.getByLabelText(/password/i), 'secret1');
    await user.type(screen.getByLabelText(/date of birth/i), '2000-01-01');
    await user.click(screen.getByRole('button', { name: 'Create account' }));

    expect(
      await screen.findByText('This request conflicts with existing data.')
    ).toBeInTheDocument();
    await waitFor(() =>
      expect(screen.getByRole('button', { name: 'Create account' })).not.toBeDisabled()
    );
  });

  it('shows backend field errors next to register inputs', async () => {
    const user = userEvent.setup();
    authApi.register.mockRejectedValue({
      response: {
        status: 400,
        data: { email: 'Email must be valid' },
      },
    });

    renderAuthPage(<RegisterPage />);

    await user.type(screen.getByLabelText(/first name/i), 'Nino');
    await user.type(screen.getByLabelText(/last name/i), 'Test');
    await user.type(screen.getByLabelText(/email/i), 'nino@test.com');
    await user.type(screen.getByLabelText(/password/i), 'secret1');
    await user.type(screen.getByLabelText(/date of birth/i), '2000-01-01');
    await user.click(screen.getByRole('button', { name: 'Create account' }));

    expect(await screen.findByText('Email must be valid')).toBeInTheDocument();
    expect(
      await screen.findByText('Invalid input. Please check the fields and try again.')
    ).toBeInTheDocument();
  });

  it('sends null for blank optional registration fields', async () => {
    const user = userEvent.setup();
    authApi.register.mockResolvedValue('ok');

    renderAuthPage(<RegisterPage />);

    await user.type(screen.getByLabelText(/first name/i), 'Nino');
    await user.type(screen.getByLabelText(/last name/i), 'Test');
    await user.type(screen.getByLabelText(/email/i), 'nino@test.com');
    await user.type(screen.getByLabelText(/password/i), 'secret1');
    await user.type(screen.getByLabelText(/date of birth/i), '2000-01-01');
    await user.click(screen.getByRole('button', { name: 'Create account' }));

    await waitFor(() =>
      expect(authApi.register).toHaveBeenCalledWith(
        expect.objectContaining({
          phoneNumber: null,
          address: null,
        })
      )
    );
  });
});
