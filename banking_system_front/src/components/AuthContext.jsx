import { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { authApi } from '../api/authApi';
import { TOKEN_KEY } from '../api/config';

function decodeAuthority(token) {
  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    const claims = payload;
    const raw = claims.authorities ?? claims.roles ?? claims.role ?? [];
    if (Array.isArray(raw)) {
      const first = raw[0];
      return typeof first === 'object' ? first.authority : first;
    }
    return typeof raw === 'string' ? raw : null;
  } catch {
    return null;
  }
}

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [token, setToken] = useState(null);
  const [authority, setAuthority] = useState(null);
  const [ready, setReady] = useState(false);

  useEffect(() => {
    const stored = localStorage.getItem(TOKEN_KEY);
    if (!stored) {
      Promise.resolve().then(() => setReady(true));
      return;
    }

    authApi
      .validate()
      .then(() => {
        const auth = decodeAuthority(stored);
        setToken(stored);
        setAuthority(auth);
      })
      .catch(() => {
        localStorage.removeItem(TOKEN_KEY);
      })
      .finally(() => setReady(true));
  }, []);

  const login = useCallback((newToken) => {
    localStorage.setItem(TOKEN_KEY, newToken);
    const auth = decodeAuthority(newToken);
    setToken(newToken);
    setAuthority(auth);
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem(TOKEN_KEY);
    setToken(null);
    setAuthority(null);
  }, []);

  const isAuthenticated = Boolean(token);

  return (
    <AuthContext.Provider value={{ token, authority, login, logout, isAuthenticated, ready }}>
      {children}
    </AuthContext.Provider>
  );
}

// eslint-disable-next-line react-refresh/only-export-components
export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used inside <AuthProvider>');
  return ctx;
}
