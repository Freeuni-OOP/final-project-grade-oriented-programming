import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useRef,
  useState,
} from 'react';
import { getBackendErrorMessage } from '../api/errorMessages';
import { subscribeToBackendErrors } from '../api/errorNotifications';
import Toast, { ToastContainer } from './ui/Toast/Toast';

const ToastContext = createContext(null);
const DEFAULT_TOAST_TIMEOUT = 5000;

let nextToastId = 0;

function makeToastId() {
  nextToastId += 1;
  return `toast-${nextToastId}`;
}

function BackendErrorToastBridge() {
  const { showErrorToast } = useToast();

  useEffect(
    () =>
      subscribeToBackendErrors((error) => {
        showErrorToast(getBackendErrorMessage(error));
      }),
    [showErrorToast]
  );

  return null;
}

export function ToastProvider({ children }) {
  const [toasts, setToasts] = useState([]);
  const timersRef = useRef(new Map());

  const dismissToast = useCallback((id) => {
    const timer = timersRef.current.get(id);
    if (timer) {
      window.clearTimeout(timer);
      timersRef.current.delete(id);
    }

    setToasts((current) => current.filter((toast) => toast.id !== id));
  }, []);

  const showToast = useCallback(
    ({ id = makeToastId(), timeout = DEFAULT_TOAST_TIMEOUT, variant = 'info', ...toast }) => {
      const nextToast = { id, variant, ...toast };

      // If the same id comes again, replace it so the user does not get copies.
      setToasts((current) => [...current.filter((item) => item.id !== id), nextToast]);

      if (timeout > 0) {
        const oldTimer = timersRef.current.get(id);
        if (oldTimer) {
          window.clearTimeout(oldTimer);
        }

        const timer = window.setTimeout(() => dismissToast(id), timeout);
        timersRef.current.set(id, timer);
      }

      return id;
    },
    [dismissToast]
  );

  const showErrorToast = useCallback(
    (message, options = {}) => {
      const { title = message, ...rest } = options;

      return showToast({
        title,
        variant: 'danger',
        ...rest,
      });
    },
    [showToast]
  );

  useEffect(
    () => () => {
      timersRef.current.forEach((timer) => window.clearTimeout(timer));
      timersRef.current.clear();
    },
    []
  );

  const value = useMemo(
    () => ({ showToast, showErrorToast, dismissToast }),
    [dismissToast, showErrorToast, showToast]
  );

  return (
    <ToastContext.Provider value={value}>
      <BackendErrorToastBridge />
      {children}
      <ToastContainer>
        {toasts.map(({ id, title, message, variant, action, dismissLabel }) => (
          <Toast
            key={id}
            title={title}
            message={message}
            variant={variant}
            action={action}
            dismissLabel={dismissLabel}
            onDismiss={() => dismissToast(id)}
          />
        ))}
      </ToastContainer>
    </ToastContext.Provider>
  );
}

// eslint-disable-next-line react-refresh/only-export-components
export function useToast() {
  const context = useContext(ToastContext);
  if (!context) throw new Error('useToast must be used inside <ToastProvider>');
  return context;
}
