export const accountKeys = {
  all: ['accounts'],
  byId: (id) => [...accountKeys.all, 'id', String(id)],
  byCustomerEmail: (email) => [...accountKeys.all, 'email', String(email).trim().toLowerCase()],
  byCustomerId: (customerId) => [...accountKeys.all, 'customer', String(customerId)],
  balance: (id, currencyCode) => [
    ...accountKeys.all,
    'balance',
    String(id),
    String(currencyCode).trim().toUpperCase(),
  ],
  filter: (params, page) => [...accountKeys.all, 'filter', JSON.stringify(params ?? {}), page],
};
