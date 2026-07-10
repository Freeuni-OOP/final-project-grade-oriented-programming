// src/components/Layout.jsx
import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useAuth } from './AuthContext';
import { Button } from './ui';
import { MANAGER_NAV_ITEMS, PROTECTED_NAV_ITEMS, PUBLIC_NAV_ITEMS } from '../routes/navigation';
import styles from './Layout.module.css';

export default function Layout() {
  const { authority, isAuthenticated, logout } = useAuth();
  const navigate = useNavigate();
  const { t, i18n } = useTranslation();
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
        <NavLink to={isAuthenticated ? '/customers' : '/login'} className={styles.brand}>
          {t('common:brand')}
        </NavLink>

        <nav aria-label={t('common:main_navigation')} className={styles.nav}>
          {navItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              className={({ isActive }) =>
                isActive ? `${styles.link} ${styles.activeLink}` : styles.link
              }
            >
              {t(item.labelKey ?? item.label)}
            </NavLink>
          ))}
        </nav>

        <div className={styles.actions}>
          <select
            value={i18n.language}
            onChange={(e) => i18n.changeLanguage(e.target.value)}
            className={styles.languageSwitcher}
            aria-label={t('common:choose_language')}
          >
            <option value="en">{t('common:language_english')}</option>
            <option value="ka">{t('common:language_georgian')}</option>
            <option value="zh">{t('common:language_chinese')}</option>
          </select>

          {isAuthenticated && (
            <>
              <span className={styles.role}>
                {t(`common:role_${String(authority ?? 'USER').toLowerCase()}`) ||
                  authority ||
                  t('common:role_user')}
              </span>
              <Button type="button" variant="secondary" size="sm" onClick={handleLogout}>
                {t('common:logout')}
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
