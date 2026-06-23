import httpClient from './httpClient';

const BASE_PATH = '/api/auth';

async function register(payload) {
  const response = await httpClient.post(`${BASE_PATH}/register`, payload);
  return response.data;
}

async function login(credentials) {
  const response = await httpClient.post(`${BASE_PATH}/login`, credentials);
  return response.data;
}

async function validate() {
  const response = await httpClient.get(`${BASE_PATH}/validate`);
  return response.data;
}

export const authApi = { register, login, validate };
