import { render, screen, waitFor, act } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { AuthProvider, useAuth } from '../components/AuthContext';

/**
 * Build a minimal, unsigned JWT whose payload is `claims`.
 * Real signature validation doesn't run in the frontend; we just decode the
 * base64 payload, so a made-up signature is fine for tests.
 */
function makeToken(claims) {
  const header = btoa(JSON.stringify({ alg: 'HS256', typ: 'JWT' }));
  const payload = btoa(JSON.stringify(claims));
  return `${header}.${payload}.fakesig`;
}

const STANDARD_TOKEN = makeToken({
  authorities: [{ authority: 'STANDARD' }],
  sub: 'user@test.com',
});
const MANAGER_TOKEN = makeToken({ authorities: [{ authority: 'MANAGER' }], sub: 'mgr@test.com' });

vi.mock('../api/authApi', () => ({
  authApi: {
    validate: vi.fn(),
  },
}));

import { authApi } from '../api/authApi';

function Consumer() {
  const { token, authority, isAuthenticated, login, logout } = useAuth();
  return (
    <div>
      <span data-testid="authenticated">{String(isAuthenticated)}</span>
      <span data-testid="authority">{authority ?? 'none'}</span>
      <span data-testid="token">{token ?? 'none'}</span>
      <button onClick={() => login(STANDARD_TOKEN)}>login-standard</button>
      <button onClick={() => login(MANAGER_TOKEN)}>login-manager</button>
      <button onClick={logout}>logout</button>
    </div>
  );
}

function renderWithAuth() {
  return render(
    <AuthProvider>
      <Consumer />
    </AuthProvider>
  );
}

describe('AuthContext', () => {
  beforeEach(() => {
    localStorage.clear();
    vi.clearAllMocks();
  });

  afterEach(() => {
    localStorage.clear();
  });

  it('starts unauthenticated when localStorage is empty', async () => {
    authApi.validate.mockResolvedValue({});
    renderWithAuth();

    await waitFor(() => {
      expect(screen.getByTestId('authenticated').textContent).toBe('false');
    });
    expect(screen.getByTestId('authority').textContent).toBe('none');
  });

  it('rehydrates from localStorage when token is valid', async () => {
    localStorage.setItem('token', STANDARD_TOKEN);
    authApi.validate.mockResolvedValue({});

    renderWithAuth();

    await waitFor(() => {
      expect(screen.getByTestId('authenticated').textContent).toBe('true');
    });
    expect(screen.getByTestId('authority').textContent).toBe('STANDARD');
  });

  it('clears state when stored token fails validation', async () => {
    localStorage.setItem('token', STANDARD_TOKEN);
    authApi.validate.mockRejectedValue({ response: { status: 401 } });

    renderWithAuth();

    await waitFor(() => {
      expect(screen.getByTestId('authenticated').textContent).toBe('false');
    });
    expect(localStorage.getItem('token')).toBeNull();
  });

  it('sets isAuthenticated and authority on login (STANDARD)', async () => {
    authApi.validate.mockResolvedValue({});
    renderWithAuth();
    await waitFor(() => screen.getByTestId('authenticated'));

    act(() => screen.getByText('login-standard').click());

    expect(screen.getByTestId('authenticated').textContent).toBe('true');
    expect(screen.getByTestId('authority').textContent).toBe('STANDARD');
    expect(localStorage.getItem('token')).toBe(STANDARD_TOKEN);
  });

  it('sets authority to MANAGER when MANAGER token is used', async () => {
    authApi.validate.mockResolvedValue({});
    renderWithAuth();
    await waitFor(() => screen.getByTestId('authenticated'));

    act(() => screen.getByText('login-manager').click());

    expect(screen.getByTestId('authority').textContent).toBe('MANAGER');
  });

  it('clears auth state and localStorage on logout', async () => {
    localStorage.setItem('token', STANDARD_TOKEN);
    authApi.validate.mockResolvedValue({});
    renderWithAuth();
    await waitFor(() => expect(screen.getByTestId('authenticated').textContent).toBe('true'));

    act(() => screen.getByText('logout').click());

    expect(screen.getByTestId('authenticated').textContent).toBe('false');
    expect(screen.getByTestId('authority').textContent).toBe('none');
    expect(localStorage.getItem('token')).toBeNull();
  });

  it('isAuthenticated is false before login and true after', async () => {
    authApi.validate.mockResolvedValue({});
    renderWithAuth();
    await waitFor(() => expect(screen.getByTestId('authenticated').textContent).toBe('false'));

    act(() => screen.getByText('login-standard').click());

    expect(screen.getByTestId('authenticated').textContent).toBe('true');
  });
});
