import { cx } from '../utils';
import styles from './Spinner.module.css';

export default function Spinner({ size = 'md', tone = 'default', label, className = '' }) {
  const spinner = (
    <span
      className={cx(styles.spinner, styles[size], styles[tone], className)}
      aria-hidden={label ? undefined : 'true'}
    />
  );

  if (!label) {
    return spinner;
  }

  return (
    <span className={styles.withLabel} role="status" aria-live="polite">
      {spinner}
      <span className={styles.label}>{label}</span>
    </span>
  );
}
