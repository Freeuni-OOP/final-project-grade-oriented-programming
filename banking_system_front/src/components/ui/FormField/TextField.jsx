import { forwardRef, useId } from 'react';
import FormField from './FormField';
import { cx } from '../utils';
import styles from './FormField.module.css';

const TextField = forwardRef(function TextField(
  {
    id,
    label,
    hint,
    error,
    required = false,
    multiline = false,
    rows = 4,
    className = '',
    inputClassName = '',
    ...props
  },
  ref
) {
  const generatedId = useId();
  const fieldId = id ?? props.name ?? generatedId;
  const describedBy = [hint && !error ? `${fieldId}-hint` : null, error ? `${fieldId}-error` : null]
    .filter(Boolean)
    .join(' ');
  const Control = multiline ? 'textarea' : 'input';

  return (
    <FormField
      id={fieldId}
      label={label}
      hint={hint}
      error={error}
      required={required}
      className={className}
    >
      <Control
        id={fieldId}
        ref={ref}
        rows={multiline ? rows : undefined}
        className={cx(styles.control, error && styles.invalid, inputClassName)}
        aria-invalid={error ? 'true' : undefined}
        aria-describedby={describedBy || undefined}
        {...props}
      />
    </FormField>
  );
});

export default TextField;
