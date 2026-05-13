import { describe, it, expect, vi, beforeEach } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import type { ReactNode } from 'react';
import {
  useRechargeMutation,
  useWithdrawMutation,
  useInternalTransferMutation,
  useExternalTransferMutation,
} from '../hooks';

vi.mock('../../../api/operations', () => ({
  recharge: vi.fn(),
  withdraw: vi.fn(),
  internalTransfer: vi.fn(),
  externalTransfer: vi.fn(),
}));

import { recharge, withdraw, internalTransfer, externalTransfer } from '../../../api/operations';

const MOCK_TX = {
  id: 'TX-000001',
  timestamp: '2026-01-01T00:00:00Z',
  type: 'RECHARGE',
  amount: 500,
  sourceWalletId: 'W001',
  targetWalletId: null,
  sourceUserId: 'USR001',
  targetUserId: null,
  status: 'SUCCESSFUL',
  pointsGenerated: 5,
  description: null,
  reversible: true,
};

const MOCK_EXTERNAL_RESULT = {
  outgoingTransaction: { ...MOCK_TX, type: 'EXTERNAL_TRANSFER_SENT' },
  incomingTransaction: { ...MOCK_TX, id: 'TX-000002', type: 'EXTERNAL_TRANSFER_RECEIVED' },
};

describe('useRechargeMutation', () => {
  beforeEach(() => { vi.clearAllMocks(); });

  it('calls recharge with correct args', async () => {
    vi.mocked(recharge).mockResolvedValue(MOCK_TX as any);

    const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
    const wrapper = ({ children }: { children: ReactNode }) => (
      <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
    );

    const { result } = renderHook(() => useRechargeMutation(), { wrapper });
    result.current.mutate({ userId: 'USR001', walletId: 'W001', amount: 500 });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(recharge).toHaveBeenCalledWith('USR001', 'W001', { amount: 500 });
  });

  it('invalidates wallets.byUser and users.detail on success', async () => {
    vi.mocked(recharge).mockResolvedValue(MOCK_TX as any);

    const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
    const invalidateSpy = vi.spyOn(queryClient, 'invalidateQueries');
    const wrapper = ({ children }: { children: ReactNode }) => (
      <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
    );

    const { result } = renderHook(() => useRechargeMutation(), { wrapper });
    result.current.mutate({ userId: 'USR001', walletId: 'W001', amount: 500 });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(invalidateSpy).toHaveBeenCalledWith(expect.objectContaining({ queryKey: ['wallets', 'USR001'] }));
    expect(invalidateSpy).toHaveBeenCalledWith(expect.objectContaining({ queryKey: ['users', 'USR001'] }));
  });

  // T07-F08 (RED): points invalidation after recharge
  it('invalidates points.byUser and points.ranking on success', async () => {
    vi.mocked(recharge).mockResolvedValue(MOCK_TX as any);

    const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
    const invalidateSpy = vi.spyOn(queryClient, 'invalidateQueries');
    const wrapper = ({ children }: { children: ReactNode }) => (
      <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
    );

    const { result } = renderHook(() => useRechargeMutation(), { wrapper });
    result.current.mutate({ userId: 'USR001', walletId: 'W001', amount: 500 });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(invalidateSpy).toHaveBeenCalledWith(expect.objectContaining({ queryKey: ['points', 'USR001'] }));
    // ranking invalidated via predicate
    const rankingCall = invalidateSpy.mock.calls.find(
      (call) => typeof call[0] === 'object' && 'predicate' in (call[0] as object)
    );
    expect(rankingCall).toBeDefined();
  });

  // T09-F08: analytics and fraud invalidation after recharge
  it('invalidates analytics and fraud keys on success via predicate', async () => {
    vi.mocked(recharge).mockResolvedValue(MOCK_TX as any);

    const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
    const invalidateSpy = vi.spyOn(queryClient, 'invalidateQueries');
    const wrapper = ({ children }: { children: ReactNode }) => (
      <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
    );

    const { result } = renderHook(() => useRechargeMutation(), { wrapper });
    result.current.mutate({ userId: 'USR001', walletId: 'W001', amount: 500 });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));

    // Find the predicate call that covers analytics/fraud
    const predicateCalls = invalidateSpy.mock.calls.filter(
      (call) => typeof call[0] === 'object' && 'predicate' in (call[0] as object)
    );
    expect(predicateCalls.length).toBeGreaterThan(0);

    // The analytics/fraud predicate should match 'analytics' and 'fraud' keys
    const analyticsKey = { queryKey: ['analytics', 'summary'] };
    const fraudKey = { queryKey: ['fraud', 'events', {}] };

    const analyticsPredicateCall = predicateCalls.find((call) => {
      const opts = call[0] as { predicate: (q: any) => boolean };
      return opts.predicate(analyticsKey) === true;
    });
    const fraudPredicateCall = predicateCalls.find((call) => {
      const opts = call[0] as { predicate: (q: any) => boolean };
      return opts.predicate(fraudKey) === true;
    });

    expect(analyticsPredicateCall).toBeDefined();
    expect(fraudPredicateCall).toBeDefined();
  });
});

describe('useWithdrawMutation', () => {
  beforeEach(() => { vi.clearAllMocks(); });

  it('calls withdraw and invalidates on success', async () => {
    vi.mocked(withdraw).mockResolvedValue({ ...MOCK_TX, type: 'WITHDRAWAL' } as any);

    const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
    const invalidateSpy = vi.spyOn(queryClient, 'invalidateQueries');
    const wrapper = ({ children }: { children: ReactNode }) => (
      <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
    );

    const { result } = renderHook(() => useWithdrawMutation(), { wrapper });
    result.current.mutate({ userId: 'USR001', walletId: 'W001', amount: 200 });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(withdraw).toHaveBeenCalledWith('USR001', 'W001', { amount: 200 });
    expect(invalidateSpy).toHaveBeenCalledWith(expect.objectContaining({ queryKey: ['wallets', 'USR001'] }));
    expect(invalidateSpy).toHaveBeenCalledWith(expect.objectContaining({ queryKey: ['users', 'USR001'] }));
  });
});

describe('useInternalTransferMutation', () => {
  beforeEach(() => { vi.clearAllMocks(); });

  it('calls internalTransfer and invalidates on success', async () => {
    vi.mocked(internalTransfer).mockResolvedValue({ ...MOCK_TX, type: 'INTERNAL_TRANSFER' } as any);

    const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
    const invalidateSpy = vi.spyOn(queryClient, 'invalidateQueries');
    const wrapper = ({ children }: { children: ReactNode }) => (
      <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
    );

    const { result } = renderHook(() => useInternalTransferMutation(), { wrapper });
    result.current.mutate({ userId: 'USR001', sourceWalletId: 'W001', targetWalletId: 'W002', amount: 150 });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(internalTransfer).toHaveBeenCalledWith('USR001', { sourceWalletId: 'W001', targetWalletId: 'W002', amount: 150 });
    expect(invalidateSpy).toHaveBeenCalledWith(expect.objectContaining({ queryKey: ['wallets', 'USR001'] }));
    expect(invalidateSpy).toHaveBeenCalledWith(expect.objectContaining({ queryKey: ['users', 'USR001'] }));
  });
});

describe('useExternalTransferMutation', () => {
  beforeEach(() => { vi.clearAllMocks(); });

  it('calls externalTransfer and invalidates BOTH users on success', async () => {
    vi.mocked(externalTransfer).mockResolvedValue(MOCK_EXTERNAL_RESULT as any);

    const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
    const invalidateSpy = vi.spyOn(queryClient, 'invalidateQueries');
    const wrapper = ({ children }: { children: ReactNode }) => (
      <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
    );

    const { result } = renderHook(() => useExternalTransferMutation(), { wrapper });
    result.current.mutate({
      sourceUserId: 'USR001', sourceWalletId: 'W001',
      targetUserId: 'USR002', targetWalletId: 'W002',
      amount: 500,
    });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));

    // Source user invalidations
    expect(invalidateSpy).toHaveBeenCalledWith(expect.objectContaining({ queryKey: ['wallets', 'USR001'] }));
    expect(invalidateSpy).toHaveBeenCalledWith(expect.objectContaining({ queryKey: ['users', 'USR001'] }));
    // Target user invalidations
    expect(invalidateSpy).toHaveBeenCalledWith(expect.objectContaining({ queryKey: ['wallets', 'USR002'] }));
    expect(invalidateSpy).toHaveBeenCalledWith(expect.objectContaining({ queryKey: ['users', 'USR002'] }));
  });
});
