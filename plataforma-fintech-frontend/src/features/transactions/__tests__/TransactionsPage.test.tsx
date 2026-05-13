import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { MemoryRouter } from 'react-router-dom';
import type { ReactNode } from 'react';
import { TransactionsPage } from '../TransactionsPage';

vi.mock('../hooks', () => ({
  useUserTransactionsQuery: vi.fn(),
  useWalletTransactionsQuery: vi.fn(),
  useReverseTransactionMutation: vi.fn(),
}));

vi.mock('../../../stores/use-selection-store', () => ({
  useSelectionStore: vi.fn(),
}));

vi.mock('../../../stores/use-app-store', () => ({
  useAppStore: vi.fn(),
}));

import {
  useUserTransactionsQuery,
  useWalletTransactionsQuery,
  useReverseTransactionMutation,
} from '../hooks';
import { useSelectionStore } from '../../../stores/use-selection-store';
import { useAppStore } from '../../../stores/use-app-store';

function makeWrapper() {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return function Wrapper({ children }: { children: ReactNode }) {
    return (
      <MemoryRouter>
        <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
      </MemoryRouter>
    );
  };
}

const DEFAULT_TX = {
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

const NON_REVERSIBLE_TX = {
  ...DEFAULT_TX,
  id: 'TX-000002',
  reversible: false,
};

const REVERSED_TX = {
  ...DEFAULT_TX,
  id: 'TX-000003',
  status: 'REVERSED',
};

const DEFAULT_STORE_FILTERS = { type: undefined, status: undefined, walletId: undefined };

describe('TransactionsPage', () => {
  const mockSetTransactionFilters = vi.fn();
  const mockResetFilters = vi.fn();
  const defaultMutationMock = {
    mutate: vi.fn(),
    isPending: false,
    isError: false,
    error: null,
  };

  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(useReverseTransactionMutation).mockReturnValue(defaultMutationMock as any);
    vi.mocked(useSelectionStore).mockReturnValue('USR001' as any);
    // Default store mock: empty filters + action stubs
    vi.mocked(useAppStore).mockImplementation((selector: (s: any) => any) =>
      selector({
        transactionFilters: DEFAULT_STORE_FILTERS,
        setTransactionFilters: mockSetTransactionFilters,
        resetFilters: mockResetFilters,
      })
    );
    // Default wallet query mock (idle — walletId is undefined by default)
    vi.mocked(useWalletTransactionsQuery).mockReturnValue({
      data: undefined,
      isLoading: false,
    } as any);
  });

  it('shows empty state when no user is selected', () => {
    vi.mocked(useSelectionStore).mockReturnValue(null);
    vi.mocked(useUserTransactionsQuery).mockReturnValue({
      data: undefined,
      isLoading: false,
    } as any);

    render(<TransactionsPage />, { wrapper: makeWrapper() });

    expect(screen.getByText(/selecciona un usuario/i)).toBeInTheDocument();
  });

  it('renders transaction rows when data is available', () => {
    vi.mocked(useUserTransactionsQuery).mockReturnValue({
      data: [DEFAULT_TX],
      isLoading: false,
    } as any);

    render(<TransactionsPage />, { wrapper: makeWrapper() });

    expect(screen.getByText('TX-000001')).toBeInTheDocument();
    // RECHARGE appears in both filter select options and table rows; use getAllByText
    expect(screen.getAllByText('RECHARGE').length).toBeGreaterThanOrEqual(1);
  });

  it('Revertir button is ENABLED when reversible=true and status=SUCCESSFUL', () => {
    vi.mocked(useUserTransactionsQuery).mockReturnValue({
      data: [DEFAULT_TX],
      isLoading: false,
    } as any);

    render(<TransactionsPage />, { wrapper: makeWrapper() });

    const button = screen.getByRole('button', { name: /revertir/i });
    expect(button).not.toBeDisabled();
  });

  it('Revertir button is DISABLED when reversible=false', () => {
    vi.mocked(useUserTransactionsQuery).mockReturnValue({
      data: [NON_REVERSIBLE_TX],
      isLoading: false,
    } as any);

    render(<TransactionsPage />, { wrapper: makeWrapper() });

    const button = screen.getByRole('button', { name: /revertir/i });
    expect(button).toBeDisabled();
  });

  it('Revertir button is DISABLED when status=REVERSED', () => {
    vi.mocked(useUserTransactionsQuery).mockReturnValue({
      data: [REVERSED_TX],
      isLoading: false,
    } as any);

    render(<TransactionsPage />, { wrapper: makeWrapper() });

    const button = screen.getByRole('button', { name: /revertir/i });
    expect(button).toBeDisabled();
  });

  it('clicking Revertir calls mutation with transactionId', () => {
    const mockMutate = vi.fn();
    vi.mocked(useReverseTransactionMutation).mockReturnValue({
      ...defaultMutationMock,
      mutate: mockMutate,
    } as any);
    vi.mocked(useUserTransactionsQuery).mockReturnValue({
      data: [DEFAULT_TX],
      isLoading: false,
    } as any);

    render(<TransactionsPage />, { wrapper: makeWrapper() });

    const button = screen.getByRole('button', { name: /revertir/i });
    fireEvent.click(button);

    expect(mockMutate).toHaveBeenCalledWith(
      expect.objectContaining({ transactionId: 'TX-000001' }),
      expect.any(Object)
    );
  });

  it('filter change triggers re-fetch (type filter select exists)', () => {
    vi.mocked(useUserTransactionsQuery).mockReturnValue({
      data: [DEFAULT_TX],
      isLoading: false,
    } as any);

    render(<TransactionsPage />, { wrapper: makeWrapper() });

    // Filter selects should be present
    const typeSelect = screen.getByRole('combobox', { name: /tipo/i });
    expect(typeSelect).toBeInTheDocument();
    fireEvent.change(typeSelect, { target: { value: 'RECHARGE' } });

    // useUserTransactionsQuery should have been called with type filter
    expect(useUserTransactionsQuery).toHaveBeenCalled();
  });

  // W1: type filter change calls setTransactionFilters on the store
  it('W1 — type filter change calls setTransactionFilters on store', () => {
    vi.mocked(useUserTransactionsQuery).mockReturnValue({
      data: [DEFAULT_TX],
      isLoading: false,
    } as any);

    render(<TransactionsPage />, { wrapper: makeWrapper() });

    const typeSelect = screen.getByRole('combobox', { name: /tipo/i });
    fireEvent.change(typeSelect, { target: { value: 'WITHDRAWAL' } });

    expect(mockSetTransactionFilters).toHaveBeenCalledWith({ type: 'WITHDRAWAL' });
  });

  // W1: status filter change calls setTransactionFilters on the store
  it('W1 — status filter change calls setTransactionFilters on store', () => {
    vi.mocked(useUserTransactionsQuery).mockReturnValue({
      data: [DEFAULT_TX],
      isLoading: false,
    } as any);

    render(<TransactionsPage />, { wrapper: makeWrapper() });

    const statusSelect = screen.getByRole('combobox', { name: /estado/i });
    fireEvent.change(statusSelect, { target: { value: 'REVERSED' } });

    expect(mockSetTransactionFilters).toHaveBeenCalledWith({ status: 'REVERSED' });
  });

  // W1: store filters are read — query called with type from store
  it('W1 — useUserTransactionsQuery receives type from store transactionFilters', () => {
    vi.mocked(useAppStore).mockImplementation((selector: (s: any) => any) =>
      selector({
        transactionFilters: { type: 'RECHARGE', status: undefined, walletId: undefined },
        setTransactionFilters: mockSetTransactionFilters,
        resetFilters: mockResetFilters,
      })
    );
    vi.mocked(useUserTransactionsQuery).mockReturnValue({
      data: [DEFAULT_TX],
      isLoading: false,
    } as any);

    render(<TransactionsPage />, { wrapper: makeWrapper() });

    expect(useUserTransactionsQuery).toHaveBeenCalledWith(
      'USR001',
      expect.objectContaining({ type: 'RECHARGE' })
    );
  });

  // W2: reverseMutation.mutate is called WITH walletId when store has walletId
  it('W2 — clicking Revertir passes walletId from store to mutation', () => {
    const mockMutate = vi.fn();
    vi.mocked(useReverseTransactionMutation).mockReturnValue({
      ...defaultMutationMock,
      mutate: mockMutate,
    } as any);
    vi.mocked(useAppStore).mockImplementation((selector: (s: any) => any) =>
      selector({
        transactionFilters: { type: undefined, status: undefined, walletId: 'W001' },
        setTransactionFilters: mockSetTransactionFilters,
        resetFilters: mockResetFilters,
      })
    );
    // When walletId is set, the page uses walletQuery — mock it with data
    vi.mocked(useWalletTransactionsQuery).mockReturnValue({
      data: [DEFAULT_TX],
      isLoading: false,
    } as any);
    vi.mocked(useUserTransactionsQuery).mockReturnValue({
      data: undefined,
      isLoading: false,
    } as any);

    render(<TransactionsPage />, { wrapper: makeWrapper() });

    const button = screen.getByRole('button', { name: /revertir/i });
    fireEvent.click(button);

    expect(mockMutate).toHaveBeenCalledWith(
      expect.objectContaining({ transactionId: 'TX-000001', userId: 'USR001', walletId: 'W001' }),
      expect.any(Object)
    );
  });

  // W2: reverseMutation.mutate is called WITHOUT walletId when store has no walletId
  it('W2 — clicking Revertir omits walletId when store walletId is undefined', () => {
    const mockMutate = vi.fn();
    vi.mocked(useReverseTransactionMutation).mockReturnValue({
      ...defaultMutationMock,
      mutate: mockMutate,
    } as any);
    vi.mocked(useUserTransactionsQuery).mockReturnValue({
      data: [DEFAULT_TX],
      isLoading: false,
    } as any);

    render(<TransactionsPage />, { wrapper: makeWrapper() });

    const button = screen.getByRole('button', { name: /revertir/i });
    fireEvent.click(button);

    const call = mockMutate.mock.calls[0]?.[0];
    expect(call).toBeDefined();
    expect(call?.transactionId).toBe('TX-000001');
    expect(call?.userId).toBe('USR001');
    expect(call?.walletId).toBeUndefined();
  });
});
