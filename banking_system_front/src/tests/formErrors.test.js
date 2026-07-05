import { describe, expect, it, vi } from 'vitest';
import { applyBackendFormErrors, getBackendFieldErrors } from '../api/formErrors';

const FIELDS = ['email', 'password', 'phoneNumber'];

describe('backend form errors', () => {
  it('reads simple field maps', () => {
    const errors = getBackendFieldErrors(
      {
        response: {
          data: {
            email: 'Email must be valid',
            status: 'Bad Request',
          },
        },
      },
      FIELDS
    );

    expect(errors).toEqual({ email: 'Email must be valid' });
  });

  it('reads spring-style error arrays', () => {
    const errors = getBackendFieldErrors(
      {
        response: {
          data: {
            errors: [{ field: 'password', defaultMessage: 'Password is too short' }],
          },
        },
      },
      FIELDS
    );

    expect(errors).toEqual({ password: 'Password is too short' });
  });

  it('applies field errors and the global form error', () => {
    const setError = vi.fn();

    const hasFields = applyBackendFormErrors(
      {
        response: {
          status: 400,
          data: { phoneNumber: 'Phone number must contain only digits.' },
        },
      },
      setError,
      FIELDS
    );

    expect(hasFields).toBe(true);
    expect(setError).toHaveBeenCalledWith('phoneNumber', {
      type: 'server',
      message: 'Phone number must contain only digits.',
    });
    expect(setError).toHaveBeenCalledWith('root', {
      type: 'server',
      message: 'Invalid input. Please check the fields and try again.',
    });
  });
});
