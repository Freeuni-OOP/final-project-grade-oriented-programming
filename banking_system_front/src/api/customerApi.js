import httpClient from './httpClient';

const BASE_PATH = '/api/customer';

async function getById(id) {
  const response = await httpClient.get(`${BASE_PATH}/${id}`);
  return response.data;
}

async function getByEmail(email) {
  const response = await httpClient.get(`${BASE_PATH}/email/${email}`);
  return response.data;
}

async function getByAccount(accountId) {
  const response = await httpClient.get(`${BASE_PATH}/account/${accountId}`);
  return response.data;
}

async function update(id, payload) {
  const response = await httpClient.put(`${BASE_PATH}/${id}`, payload);
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

async function deleteCustomer(id) {
  const response = await httpClient.delete(`${BASE_PATH}/${id}/delete`);
  return response.data;
}

export const customerApi = {
  getById,
  getByEmail,
  getByAccount,
  update,
  activate,
  deactivate,
  delete: deleteCustomer,
};
