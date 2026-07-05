import { describe, expect, it } from 'vitest';
import {
  DEFAULT_BACKEND_ERROR_MESSAGE,
  NETWORK_ERROR_MESSAGE,
  getBackendErrorMessage,
  getBackendStatusCode,
} from '../api/errorMessages';

describe('backend error messages', () => {
  it('maps backend status codes to user messages', () => {
    expect(getBackendErrorMessage({ response: { status: 400 } })).toBe(
      'Invalid input. Please check the fields and try again.'
    );
    expect(getBackendErrorMessage({ response: { status: 401 } })).toBe(
      'Invalid email or password.'
    );
    expect(getBackendErrorMessage({ response: { status: 403 } })).toBe(
      'You are not authorized to do this action.'
    );
    expect(getBackendErrorMessage({ response: { status: 404 } })).toBe(
      'The requested resource was not found.'
    );
    expect(getBackendErrorMessage({ response: { status: 406 } })).toBe(
      'This action is not allowed in the current state.'
    );
    expect(getBackendErrorMessage({ response: { status: 409 } })).toBe(
      'This request conflicts with existing data.'
    );
  });

  it('uses a fallback message for unknown errors', () => {
    expect(getBackendErrorMessage({ response: { status: 500 } })).toBe(
      DEFAULT_BACKEND_ERROR_MESSAGE
    );
  });

  it('uses a clear message for network errors', () => {
    expect(getBackendErrorMessage({ code: 'ERR_NETWORK' })).toBe(NETWORK_ERROR_MESSAGE);
  });

  it('reads status from axios-style and plain errors', () => {
    expect(getBackendStatusCode({ response: { status: 404 } })).toBe(404);
    expect(getBackendStatusCode({ status: 409 })).toBe(409);
  });
});
