import { describe, it, expect, vi, beforeEach } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import type { ReactNode } from 'react';
import {
  useUserTransactionsQuery,
  useWalletTransactionsQuery,
  useReverseTransactionMutation,
} from '../hooks';

vi.mock('../../../api/transactions', () => ({
  getUserTransactions: vi.fn(),
  getWalletTransactions: vi.fn(),
  reverseTransaction: vi.fn(),
}));

import {
  getUserTransactions,
  getWalletTransactions,
  reverseTransaction,
} from '../../../api/transactions';

const MOCK_TX = {
  id: 'TX-000001',
  timestamp: '2026-01-01T00:00:00Z',
  type: 'RECHARGE',
  amount: 100,
  sourceWalletId: 'W001',
  targetWalletId: null,
  sourceUserId: 'USR001',
  targetUserId: null,
  status: 'SUCCESSFUL',
  pointsGenerated: 1,
  description: null,
  reversible: true,
};

function makeWrapper(queryClient: QueryClient) {
  return function Wrapper({ children }: { children: ReactNode }) {
    return <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>;
  };
}

describe('useUserTransactionsQuery', () => {
  beforeEach(() => { vi.clearAllMocks(); });

  it('calls getUserTransactions with userId and filters', async () => {
    vi.mocked(getUserTransactions).mockResolvedValue([MOCK_TX as any]);

    const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
    const wrapper = makeWrapper(queryClient);

    const filters = { type: 'RECHARGE' as const };
    const { result } = renderHook(
      () => useUserTransactionsQuery('USR001', filters),
      { wrapper }
    );

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(getUserTransactions).toHaveBeenCalledWith('USR001', filters);
    expect(result.current.data).toEqual([MOCK_TX]);
  });

  it('is disabled when userId is empty', () => {
    const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
    const wrapper = makeWrapper(queryClient);

    const { result } = renderHook(
      () => useUserTransactionsQuery('', undefined),
      { wrapper }
    );

    // When userId is empty, query should not fire
    expect(result.current.fetchStatus).toBe('idle');
    expect(getUserTransactions).not.toHaveBeenCalled();
  });
});

describe('useWalletTransactionsQuery', () => {
  beforeEach(() => { vi.clearAllMocks(); });

  it('calls getWalletTransactions with userId and walletId', async () => {
    vi.mocked(getWalletTransactions).mockResolvedValue([MOCK_TX as any]);

    const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
    const wrapper = makeWrapper(queryClient);

    const { result } = renderHook(
      () => useWalletTransactionsQuery('USR001', 'W001'),
      { wrapper }
    );

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(getWalletTransactions).toHaveBeenCalledWith('USR001', 'W001');
  });
});

describe('useReverseTransactionMutation', () => {
  beforeEach(() => { vi.clearAllMocks(); });

  it('calls reverseTransaction and invalidates 4 query keys on success', async () => {
    const reversedTx = { ...MOCK_TX, status: 'REVERSED' };
    vi.mocked(reverseTransaction).mockResolvedValue(reversedTx as any);

    const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
    const invalidateSpy = vi.spyOn(queryClient, 'invalidateQueries');
    const wrapper = makeWrapper(queryClient);

    const { result } = renderHook(
      () => useReverseTransactionMutation(),
      { wrapper }
    );

    result.current.mutate({
      transactionId: 'TX-000001',
      userId: 'USR001',
      walletId: 'W001',
    });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));

    expect(reverseTransaction).toHaveBeenCalledWith('TX-000001');

    // 4 invalidations: byUser, byWallet, wallets.byUser, users.detail
    expect(invalidateSpy).toHaveBeenCalledWith(
      expect.objectContaining({ queryKey: ['transactions', 'user', 'USR001', {}] })
    );
    expect(invalidateSpy).toHaveBeenCalledWith(
      expect.objectContaining({ queryKey: ['transactions', 'wallet', 'USR001', 'W001'] })
    );
    expect(invalidateSpy).toHaveBeenCalledWith(
      expect.objectContaining({ queryKey: ['wallets', 'USR001'] })
    );
    expect(invalidateSpy).toHaveBeenCalledWith(
      expect.objectContaining({ queryKey: ['users', 'USR001'] })
    );

    // T07-F08 (RED): points invalidations after reverse
    expect(invalidateSpy).toHaveBeenCalledWith(
      expect.objectContaining({ queryKey: ['points', 'USR001'] })
    );
    // ranking via predicate
    const rankingCall = invalidateSpy.mock.calls.find(
      (call) => typeof call[0] === 'object' && 'predicate' in (call[0] as object)
    );
    expect(rankingCall).toBeDefined();
  });
});
