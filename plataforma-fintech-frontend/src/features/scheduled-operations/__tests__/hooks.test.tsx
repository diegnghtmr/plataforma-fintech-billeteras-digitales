import { describe, it, expect, vi, beforeEach } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import type { ReactNode } from 'react';
import {
  useScheduledOperationsQuery,
  useCreateScheduledOperationMutation,
  useCancelScheduledOperationMutation,
  useRunScheduledOpsMutation,
} from '../hooks';

vi.mock('../../../api/scheduled-operations', () => ({
  getScheduledOperations: vi.fn(),
  createScheduledOperation: vi.fn(),
  cancelScheduledOperation: vi.fn(),
  runScheduledOps: vi.fn(),
}));

import {
  getScheduledOperations,
  createScheduledOperation,
  cancelScheduledOperation,
  runScheduledOps,
} from '../../../api/scheduled-operations';

const MOCK_OP = {
  id: 'SOP-000001',
  type: 'RECHARGE',
  status: 'PENDING',
  sourceUserId: 'USR001',
  sourceWalletId: 'W001',
  targetUserId: null,
  targetWalletId: null,
  amount: 100,
  scheduledAt: '2027-01-01T00:00:00Z',
  description: null,
};

function makeWrapper() {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return {
    queryClient,
    wrapper: ({ children }: { children: ReactNode }) => (
      <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
    ),
  };
}

describe('useScheduledOperationsQuery', () => {
  beforeEach(() => { vi.clearAllMocks(); });

  it('fetches scheduled operations', async () => {
    vi.mocked(getScheduledOperations).mockResolvedValue([MOCK_OP as any]);
    const { wrapper } = makeWrapper();
    const { result } = renderHook(() => useScheduledOperationsQuery(), { wrapper });
    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(result.current.data).toHaveLength(1);
  });
});

describe('useCreateScheduledOperationMutation', () => {
  beforeEach(() => { vi.clearAllMocks(); });

  it('creates and invalidates scheduledOperations', async () => {
    vi.mocked(createScheduledOperation).mockResolvedValue(MOCK_OP as any);
    const { wrapper, queryClient } = makeWrapper();
    const invalidateSpy = vi.spyOn(queryClient, 'invalidateQueries');
    const { result } = renderHook(() => useCreateScheduledOperationMutation(), { wrapper });

    result.current.mutate({
      type: 'RECHARGE',
      sourceUserId: 'USR001',
      sourceWalletId: 'W001',
      amount: 100,
      scheduledAt: '2027-01-01T00:00:00Z',
    });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(invalidateSpy).toHaveBeenCalledWith(
      expect.objectContaining({ queryKey: ['scheduledOperations'] })
    );
  });
});

describe('useCancelScheduledOperationMutation', () => {
  beforeEach(() => { vi.clearAllMocks(); });

  it('cancels and invalidates scheduledOperations', async () => {
    vi.mocked(cancelScheduledOperation).mockResolvedValue({ ...MOCK_OP, status: 'CANCELLED' } as any);
    const { wrapper, queryClient } = makeWrapper();
    const invalidateSpy = vi.spyOn(queryClient, 'invalidateQueries');
    const { result } = renderHook(() => useCancelScheduledOperationMutation(), { wrapper });

    result.current.mutate('SOP-000001');

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(invalidateSpy).toHaveBeenCalledWith(
      expect.objectContaining({ queryKey: ['scheduledOperations'] })
    );
  });
});

describe('useRunScheduledOpsMutation', () => {
  beforeEach(() => { vi.clearAllMocks(); });

  it('runs scheduled ops and invalidates scheduledOperations and notifications', async () => {
    const MOCK_REPORT = { executed: 2, failed: 0, executedIds: ['SOP-1', 'SOP-2'], failedIds: [] };
    vi.mocked(runScheduledOps).mockResolvedValue(MOCK_REPORT as any);
    const { wrapper, queryClient } = makeWrapper();
    const invalidateSpy = vi.spyOn(queryClient, 'invalidateQueries');
    const { result } = renderHook(() => useRunScheduledOpsMutation(), { wrapper });

    result.current.mutate();

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(result.current.data?.executed).toBe(2);
    expect(invalidateSpy).toHaveBeenCalledWith(
      expect.objectContaining({ queryKey: ['scheduledOperations'] })
    );
    expect(invalidateSpy).toHaveBeenCalledWith(
      expect.objectContaining({ queryKey: ['notifications'] })
    );
  });
});
