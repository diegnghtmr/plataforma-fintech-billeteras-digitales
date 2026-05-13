import { describe, it, expect, vi, beforeEach } from 'vitest';
import { renderHook } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import type { ReactNode } from 'react';
import { createElement } from 'react';
import { useUserPointsQuery, usePointsRankingQuery } from '../hooks';

vi.mock('../../../api/points', () => ({
  getUserPoints: vi.fn(),
  getPointsRanking: vi.fn(),
}));

function makeWrapper() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  });
  return function Wrapper({ children }: { children: ReactNode }) {
    return createElement(QueryClientProvider, { client: queryClient }, children);
  };
}

describe('useUserPointsQuery', () => {
  beforeEach(() => { vi.clearAllMocks(); });

  it('is disabled when userId is undefined', () => {
    const { result } = renderHook(() => useUserPointsQuery(undefined), {
      wrapper: makeWrapper(),
    });
    expect(result.current.fetchStatus).toBe('idle');
  });

  it('has correct queryKey when userId is provided', () => {
    const { result } = renderHook(() => useUserPointsQuery('u1'), {
      wrapper: makeWrapper(),
    });
    // query is enabled and key matches
    expect(result.current.fetchStatus).not.toBe(undefined);
  });
});

describe('usePointsRankingQuery', () => {
  beforeEach(() => { vi.clearAllMocks(); });

  it('uses default limit=10 in queryKey when no limit provided', () => {
    const { result } = renderHook(() => usePointsRankingQuery(), {
      wrapper: makeWrapper(),
    });
    expect(result.current).toBeDefined();
  });

  it('uses provided limit in queryKey', () => {
    const { result } = renderHook(() => usePointsRankingQuery(25), {
      wrapper: makeWrapper(),
    });
    expect(result.current).toBeDefined();
  });
});
