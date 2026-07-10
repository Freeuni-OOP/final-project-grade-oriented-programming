// routes/navigation.js
export const PUBLIC_NAV_ITEMS = [
  { to: '/login', labelKey: 'common:login', label: 'Login' },
  { to: '/register', labelKey: 'common:register', label: 'Register' },
];

export const PROTECTED_NAV_ITEMS = [
  { to: '/customers', labelKey: 'common:profile', label: 'Profile' },
];

export const MANAGER_NAV_ITEMS = [{ to: '/manager', labelKey: 'common:manager', label: 'Manager' }];
