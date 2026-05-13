import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import type { ReactNode } from 'react';
import { ScheduledOperationsPage } from '../ScheduledOperationsPage';

vi.mock('../hooks', () => ({
  useScheduledOperationsQuery: vi.fn(),
  useCreateScheduledOperationMutation: vi.fn(),
  useCancelScheduledOperationMutation: vi.fn(),
  useRunScheduledOpsMutation: vi.fn(),
}));

vi.mock('../../../stores/use-selection-store', () => ({
  useSelectionStore: vi.fn(),
}));

import {
  useScheduledOperationsQuery,
  useCreateScheduledOperationMutation,
  useCancelScheduledOperationMutation,
  useRunScheduledOpsMutation,
} from '../hooks';
import { useSelectionStore } from '../../../stores/use-selection-store';

const MOCK_PENDING_OP = {
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

const MOCK_CANCELLED_OP = {
  ...MOCK_PENDING_OP,
  id: 'SOP-000002',
  status: 'CANCELLED',
};

function makeWrapper() {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return ({ children }: { children: ReactNode }) => (
    <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
  );
}

describe('ScheduledOperationsPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(useCreateScheduledOperationMutation).mockReturnValue({
      mutate: vi.fn(),
      isPending: false,
      isError: false,
    } as any);
    vi.mocked(useCancelScheduledOperationMutation).mockReturnValue({
      mutate: vi.fn(),
      isPending: false,
    } as any);
    vi.mocked(useRunScheduledOpsMutation).mockReturnValue({
      mutate: vi.fn(),
      isPending: false,
      isSuccess: false,
      isError: false,
      data: undefined,
    } as any);
  });

  it('renders list of operations', () => {
    vi.mocked(useSelectionStore).mockImplementation((selector: any) =>
      selector({ selectedUserId: 'USR001', selectedWalletId: 'W001' })
    );
    vi.mocked(useScheduledOperationsQuery).mockReturnValue({
      data: [MOCK_PENDING_OP],
      isLoading: false,
    } as any);

    render(<ScheduledOperationsPage />, { wrapper: makeWrapper() });

    expect(screen.getByText('SOP-000001')).toBeInTheDocument();
  });

  // S-FE-1: Cancel button disabled for non-PENDING
  it('cancel button disabled for CANCELLED status', () => {
    vi.mocked(useSelectionStore).mockImplementation((selector: any) =>
      selector({ selectedUserId: 'USR001', selectedWalletId: 'W001' })
    );
    vi.mocked(useScheduledOperationsQuery).mockReturnValue({
      data: [MOCK_CANCELLED_OP],
      isLoading: false,
    } as any);

    render(<ScheduledOperationsPage />, { wrapper: makeWrapper() });

    const cancelButton = screen.getByRole('button', { name: /cancelar/i });
    expect(cancelButton).toBeDisabled();
  });

  it('cancel button enabled for PENDING status', () => {
    vi.mocked(useSelectionStore).mockImplementation((selector: any) =>
      selector({ selectedUserId: 'USR001', selectedWalletId: 'W001' })
    );
    vi.mocked(useScheduledOperationsQuery).mockReturnValue({
      data: [MOCK_PENDING_OP],
      isLoading: false,
    } as any);

    render(<ScheduledOperationsPage />, { wrapper: makeWrapper() });

    const cancelButton = screen.getByRole('button', { name: /cancelar/i });
    expect(cancelButton).not.toBeDisabled();
  });

  it('submitting create form calls mutation', () => {
    const mutateFn = vi.fn();
    vi.mocked(useSelectionStore).mockImplementation((selector: any) =>
      selector({ selectedUserId: 'USR001', selectedWalletId: 'W001' })
    );
    vi.mocked(useScheduledOperationsQuery).mockReturnValue({
      data: [],
      isLoading: false,
    } as any);
    vi.mocked(useCreateScheduledOperationMutation).mockReturnValue({
      mutate: mutateFn,
      isPending: false,
      isError: false,
    } as any);

    render(<ScheduledOperationsPage />, { wrapper: makeWrapper() });
    // Form should be present
    expect(screen.getByRole('combobox', { name: /tipo/i })).toBeInTheDocument();
  });

  // W2 — new tests

  it('renders "Ejecutar vencidas" button', () => {
    vi.mocked(useSelectionStore).mockImplementation((selector: any) =>
      selector({ selectedUserId: null, selectedWalletId: null })
    );
    vi.mocked(useScheduledOperationsQuery).mockReturnValue({
      data: [], isLoading: false,
    } as any);

    render(<ScheduledOperationsPage />, { wrapper: makeWrapper() });

    expect(screen.getByRole('button', { name: /ejecutar vencidas/i })).toBeInTheDocument();
  });

  it('clicking "Ejecutar vencidas" calls useRunScheduledOpsMutation.mutate', () => {
    const runFn = vi.fn();
    vi.mocked(useRunScheduledOpsMutation).mockReturnValue({
      mutate: runFn, isPending: false, isSuccess: false, isError: false, data: undefined,
    } as any);
    vi.mocked(useSelectionStore).mockImplementation((selector: any) =>
      selector({ selectedUserId: null, selectedWalletId: null })
    );
    vi.mocked(useScheduledOperationsQuery).mockReturnValue({
      data: [], isLoading: false,
    } as any);

    render(<ScheduledOperationsPage />, { wrapper: makeWrapper() });

    fireEvent.click(screen.getByRole('button', { name: /ejecutar vencidas/i }));

    expect(runFn).toHaveBeenCalledTimes(1);
  });

  it('shows executed/failed summary after successful run', () => {
    vi.mocked(useRunScheduledOpsMutation).mockReturnValue({
      mutate: vi.fn(),
      isPending: false,
      isSuccess: true,
      isError: false,
      data: { executed: 3, failed: 1 },
    } as any);
    vi.mocked(useSelectionStore).mockImplementation((selector: any) =>
      selector({ selectedUserId: null, selectedWalletId: null })
    );
    vi.mocked(useScheduledOperationsQuery).mockReturnValue({
      data: [], isLoading: false,
    } as any);

    render(<ScheduledOperationsPage />, { wrapper: makeWrapper() });

    expect(screen.getByText(/ejecutadas: 3/i)).toBeInTheDocument();
    expect(screen.getByText(/fallidas: 1/i)).toBeInTheDocument();
  });
});
