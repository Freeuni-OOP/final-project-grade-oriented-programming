import { render, screen, within } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { createMemoryRouter, RouterProvider } from 'react-router-dom';
import { appRoutes } from '../routes';

const mockAuth = vi.hoisted(() => ({
  isAuthenticated: false,
  authority: null,
  ready: true,
  login: vi.fn(),
  logout: vi.fn(),
}));

vi.mock('../components/AuthContext', () => ({
  useAuth: () => mockAuth,
}));

function renderRoute(initialPath) {
  const router = createMemoryRouter(appRoutes, {
    initialEntries: [initialPath],
  });

  return render(<RouterProvider router={router} />);
}

describe('App route table', () => {
  beforeEach(() => {
    mockAuth.isAuthenticated = false;
    mockAuth.authority = null;
    mockAuth.ready = true;
    mockAuth.login.mockClear();
    mockAuth.logout.mockClear();
  });

  it('redirects an unauthenticated user from a protected route to login', async () => {
    renderRoute('/dashboard');

    expect(await screen.findByRole('heading', { name: 'Sign in' })).toBeInTheDocument();
    expect(screen.queryByRole('heading', { name: 'Dashboard' })).not.toBeInTheDocument();
  });

  it('hides manager-only navigation links from a standard user', async () => {
    mockAuth.isAuthenticated = true;
    mockAuth.authority = 'STANDARD';

    renderRoute('/dashboard');

    expect(await screen.findByRole('heading', { name: 'Dashboard' })).toBeInTheDocument();

    const nav = screen.getByRole('navigation', { name: 'Main navigation' });
    expect(within(nav).getByRole('link', { name: 'Customers' })).toBeInTheDocument();
    expect(within(nav).queryByRole('link', { name: 'Admin' })).not.toBeInTheDocument();
    expect(within(nav).queryByRole('link', { name: 'Delete Records' })).not.toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Logout' })).toBeInTheDocument();
  });
});
