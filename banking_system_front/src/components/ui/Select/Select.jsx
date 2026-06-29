import { forwardRef, useId } from 'react';
import FormField from '../FormField/FormField';
import { cx } from '../utils';
import styles from './Select.module.css';

const Select = forwardRef(function Select(
  {
    id,
    label,
    hint,
    error,
    required = false,
    placeholder,
    options = [],
    children,
    className = '',
    selectClassName = '',
    ...props
  },
  ref
) {
  const generatedId = useId();
  const fieldId = id ?? props.name ?? generatedId;
  const describedBy = [hint && !error ? `${fieldId}-hint` : null, error ? `${fieldId}-error` : null]
    .filter(Boolean)
    .join(' ');

  return (
    <FormField
      id={fieldId}
      label={label}
      hint={hint}
      error={error}
      required={required}
      className={className}
    >
      <span className={styles.selectWrap}>
        <select
          id={fieldId}
          ref={ref}
          className={cx(styles.select, error && styles.invalid, selectClassName)}
          aria-invalid={error ? 'true' : undefined}
          aria-describedby={describedBy || undefined}
          {...props}
        >
          {placeholder && <option value="">{placeholder}</option>}
          {children ??
            options.map((option) => (
              <option key={String(option.value)} value={option.value} disabled={option.disabled}>
                {option.label}
              </option>
            ))}
        </select>
      </span>
    </FormField>
  );
});

export default Select;
