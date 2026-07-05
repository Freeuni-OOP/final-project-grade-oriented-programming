const listeners = new Set();

export function subscribeToBackendErrors(listener) {
  listeners.add(listener);

  return () => {
    listeners.delete(listener);
  };
}

export function notifyBackendError(error) {
  // Axios is not React, so this small list passes errors to the toast provider.
  listeners.forEach((listener) => listener(error));
}

export function shouldNotifyBackendError(error) {
  return !error?.config?.skipErrorToast;
}
