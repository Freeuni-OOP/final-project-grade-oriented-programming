import httpClient from './httpClient';

const BASE_PATH = '/api/card';

async function getById(id) {
  const response = await httpClient.get(`${BASE_PATH}/${id}`);
  return response.data;
}

async function getLinkedAccount(id) {
  const response = await httpClient.get(`${BASE_PATH}/${id}/account`);
  return response.data;
}

async function getBalances(id) {
  const response = await httpClient.get(`${BASE_PATH}/${id}/balances`);
  return response.data;
}

async function checkExpiration(id) {
  const response = await httpClient.get(`${BASE_PATH}/${id}/expiration`);
  return response.data;
}

async function deposit(id, payload) {
  const response = await httpClient.post(`${BASE_PATH}/${id}/deposit`, payload);
  return response.data;
}

async function withdraw(id, payload) {
  const response = await httpClient.post(`${BASE_PATH}/${id}/withdraw`, payload);
  return response.data;
}

async function transfer(payload) {
  const response = await httpClient.post(`${BASE_PATH}/transfer`, payload);
  return response.data;
}

async function exchangeCurrency(id, payload) {
  const response = await httpClient.post(`${BASE_PATH}/${id}/exchange-currency`, payload);
  return response.data;
}

async function addCurrency(id, currencyCode) {
  const response = await httpClient.patch(`${BASE_PATH}/${id}/currencies/${currencyCode}`);
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

async function deleteCard(id) {
  const response = await httpClient.delete(`${BASE_PATH}/${id}`);
  return response.data;
}

export const cardApi = {
  getById,
  getLinkedAccount,
  getBalances,
  checkExpiration,
  deposit,
  withdraw,
  transfer,
  exchangeCurrency,
  addCurrency,
  activate,
  deactivate,
  delete: deleteCard,
};
