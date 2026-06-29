import { cx } from '../utils';
import styles from './Toast.module.css';

export function ToastContainer({ children, position = 'topRight', className = '' }) {
  return <div className={cx(styles.container, styles[position], className)}>{children}</div>;
}

export default function Toast({
  title,
  message,
  variant = 'info',
  action,
  onDismiss,
  dismissLabel = 'Dismiss notification',
  className = '',
}) {
  return (
    <div
      className={cx(styles.toast, styles[variant], className)}
      role={variant === 'danger' ? 'alert' : 'status'}
    >
      <span className={styles.statusDot} aria-hidden="true" />
      <div className={styles.content}>
        {title && <p className={styles.title}>{title}</p>}
        {message && <p className={styles.message}>{message}</p>}
        {action && <div className={styles.action}>{action}</div>}
      </div>
      {onDismiss && (
        <button
          className={styles.dismiss}
          type="button"
          onClick={onDismiss}
          aria-label={dismissLabel}
        >
          <span aria-hidden="true">&times;</span>
        </button>
      )}
    </div>
  );
}
