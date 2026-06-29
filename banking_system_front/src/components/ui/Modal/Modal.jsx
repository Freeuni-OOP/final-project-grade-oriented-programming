import { useId } from 'react';
import { cx } from '../utils';
import styles from './Modal.module.css';

export default function Modal({
  open = false,
  title,
  children,
  footer,
  onClose,
  closeLabel = 'Close modal',
  size = 'md',
  className = '',
}) {
  const titleId = useId();

  if (!open) {
    return null;
  }

  const handleOverlayMouseDown = (event) => {
    if (event.target === event.currentTarget) {
      onClose?.();
    }
  };

  return (
    <div className={styles.overlay} onMouseDown={handleOverlayMouseDown}>
      <section
        className={cx(styles.dialog, styles[size], className)}
        role="dialog"
        aria-modal="true"
        aria-labelledby={title ? titleId : undefined}
      >
        <header className={styles.header}>
          {title && (
            <h2 className={styles.title} id={titleId}>
              {title}
            </h2>
          )}
          {onClose && (
            <button
              className={styles.closeButton}
              type="button"
              onClick={onClose}
              aria-label={closeLabel}
            >
              <span aria-hidden="true">&times;</span>
            </button>
          )}
        </header>
        <div className={styles.body}>{children}</div>
        {footer && <footer className={styles.footer}>{footer}</footer>}
      </section>
    </div>
  );
}
