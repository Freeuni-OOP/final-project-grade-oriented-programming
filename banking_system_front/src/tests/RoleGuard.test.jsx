import { render, screen } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import ProtectedRoute from '../components/ProtectedRoute';

const mockAuth = vi.hoisted(() => ({
  isAuthenticated: true,
  authority: 'STANDARD',
  ready: true,
}));

vi.mock('../components/AuthContext', () => ({
  useAuth: () => mockAuth,
}));

function renderWithRole(userAuthority, requiredRole) {
  mockAuth.authority = userAuthority;

  return render(
    <MemoryRouter initialEntries={['/target']}>
      <Routes>
        <Route element={<ProtectedRoute requiredRole={requiredRole} />}>
          <Route path="/target" element={<div>Role-gated content</div>} />
        </Route>
        <Route path="/unauthorized" element={<div>Unauthorized page</div>} />
        <Route path="/login" element={<div>Login page</div>} />
      </Routes>
    </MemoryRouter>
  );
}

describe('Role guard', () => {
  beforeEach(() => {
    mockAuth.isAuthenticated = true;
    mockAuth.ready = true;
  });

  describe('MANAGER-only page', () => {
    it('grants access to a MANAGER', () => {
      renderWithRole('MANAGER', 'MANAGER');
      expect(screen.getByText('Role-gated content')).toBeInTheDocument();
    });

    it('blocks a STANDARD user and redirects to /unauthorized', () => {
      renderWithRole('STANDARD', 'MANAGER');
      expect(screen.getByText('Unauthorized page')).toBeInTheDocument();
      expect(screen.queryByText('Role-gated content')).not.toBeInTheDocument();
    });

    it('blocks a user with no authority (null) and redirects to /unauthorized', () => {
      renderWithRole(null, 'MANAGER');
      expect(screen.getByText('Unauthorized page')).toBeInTheDocument();
    });
  });

  describe('page with no requiredRole', () => {
    it('grants access to a STANDARD user', () => {
      renderWithRole('STANDARD', undefined);
      expect(screen.getByText('Role-gated content')).toBeInTheDocument();
    });

    it('grants access to a MANAGER', () => {
      renderWithRole('MANAGER', undefined);
      expect(screen.getByText('Role-gated content')).toBeInTheDocument();
    });
  });

  it('is case-sensitive: "manager" (lowercase) does not pass a MANAGER check', () => {
    renderWithRole('manager', 'MANAGER');
    expect(screen.getByText('Unauthorized page')).toBeInTheDocument();
  });
});
