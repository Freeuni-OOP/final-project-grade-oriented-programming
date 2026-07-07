import { afterEach, describe, expect, it, vi } from 'vitest';
import httpClient from '../api/httpClient';
import { accountApi } from '../api/accountApi';

const originalAdapter = httpClient.defaults.adapter;

function mockAdapter(data = { ok: true }) {
  const adapter = vi.fn((config) =>
    Promise.resolve({
      data,
      status: 200,
      statusText: 'OK',
      headers: {},
      config,
    })
  );

  httpClient.defaults.adapter = adapter;
  return adapter;
}

describe('accountApi read endpoints', () => {
  afterEach(() => {
    httpClient.defaults.adapter = originalAdapter;
  });

  it('uses account detail and listing endpoints', async () => {
    const adapter = mockAdapter();

    await accountApi.getById(10);
    expect(adapter).toHaveBeenLastCalledWith(
      expect.objectContaining({ method: 'get', url: '/api/account/10' })
    );

    await accountApi.getByEmail('nino@test.com');
    expect(adapter).toHaveBeenLastCalledWith(
      expect.objectContaining({
        method: 'get',
        params: { customerEmail: 'nino@test.com' },
        url: '/api/account',
      })
    );

    await accountApi.getByCustomerId(42);
    expect(adapter).toHaveBeenLastCalledWith(
      expect.objectContaining({ method: 'get', url: '/api/account/customer/42' })
    );
  });
});
