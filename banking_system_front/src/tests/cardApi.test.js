import { afterEach, describe, expect, it, vi } from 'vitest';
import httpClient from '../api/httpClient';
import { cardApi } from '../api/cardApi';

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

describe('cardApi', () => {
  afterEach(() => {
    httpClient.defaults.adapter = originalAdapter;
  });

  it('uses card read endpoints', async () => {
    const adapter = mockAdapter();

    await cardApi.getById(10);
    expect(adapter).toHaveBeenLastCalledWith(
      expect.objectContaining({ method: 'get', url: '/api/card/10' })
    );

    await cardApi.getLinkedAccount(10);
    expect(adapter).toHaveBeenLastCalledWith(
      expect.objectContaining({ method: 'get', url: '/api/card/10/account' })
    );

    await cardApi.getBalances(10);
    expect(adapter).toHaveBeenLastCalledWith(
      expect.objectContaining({ method: 'get', url: '/api/card/10/balances' })
    );

    await cardApi.checkExpiration(10);
    expect(adapter).toHaveBeenLastCalledWith(
      expect.objectContaining({ method: 'get', url: '/api/card/10/expiration' })
    );
  });

  it('uses card write endpoints', async () => {
    const adapter = mockAdapter();

    await cardApi.deposit(10, { amountToDeposit: '25', currencyCode: 'GEL' });
    expect(adapter).toHaveBeenLastCalledWith(
      expect.objectContaining({
        data: JSON.stringify({ amountToDeposit: '25', currencyCode: 'GEL' }),
        method: 'post',
        url: '/api/card/10/deposit',
      })
    );

    await cardApi.withdraw(10, { amountToWithdraw: '15', currencyCode: 'USD' });
    expect(adapter).toHaveBeenLastCalledWith(
      expect.objectContaining({
        data: JSON.stringify({ amountToWithdraw: '15', currencyCode: 'USD' }),
        method: 'post',
        url: '/api/card/10/withdraw',
      })
    );

    await cardApi.transfer({
      senderCardId: 10,
      receiverCardId: 12,
      amount: '5',
      currencyCode: 'EUR',
    });
    expect(adapter).toHaveBeenLastCalledWith(
      expect.objectContaining({
        data: JSON.stringify({
          senderCardId: 10,
          receiverCardId: 12,
          amount: '5',
          currencyCode: 'EUR',
        }),
        method: 'post',
        url: '/api/card/transfer',
      })
    );

    await cardApi.exchangeCurrency(10, {
      amount: '20',
      fromCurrencyCode: 'USD',
      toCurrencyCode: 'GEL',
    });
    expect(adapter).toHaveBeenLastCalledWith(
      expect.objectContaining({
        data: JSON.stringify({
          amount: '20',
          fromCurrencyCode: 'USD',
          toCurrencyCode: 'GEL',
        }),
        method: 'post',
        url: '/api/card/10/exchange-currency',
      })
    );

    await cardApi.addCurrency(10, 'GBP');
    expect(adapter).toHaveBeenLastCalledWith(
      expect.objectContaining({ method: 'patch', url: '/api/card/10/currencies/GBP' })
    );

    await cardApi.activate(10);
    expect(adapter).toHaveBeenLastCalledWith(
      expect.objectContaining({ method: 'patch', url: '/api/card/10/activate' })
    );

    await cardApi.deactivate(10);
    expect(adapter).toHaveBeenLastCalledWith(
      expect.objectContaining({ method: 'patch', url: '/api/card/10/deactivate' })
    );

    await cardApi.delete(10);
    expect(adapter).toHaveBeenLastCalledWith(
      expect.objectContaining({ method: 'delete', url: '/api/card/10' })
    );
  });
});
