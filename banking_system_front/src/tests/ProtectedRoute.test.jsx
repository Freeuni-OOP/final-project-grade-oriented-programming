import { render, screen } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import ProtectedRoute from '../../components/ProtectedRoute';

const mockAuth = {
  isAuthenticated: false,
  authority: null,
  ready: true,
};

vi.mock('../components/AuthContext', () => ({
  useAuth: () => mockAuth,
}));

function renderRoute({ isAuthenticated, authority, requiredRole } = {}) {
  mockAuth.isAuthenticated = isAuthenticated ?? false;
  mockAuth.authority = authority ?? null;
  mockAuth.ready = true;

  return render(
    <MemoryRouter initialEntries={['/protected']}>
      <Routes>
        <Route element={<ProtectedRoute requiredRole={requiredRole} />}>
          <Route path="/protected" element={<div>Protected content</div>} />
        </Route>
        <Route path="/login" element={<div>Login page</div>} />
        <Route path="/unauthorized" element={<div>Unauthorized page</div>} />
      </Routes>
    </MemoryRouter>
  );
}

describe('ProtectedRoute', () => {
  beforeEach(() => {
    mockAuth.isAuthenticated = false;
    mockAuth.authority = null;
    mockAuth.ready = true;
  });

  it('redirects to /login when user is not authenticated', () => {
    renderRoute({ isAuthenticated: false });
    expect(screen.getByText('Login page')).toBeInTheDocument();
    expect(screen.queryByText('Protected content')).not.toBeInTheDocument();
  });

  it('renders the protected page when user is authenticated', () => {
    renderRoute({ isAuthenticated: true, authority: 'STANDARD' });
    expect(screen.getByText('Protected content')).toBeInTheDocument();
  });

  it('renders nothing while auth is not yet ready (prevents flash redirect)', () => {
    mockAuth.ready = false;
    mockAuth.isAuthenticated = false;

    render(
      <MemoryRouter initialEntries={['/protected']}>
        <Routes>
          <Route element={<ProtectedRoute />}>
            <Route path="/protected" element={<div>Protected content</div>} />
          </Route>
          <Route path="/login" element={<div>Login page</div>} />
        </Routes>
      </MemoryRouter>
    );

    expect(screen.queryByText('Protected content')).not.toBeInTheDocument();
    expect(screen.queryByText('Login page')).not.toBeInTheDocument();
  });

  describe('role guard via requiredRole prop', () => {
    it('allows a MANAGER to access a MANAGER-only route', () => {
      renderRoute({ isAuthenticated: true, authority: 'MANAGER', requiredRole: 'MANAGER' });
      expect(screen.getByText('Protected content')).toBeInTheDocument();
    });

    it('redirects a STANDARD user away from a MANAGER-only route', () => {
      renderRoute({ isAuthenticated: true, authority: 'STANDARD', requiredRole: 'MANAGER' });
      expect(screen.getByText('Unauthorized page')).toBeInTheDocument();
      expect(screen.queryByText('Protected content')).not.toBeInTheDocument();
    });

    it('allows a STANDARD user on a route with no requiredRole', () => {
      renderRoute({ isAuthenticated: true, authority: 'STANDARD' });
      expect(screen.getByText('Protected content')).toBeInTheDocument();
    });
  });
});
