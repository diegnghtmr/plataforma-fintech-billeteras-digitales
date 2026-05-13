import { describe, it, expect, vi, beforeEach } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import type { ReactNode } from 'react';
import {
  useUserNotificationsQuery,
  useMarkNotificationReadMutation,
} from '../hooks';

vi.mock('../../../api/notifications', () => ({
  getUserNotifications: vi.fn(),
  markNotificationAsRead: vi.fn(),
}));

import { getUserNotifications, markNotificationAsRead } from '../../../api/notifications';

const MOCK_NOTIF = {
  id: 'NTF-000001',
  userId: 'USR001',
  type: 'SYSTEM',
  severity: 'INFO',
  title: 'Test',
  message: 'Test msg',
  read: false,
  createdAt: '2026-01-01T00:00:00Z',
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

describe('useUserNotificationsQuery', () => {
  beforeEach(() => { vi.clearAllMocks(); });

  it('is disabled without userId', () => {
    const { wrapper } = makeWrapper();
    const { result } = renderHook(
      () => useUserNotificationsQuery(undefined, false),
      { wrapper }
    );
    expect(result.current.fetchStatus).toBe('idle');
    expect(getUserNotifications).not.toHaveBeenCalled();
  });

  it('fetches when userId is provided', async () => {
    vi.mocked(getUserNotifications).mockResolvedValue([MOCK_NOTIF as any]);
    const { wrapper } = makeWrapper();
    const { result } = renderHook(
      () => useUserNotificationsQuery('USR001', false),
      { wrapper }
    );
    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(result.current.data).toHaveLength(1);
  });

  it('uses different cache key for unreadOnly=true', () => {
    const { wrapper, queryClient } = makeWrapper();
    vi.mocked(getUserNotifications).mockResolvedValue([]);

    renderHook(() => useUserNotificationsQuery('USR001', false), { wrapper });
    renderHook(() => useUserNotificationsQuery('USR001', true), { wrapper });

    const queries = queryClient.getQueryCache().getAll();
    const keys = queries.map((q) => JSON.stringify(q.queryKey));
    // Both false and true variants should be separate cache entries
    const hasFalse = keys.some((k) => k.includes('false'));
    const hasTrue = keys.some((k) => k.includes('true'));
    expect(hasFalse).toBe(true);
    expect(hasTrue).toBe(true);
  });
});

describe('useMarkNotificationReadMutation', () => {
  beforeEach(() => { vi.clearAllMocks(); });

  it('invalidates both unreadOnly variants for the user on success', async () => {
    vi.mocked(markNotificationAsRead).mockResolvedValue({ ...MOCK_NOTIF, read: true } as any);
    const { wrapper, queryClient } = makeWrapper();
    const invalidateSpy = vi.spyOn(queryClient, 'invalidateQueries');

    const { result } = renderHook(
      () => useMarkNotificationReadMutation('USR001'),
      { wrapper }
    );
    result.current.mutate('NTF-000001');

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    // Should have called invalidateQueries with a predicate (for both variants)
    const predicateCall = invalidateSpy.mock.calls.find(
      (call) => typeof call[0] === 'object' && 'predicate' in (call[0] as object)
    );
    expect(predicateCall).toBeDefined();
  });
});
