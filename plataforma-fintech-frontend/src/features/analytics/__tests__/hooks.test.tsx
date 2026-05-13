import { describe, it, expect, vi, beforeEach } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import type { ReactNode } from 'react';
import {
  useAnalyticsSummaryQuery,
  useTopUsersQuery,
  useTopWalletsQuery,
  useFrequentRoutesQuery,
  useTopTransactionsQuery,
  useCyclesQuery,
  useTopWalletCategoriesQuery,
  useMovementByTypeQuery,
  useTotalMovedQuery,
} from '../hooks';

vi.mock('../../../api/analytics', () => ({
  getAnalyticsSummary: vi.fn(),
  getTopUsers: vi.fn(),
  getTopWallets: vi.fn(),
  getFrequentRoutes: vi.fn(),
  getTopTransactions: vi.fn(),
  getCycles: vi.fn(),
  getTopWalletCategories: vi.fn(),
  getMovementByType: vi.fn(),
  getTotalMoved: vi.fn(),
}));

import {
  getAnalyticsSummary,
  getTopUsers,
  getTopWallets,
  getFrequentRoutes,
  getTopTransactions,
  getCycles,
  getTopWalletCategories,
  getMovementByType,
  getTotalMoved,
} from '../../../api/analytics';

const MOCK_SUMMARY = {
  totalUsers: 3,
  totalWallets: 5,
  totalTransactions: 10,
  totalMovedAmount: 800.0,
  fraudEventCount: 2,
  unreadNotificationCount: 4,
};

const MOCK_METRIC_ITEMS = [{ id: 'USR_A', label: 'Ana', value: 5 }];
const MOCK_ROUTES = [{ sourceUserId: 'A', targetUserId: 'B', transferCount: 3, totalAmount: 900 }];

function makeWrapper() {
  const qc = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return {
    queryClient: qc,
    wrapper: ({ children }: { children: ReactNode }) => (
      <QueryClientProvider client={qc}>{children}</QueryClientProvider>
    ),
  };
}

describe('useAnalyticsSummaryQuery', () => {
  beforeEach(() => { vi.clearAllMocks(); });

  it('fetches and returns analytics summary', async () => {
    vi.mocked(getAnalyticsSummary).mockResolvedValue(MOCK_SUMMARY as any);
    const { wrapper } = makeWrapper();

    const { result } = renderHook(() => useAnalyticsSummaryQuery(), { wrapper });
    await waitFor(() => expect(result.current.isSuccess).toBe(true));

    expect(result.current.data?.totalUsers).toBe(3);
    expect(result.current.data?.fraudEventCount).toBe(2);
  });
});

describe('useTopUsersQuery', () => {
  beforeEach(() => { vi.clearAllMocks(); });

  it('fetches top users with default limit', async () => {
    vi.mocked(getTopUsers).mockResolvedValue(MOCK_METRIC_ITEMS as any);
    const { wrapper } = makeWrapper();

    const { result } = renderHook(() => useTopUsersQuery(), { wrapper });
    await waitFor(() => expect(result.current.isSuccess).toBe(true));

    expect(getTopUsers).toHaveBeenCalledWith(10);
    expect(result.current.data).toHaveLength(1);
  });

  it('fetches top users with custom limit', async () => {
    vi.mocked(getTopUsers).mockResolvedValue(MOCK_METRIC_ITEMS as any);
    const { wrapper } = makeWrapper();

    const { result } = renderHook(() => useTopUsersQuery(5), { wrapper });
    await waitFor(() => expect(result.current.isSuccess).toBe(true));

    expect(getTopUsers).toHaveBeenCalledWith(5);
  });
});

describe('useTopWalletsQuery', () => {
  beforeEach(() => { vi.clearAllMocks(); });

  it('fetches top wallets', async () => {
    vi.mocked(getTopWallets).mockResolvedValue(MOCK_METRIC_ITEMS as any);
    const { wrapper } = makeWrapper();

    const { result } = renderHook(() => useTopWalletsQuery(), { wrapper });
    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(getTopWallets).toHaveBeenCalledWith(10);
  });
});

describe('useFrequentRoutesQuery', () => {
  beforeEach(() => { vi.clearAllMocks(); });

  it('fetches frequent routes with default minTransfers', async () => {
    vi.mocked(getFrequentRoutes).mockResolvedValue(MOCK_ROUTES as any);
    const { wrapper } = makeWrapper();

    const { result } = renderHook(() => useFrequentRoutesQuery(), { wrapper });
    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(getFrequentRoutes).toHaveBeenCalledWith(1);
  });
});

describe('useTopTransactionsQuery', () => {
  beforeEach(() => { vi.clearAllMocks(); });

  it('fetches top transactions with default limit', async () => {
    vi.mocked(getTopTransactions).mockResolvedValue([]);
    const { wrapper } = makeWrapper();

    const { result } = renderHook(() => useTopTransactionsQuery(), { wrapper });
    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(getTopTransactions).toHaveBeenCalledWith(10);
  });
});

describe('useCyclesQuery', () => {
  beforeEach(() => { vi.clearAllMocks(); });

  it('fetches cycles', async () => {
    vi.mocked(getCycles).mockResolvedValue([['A', 'B']]);
    const { wrapper } = makeWrapper();

    const { result } = renderHook(() => useCyclesQuery(), { wrapper });
    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(result.current.data).toEqual([['A', 'B']]);
  });
});

describe('useTopWalletCategoriesQuery', () => {
  beforeEach(() => { vi.clearAllMocks(); });

  it('fetches top wallet categories', async () => {
    vi.mocked(getTopWalletCategories).mockResolvedValue(MOCK_METRIC_ITEMS as any);
    const { wrapper } = makeWrapper();

    const { result } = renderHook(() => useTopWalletCategoriesQuery(), { wrapper });
    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(getTopWalletCategories).toHaveBeenCalledWith(10);
  });
});

describe('useMovementByTypeQuery', () => {
  beforeEach(() => { vi.clearAllMocks(); });

  it('fetches movement by type', async () => {
    vi.mocked(getMovementByType).mockResolvedValue(MOCK_METRIC_ITEMS as any);
    const { wrapper } = makeWrapper();

    const { result } = renderHook(() => useMovementByTypeQuery(), { wrapper });
    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(getMovementByType).toHaveBeenCalled();
  });
});

describe('useTotalMovedQuery', () => {
  beforeEach(() => { vi.clearAllMocks(); });

  it('fetches total moved when from and to provided', async () => {
    const mockResult = { totalAmount: 1500, count: 3, from: '2026-01-01T00:00:00Z', to: '2026-12-31T00:00:00Z' };
    vi.mocked(getTotalMoved).mockResolvedValue(mockResult as any);
    const { wrapper } = makeWrapper();

    const { result } = renderHook(
      () => useTotalMovedQuery('2026-01-01T00:00:00Z', '2026-12-31T00:00:00Z'),
      { wrapper }
    );
    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(result.current.data?.totalAmount).toBe(1500);
  });

  it('is disabled when from is empty', () => {
    const { wrapper } = makeWrapper();
    const { result } = renderHook(() => useTotalMovedQuery('', '2026-12-31T00:00:00Z'), { wrapper });
    expect(result.current.fetchStatus).toBe('idle');
  });
});
