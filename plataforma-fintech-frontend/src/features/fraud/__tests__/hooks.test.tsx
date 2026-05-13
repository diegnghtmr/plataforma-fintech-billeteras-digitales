import { describe, it, expect, vi, beforeEach } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import type { ReactNode } from 'react';
import { useFraudEventsQuery } from '../hooks';

vi.mock('../../../api/fraud', () => ({
  getFraudEvents: vi.fn(),
}));

import { getFraudEvents } from '../../../api/fraud';

const MOCK_EVENTS = [
  {
    id: 'FRD-000001',
    userId: 'USR001',
    transactionId: 'TX-000001',
    type: 'LARGE_TRANSACTION',
    severity: 'HIGH',
    description: 'Transacción de monto inusual: 15000.0',
    createdAt: '2026-01-01T00:00:00Z',
  },
];

function makeWrapper() {
  const qc = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return {
    queryClient: qc,
    wrapper: ({ children }: { children: ReactNode }) => (
      <QueryClientProvider client={qc}>{children}</QueryClientProvider>
    ),
  };
}

describe('useFraudEventsQuery', () => {
  beforeEach(() => { vi.clearAllMocks(); });

  it('fetches all events with no filters', async () => {
    vi.mocked(getFraudEvents).mockResolvedValue(MOCK_EVENTS as any);
    const { wrapper } = makeWrapper();

    const { result } = renderHook(() => useFraudEventsQuery(), { wrapper });
    await waitFor(() => expect(result.current.isSuccess).toBe(true));

    expect(getFraudEvents).toHaveBeenCalledWith(undefined);
    expect(result.current.data).toHaveLength(1);
  });

  it('fetches events with severity filter', async () => {
    vi.mocked(getFraudEvents).mockResolvedValue(MOCK_EVENTS as any);
    const { wrapper } = makeWrapper();

    const { result } = renderHook(
      () => useFraudEventsQuery({ severity: 'HIGH' }),
      { wrapper }
    );
    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(getFraudEvents).toHaveBeenCalledWith({ severity: 'HIGH' });
  });

  it('fetches events with userId filter', async () => {
    vi.mocked(getFraudEvents).mockResolvedValue([]);
    const { wrapper } = makeWrapper();

    const { result } = renderHook(
      () => useFraudEventsQuery({ userId: 'USR_X' }),
      { wrapper }
    );
    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(getFraudEvents).toHaveBeenCalledWith({ userId: 'USR_X' });
  });
});
