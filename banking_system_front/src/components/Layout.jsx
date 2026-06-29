import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from './AuthContext';
import { Button } from './ui';
import { MANAGER_NAV_ITEMS, PROTECTED_NAV_ITEMS, PUBLIC_NAV_ITEMS } from '../routes/navigation';
import styles from './Layout.module.css';

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
    <div className={styles.shell}>
      <header className={styles.header}>
        <NavLink to={isAuthenticated ? '/dashboard' : '/login'} className={styles.brand}>
          Banking System
        </NavLink>

        <nav aria-label="Main navigation" className={styles.nav}>
          {navItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              className={({ isActive }) =>
                isActive ? `${styles.link} ${styles.activeLink}` : styles.link
              }
            >
              {item.label}
            </NavLink>
          ))}
        </nav>

        <div className={styles.actions}>
          {isAuthenticated && (
            <>
              <span className={styles.role}>{authority ?? 'USER'}</span>
              <Button type="button" variant="secondary" size="sm" onClick={handleLogout}>
                Logout
              </Button>
            </>
          )}
        </div>
      </header>

      <main className={styles.content}>
        <Outlet />
      </main>
    </div>
  );
}
