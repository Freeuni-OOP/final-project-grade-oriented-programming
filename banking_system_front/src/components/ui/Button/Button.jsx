import Spinner from '../Spinner/Spinner';
import { cx } from '../utils';
import styles from './Button.module.css';

export default function Button({
  as: Component = 'button',
  type = 'button',
  variant = 'primary',
  size = 'md',
  fullWidth = false,
  isLoading = false,
  disabled = false,
  leftIcon,
  rightIcon,
  children,
  className = '',
  ...props
}) {
  const isNativeButton = Component === 'button';
  const isDisabled = disabled || isLoading;
  const spinnerTone = variant === 'primary' || variant === 'danger' ? 'inverse' : 'default';

  return (
    <Component
      className={cx(
        styles.button,
        styles[variant],
        styles[size],
        fullWidth && styles.fullWidth,
        className
      )}
      type={isNativeButton ? type : undefined}
      disabled={isNativeButton ? isDisabled : undefined}
      aria-disabled={!isNativeButton && isDisabled ? 'true' : undefined}
      aria-busy={isLoading ? 'true' : undefined}
      {...props}
    >
      {isLoading && <Spinner size="sm" tone={spinnerTone} />}
      {leftIcon && <span className={styles.icon}>{leftIcon}</span>}
      <span className={styles.label}>{children}</span>
      {rightIcon && <span className={styles.icon}>{rightIcon}</span>}
    </Component>
  );
}
