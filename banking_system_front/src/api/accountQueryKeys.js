export const accountKeys = {
  all: ['accounts'],
  byId: (id) => [...accountKeys.all, 'id', String(id)],
  byCustomerEmail: (email) => [...accountKeys.all, 'email', String(email).trim().toLowerCase()],
  byCustomerId: (customerId) => [...accountKeys.all, 'customer', String(customerId)],
};
