import { describe, it, expect, vi, beforeEach } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import type { ReactNode } from 'react';
import { useUserWalletsQuery, useCreateWalletMutation } from '../hooks';

vi.mock('../../../api/wallets', () => ({
  listUserWallets: vi.fn(),
  createWallet: vi.fn(),
}));

import { listUserWallets, createWallet } from '../../../api/wallets';

function makeWrapper() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  });
  return function Wrapper({ children }: { children: ReactNode }) {
    return <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>;
  };
}

describe('useUserWalletsQuery', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('is disabled when userId is undefined', () => {
    const { result } = renderHook(() => useUserWalletsQuery(undefined), {
      wrapper: makeWrapper(),
    });
    expect(result.current.fetchStatus).toBe('idle');
  });

  it('fetches when userId is defined', async () => {
    const mockWallets = [{
      code: 'W001', name: 'Ahorros', type: 'SAVINGS',
      ownerId: 'USR001', balance: 500, active: true,
      createdAt: '2026-01-01T00:00:00Z', transactionCount: 0,
    }];
    vi.mocked(listUserWallets).mockResolvedValue(mockWallets);

    const { result } = renderHook(() => useUserWalletsQuery('USR001'), {
      wrapper: makeWrapper(),
    });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(result.current.data).toEqual(mockWallets);
  });
});

describe('useCreateWalletMutation', () => {
  it('exists and returns mutation object', () => {
    const { result } = renderHook(() => useCreateWalletMutation(), {
      wrapper: makeWrapper(),
    });
    expect(result.current.mutate).toBeDefined();
  });

  it('invalidates both wallet and user queries on success', async () => {
    const mockWallet = {
      code: 'W001', name: 'Ahorros', type: 'SAVINGS',
      ownerId: 'USR001', balance: 0, active: true,
      createdAt: '2026-01-01T00:00:00Z', transactionCount: 0,
    };
    vi.mocked(createWallet).mockResolvedValue(mockWallet);

    const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
    const invalidateSpy = vi.spyOn(queryClient, 'invalidateQueries');

    const wrapper = ({ children }: { children: ReactNode }) => (
      <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
    );

    const { result } = renderHook(() => useCreateWalletMutation(), { wrapper });
    result.current.mutate({ userId: 'USR001', code: 'W001', name: 'Ahorros', type: 'SAVINGS' });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));

    // Should invalidate wallets.byUser(userId)
    expect(invalidateSpy).toHaveBeenCalledWith(expect.objectContaining({
      queryKey: ['wallets', 'USR001'],
    }));
    // Should also invalidate users.detail(userId) for walletCount/totalBalance refresh
    expect(invalidateSpy).toHaveBeenCalledWith(expect.objectContaining({
      queryKey: ['users', 'USR001'],
    }));
  });
});
