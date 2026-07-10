export const cardKeys = {
  all: ['cards'],
  byId: (id) => [...cardKeys.all, 'id', String(id)],
  account: (id) => [...cardKeys.all, 'account', String(id)],
  balances: (id) => [...cardKeys.all, 'balances', String(id)],
  expiration: (id) => [...cardKeys.all, 'expiration', String(id)],
  filter: (params, page) => [...cardKeys.all, 'filter', JSON.stringify(params ?? {}), page],
};
