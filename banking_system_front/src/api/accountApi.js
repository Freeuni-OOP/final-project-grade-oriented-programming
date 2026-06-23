import httpClient from './httpClient';

const BASE_PATH = '/api/account';

async function create(payload) {
  const response = await httpClient.post(BASE_PATH, payload);
  return response.data;
}

async function createCard(id, payload) {
  const response = await httpClient.post(`${BASE_PATH}/${id}/cards`, payload);
  return response.data;
}

async function activate(id) {
  const response = await httpClient.patch(`${BASE_PATH}/${id}/activate`);
  return response.data;
}

async function deactivate(id) {
  const response = await httpClient.patch(`${BASE_PATH}/${id}/deactivate`);
  return response.data;
}

async function getById(id) {
  const response = await httpClient.get(`${BASE_PATH}/${id}`);
  return response.data;
}

async function deleteAccount(id) {
  const response = await httpClient.delete(`${BASE_PATH}/${id}`);
  return response.data;
}

async function getByEmail(email) {
  const response = await httpClient.get(BASE_PATH, {
    params: { customerEmail: email },
  });
  return response.data;
}

async function getByCustomerId(customerId) {
  const response = await httpClient.get(`${BASE_PATH}/customer/${customerId}`);
  return response.data;
}

async function updateName(id, accountName) {
  const response = await httpClient.put(`${BASE_PATH}/${id}`, accountName, {
    headers: { 'Content-Type': 'text/plain' },
  });
  return response.data;
}

async function registerCustomer(accountId, customerId) {
  const response = await httpClient.put(`${BASE_PATH}/${accountId}/customers/${customerId}`);
  return response.data;
}

async function getBalanceByCurrency(id, currencyCode) {
  const response = await httpClient.get(`${BASE_PATH}/${id}/balance`, {
    params: { currencyCode },
  });
  return response.data;
}

export const accountApi = {
  create,
  createCard,
  activate,
  deactivate,
  getById,
  delete: deleteAccount,
  getByEmail,
  getByCustomerId,
  updateName,
  registerCustomer,
  getBalanceByCurrency,
};
