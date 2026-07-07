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

    await accountApi.getBalanceByCurrency(10, 'USD');
    expect(adapter).toHaveBeenLastCalledWith(
      expect.objectContaining({
        method: 'get',
        params: { currencyCode: 'USD' },
        url: '/api/account/10/balance',
      })
    );
  });

  it('uses account write endpoints for part 2 actions', async () => {
    const adapter = mockAdapter();
    const payload = { accountName: 'Daily', category: 'CHECKING' };

    await accountApi.create(payload);
    expect(adapter).toHaveBeenLastCalledWith(
      expect.objectContaining({
        data: JSON.stringify(payload),
        method: 'post',
        url: '/api/account',
      })
    );

    await accountApi.updateName(10, 'Travel');
    expect(adapter).toHaveBeenLastCalledWith(
      expect.objectContaining({
        data: 'Travel',
        method: 'put',
        url: '/api/account/10',
      })
    );

    await accountApi.activate(10);
    expect(adapter).toHaveBeenLastCalledWith(
      expect.objectContaining({ method: 'patch', url: '/api/account/10/activate' })
    );

    await accountApi.deactivate(10);
    expect(adapter).toHaveBeenLastCalledWith(
      expect.objectContaining({ method: 'patch', url: '/api/account/10/deactivate' })
    );

    await accountApi.registerCustomer(10, 42);
    expect(adapter).toHaveBeenLastCalledWith(
      expect.objectContaining({ method: 'put', url: '/api/account/10/customers/42' })
    );
  });
});
