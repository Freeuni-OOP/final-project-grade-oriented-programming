import { getBackendErrorMessage } from './errorMessages';

function addFieldError(errors, allowedFields, field, message) {
  if (!field || !message || !allowedFields.has(field)) {
    return;
  }

  errors[field] = String(message);
}

function collectFromObject(errors, allowedFields, data) {
  Object.entries(data).forEach(([field, message]) => {
    if (typeof message === 'string') {
      addFieldError(errors, allowedFields, field, message);
    }
  });
}

function collectFromArray(errors, allowedFields, items) {
  items.forEach((item) => {
    if (!item || typeof item !== 'object') {
      return;
    }

    addFieldError(
      errors,
      allowedFields,
      item.field ?? item.name,
      item.message ?? item.defaultMessage
    );
  });
}

export function getBackendFieldErrors(error, fieldNames) {
  const allowedFields = new Set(fieldNames);
  const data = error?.response?.data;
  const errors = {};

  if (!data || typeof data !== 'object') {
    return errors;
  }

  // Different backends write validation errors in slightly different shapes.
  collectFromObject(errors, allowedFields, data);

  if (data.errors && !Array.isArray(data.errors) && typeof data.errors === 'object') {
    collectFromObject(errors, allowedFields, data.errors);
  }

  if (Array.isArray(data.errors)) {
    collectFromArray(errors, allowedFields, data.errors);
  }

  if (Array.isArray(data.fieldErrors)) {
    collectFromArray(errors, allowedFields, data.fieldErrors);
  }

  if (
    data.fieldErrors &&
    !Array.isArray(data.fieldErrors) &&
    typeof data.fieldErrors === 'object'
  ) {
    collectFromObject(errors, allowedFields, data.fieldErrors);
  }

  return errors;
}

export function applyBackendFormErrors(error, setError, fieldNames) {
  const fieldErrors = getBackendFieldErrors(error, fieldNames);

  Object.entries(fieldErrors).forEach(([field, message]) => {
    setError(field, { type: 'server', message });
  });

  setError('root', { type: 'server', message: getBackendErrorMessage(error) });

  return Object.keys(fieldErrors).length > 0;
}
