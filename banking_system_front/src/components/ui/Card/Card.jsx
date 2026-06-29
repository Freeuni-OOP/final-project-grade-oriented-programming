import { cx } from '../utils';
import styles from './Card.module.css';

export default function Card({
  as: Component = 'section',
  title,
  subtitle,
  actions,
  footer,
  children,
  padding = 'md',
  className = '',
  bodyClassName = '',
  ...props
}) {
  const hasHeader = title || subtitle || actions;

  return (
    <Component className={cx(styles.card, styles[padding], className)} {...props}>
      {hasHeader && (
        <header className={styles.header}>
          <div className={styles.headingGroup}>
            {title && <h2 className={styles.title}>{title}</h2>}
            {subtitle && <p className={styles.subtitle}>{subtitle}</p>}
          </div>
          {actions && <div className={styles.actions}>{actions}</div>}
        </header>
      )}
      <div className={cx(styles.body, bodyClassName)}>{children}</div>
      {footer && <footer className={styles.footer}>{footer}</footer>}
    </Component>
  );
}
