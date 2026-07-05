import axios from 'axios';
import { API_BASE_URL, TOKEN_KEY } from './config';
import { notifyBackendError, shouldNotifyBackendError } from './errorNotifications';

const httpClient = axios.create({
  baseURL: API_BASE_URL,
});

httpClient.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY);
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

httpClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem(TOKEN_KEY);
      if (window.location.pathname !== '/login') {
        window.location.href = '/login';
      }
    }

    if (shouldNotifyBackendError(error)) {
      notifyBackendError(error);
    }

    return Promise.reject(error);
  }
);

export default httpClient;
