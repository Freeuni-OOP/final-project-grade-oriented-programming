import { afterEach, describe, expect, it, vi } from 'vitest';
import httpClient from '../api/httpClient';
import { subscribeToBackendErrors } from '../api/errorNotifications';

const originalAdapter = httpClient.defaults.adapter;

describe('httpClient backend error notifications', () => {
  afterEach(() => {
    httpClient.defaults.adapter = originalAdapter;
  });

  it('notifies listeners when a backend request fails', async () => {
    const listener = vi.fn();
    const unsubscribe = subscribeToBackendErrors(listener);
    httpClient.defaults.adapter = vi.fn((config) =>
      Promise.reject({ response: { status: 404 }, config })
    );

    try {
      await expect(httpClient.get('/missing')).rejects.toMatchObject({
        response: { status: 404 },
      });

      expect(listener).toHaveBeenCalledWith(
        expect.objectContaining({ response: expect.objectContaining({ status: 404 }) })
      );
    } finally {
      unsubscribe();
    }
  });

  it('skips notifications for background requests', async () => {
    const listener = vi.fn();
    const unsubscribe = subscribeToBackendErrors(listener);
    httpClient.defaults.adapter = vi.fn((config) =>
      Promise.reject({ response: { status: 400 }, config })
    );

    try {
      await expect(httpClient.get('/background', { skipErrorToast: true })).rejects.toMatchObject({
        response: { status: 400 },
      });

      expect(listener).not.toHaveBeenCalled();
    } finally {
      unsubscribe();
    }
  });
});
