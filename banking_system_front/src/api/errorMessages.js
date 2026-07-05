export const BACKEND_ERROR_MESSAGES = {
  400: 'Invalid input. Please check the fields and try again.',
  401: 'Invalid email or password.',
  403: 'You are not authorized to do this action.',
  404: 'The requested resource was not found.',
  406: 'This action is not allowed in the current state.',
  409: 'This request conflicts with existing data.',
};

export const DEFAULT_BACKEND_ERROR_MESSAGE = 'Something went wrong. Please try again.';
export const NETWORK_ERROR_MESSAGE =
  'Cannot reach the backend. Please check that the server is running.';

export function getBackendStatusCode(error) {
  return error?.response?.status ?? error?.status ?? null;
}

export function getBackendErrorMessage(error) {
  if (!error?.response) {
    return NETWORK_ERROR_MESSAGE;
  }

  const status = getBackendStatusCode(error);

  // One list is easier than writing random messages in every catch block.
  return BACKEND_ERROR_MESSAGES[status] ?? DEFAULT_BACKEND_ERROR_MESSAGE;
}
