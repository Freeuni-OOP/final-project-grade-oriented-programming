import { afterEach, describe, expect, it, vi } from 'vitest';
import httpClient from '../api/httpClient';
import { customerApi } from '../api/customerApi';

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

describe('customerApi', () => {
  afterEach(() => {
    httpClient.defaults.adapter = originalAdapter;
  });

  it('uses the required read endpoints', async () => {
    const adapter = mockAdapter();

    await customerApi.getById(12);
    expect(adapter).toHaveBeenLastCalledWith(
      expect.objectContaining({ method: 'get', url: '/api/customer/12' })
    );

    await customerApi.getByEmail('nino@test.com');
    expect(adapter).toHaveBeenLastCalledWith(
      expect.objectContaining({
        method: 'get',
        params: { email: 'nino@test.com' },
        url: '/api/customer',
      })
    );

    await customerApi.getByAccount(7);
    expect(adapter).toHaveBeenLastCalledWith(
      expect.objectContaining({ method: 'get', url: '/api/customer/account/7' })
    );
  });

  it('uses the required write endpoints', async () => {
    const adapter = mockAdapter();
    const payload = {
      firstName: 'Nino',
      lastName: 'Test',
      phoneNumber: '5551234',
      address: 'Tbilisi',
    };

    await customerApi.update(12, payload);
    expect(adapter).toHaveBeenLastCalledWith(
      expect.objectContaining({
        data: JSON.stringify(payload),
        method: 'put',
        url: '/api/customer/12',
      })
    );

    await customerApi.activate(12);
    expect(adapter).toHaveBeenLastCalledWith(
      expect.objectContaining({ method: 'patch', url: '/api/customer/12/activate' })
    );

    await customerApi.deactivate(12);
    expect(adapter).toHaveBeenLastCalledWith(
      expect.objectContaining({ method: 'patch', url: '/api/customer/12/deactivate' })
    );

    await customerApi.delete(12);
    expect(adapter).toHaveBeenLastCalledWith(
      expect.objectContaining({ method: 'delete', url: '/api/customer/12/delete' })
    );
  });
});
