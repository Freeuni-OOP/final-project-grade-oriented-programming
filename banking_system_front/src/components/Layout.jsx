import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from './AuthContext';
import { MANAGER_NAV_ITEMS, PROTECTED_NAV_ITEMS, PUBLIC_NAV_ITEMS } from '../routes/navigation';

export default function Layout() {
  const { authority, isAuthenticated, logout } = useAuth();
  const navigate = useNavigate();
  const isManager = authority === 'MANAGER';

  const handleLogout = () => {
    logout();
    navigate('/login', { replace: true });
  };

  const navItems = isAuthenticated
    ? [...PROTECTED_NAV_ITEMS, ...(isManager ? MANAGER_NAV_ITEMS : [])]
    : PUBLIC_NAV_ITEMS;

  return (
    <div style={styles.shell}>
      <header style={styles.header}>
        <NavLink to={isAuthenticated ? '/dashboard' : '/login'} style={styles.brand}>
          Banking System
        </NavLink>

        <nav aria-label="Main navigation" style={styles.nav}>
          {navItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              style={({ isActive }) => ({
                ...styles.link,
                ...(isActive ? styles.activeLink : {}),
              })}
            >
              {item.label}
            </NavLink>
          ))}
        </nav>

        <div style={styles.actions}>
          {isAuthenticated && (
            <>
              <span style={styles.role}>{authority ?? 'USER'}</span>
              <button type="button" onClick={handleLogout} style={styles.logout}>
                Logout
              </button>
            </>
          )}
        </div>
      </header>

      <main style={styles.content}>
        <Outlet />
      </main>
    </div>
  );
}

const styles = {
  shell: {
    minHeight: '100vh',
    background: '#f7f8fb',
    color: '#172033',
  },
  header: {
    display: 'flex',
    alignItems: 'center',
    flexWrap: 'wrap',
    gap: 18,
    minHeight: 64,
    padding: '12px 32px',
    borderBottom: '1px solid #dfe3eb',
    background: '#ffffff',
    boxSizing: 'border-box',
  },
  brand: {
    color: '#172033',
    fontSize: 18,
    fontWeight: 700,
    textDecoration: 'none',
    whiteSpace: 'nowrap',
  },
  nav: {
    display: 'flex',
    alignItems: 'center',
    flexWrap: 'wrap',
    gap: 8,
    flex: 1,
  },
  link: {
    color: '#4b5567',
    textDecoration: 'none',
    fontSize: 14,
    fontWeight: 600,
    padding: '8px 10px',
    borderRadius: 6,
  },
  activeLink: {
    color: '#0f5132',
    background: '#dff3e9',
  },
  actions: {
    display: 'flex',
    alignItems: 'center',
    gap: 10,
    minHeight: 36,
  },
  role: {
    color: '#5f6b7a',
    fontSize: 12,
    fontWeight: 700,
  },
  logout: {
    padding: '8px 12px',
    border: '1px solid #c7ced9',
    borderRadius: 6,
    background: '#ffffff',
    color: '#172033',
    fontSize: 14,
    fontWeight: 600,
    cursor: 'pointer',
  },
  content: {
    width: 'min(100%, 1120px)',
    margin: '0 auto',
    padding: 32,
    boxSizing: 'border-box',
    textAlign: 'left',
  },
};
