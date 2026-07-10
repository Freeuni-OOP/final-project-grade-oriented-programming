export const customerKeys = {
  all: ['customers'],
  byId: (id) => [...customerKeys.all, 'id', String(id)],
  byEmail: (email) => [...customerKeys.all, 'email', String(email).trim().toLowerCase()],
  byAccount: (accountId) => [...customerKeys.all, 'account', String(accountId)],
  filter: (params, page) => [...customerKeys.all, 'filter', JSON.stringify(params ?? {}), page],
};
