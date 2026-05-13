import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { MemoryRouter } from 'react-router-dom';
import type { ReactNode } from 'react';
import { OperationsPage } from '../OperationsPage';

vi.mock('../hooks', () => ({
  useRechargeMutation: vi.fn(),
  useWithdrawMutation: vi.fn(),
  useInternalTransferMutation: vi.fn(),
  useExternalTransferMutation: vi.fn(),
}));

vi.mock('../../../stores/use-selection-store', () => ({
  useSelectionStore: vi.fn(),
}));

import {
  useRechargeMutation,
  useWithdrawMutation,
  useInternalTransferMutation,
  useExternalTransferMutation,
} from '../hooks';
import { useSelectionStore } from '../../../stores/use-selection-store';

const INSUFFICIENT_FUNDS_ERROR = {
  code: 'INSUFFICIENT_FUNDS',
  message: 'Saldo insuficiente',
};

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

const defaultMutationMock = {
  mutate: vi.fn(),
  isPending: false,
  isError: false,
  error: null,
};

describe('OperationsPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(useRechargeMutation).mockReturnValue(defaultMutationMock as any);
    vi.mocked(useWithdrawMutation).mockReturnValue(defaultMutationMock as any);
    vi.mocked(useInternalTransferMutation).mockReturnValue(defaultMutationMock as any);
    vi.mocked(useExternalTransferMutation).mockReturnValue(defaultMutationMock as any);
  });

  it('shows message when no user is selected', () => {
    vi.mocked(useSelectionStore).mockReturnValue(null);

    render(<OperationsPage />, { wrapper: makeWrapper() });

    expect(screen.getByText(/elegí un usuario/i)).toBeInTheDocument();
  });

  it('renders all four operation forms when user is selected', () => {
    vi.mocked(useSelectionStore).mockImplementation((selector: any) => {
      const state = { selectedUserId: 'USR001', selectedWalletId: 'W001' };
      return selector(state);
    });

    render(<OperationsPage />, { wrapper: makeWrapper() });

    // Check that tab navigation is visible (tabs are role="button")
    expect(screen.getByRole('button', { name: /^recarga$/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /retiro/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /interna/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /externa/i })).toBeInTheDocument();
  });

  it('calls recharge mutation on submit with pre-filled userId and walletId', async () => {
    const mockMutate = vi.fn();
    vi.mocked(useRechargeMutation).mockReturnValue({
      ...defaultMutationMock,
      mutate: mockMutate,
    } as any);
    vi.mocked(useSelectionStore).mockImplementation((selector: any) => {
      const state = { selectedUserId: 'USR001', selectedWalletId: 'W001' };
      return selector(state);
    });

    render(<OperationsPage />, { wrapper: makeWrapper() });

    const amountInput = screen.getByTestId('recharge-amount');
    fireEvent.change(amountInput, { target: { value: '200' } });
    fireEvent.submit(screen.getByTestId('recharge-form'));

    await waitFor(() => {
      expect(mockMutate).toHaveBeenCalledWith(
        expect.objectContaining({ userId: 'USR001', walletId: 'W001', amount: 200 }),
        expect.any(Object)
      );
    });
  });

  it('displays Spanish error message for INSUFFICIENT_FUNDS', () => {
    vi.mocked(useRechargeMutation).mockReturnValue({
      ...defaultMutationMock,
      isError: true,
      error: INSUFFICIENT_FUNDS_ERROR,
    } as any);
    vi.mocked(useSelectionStore).mockImplementation((selector: any) => {
      const state = { selectedUserId: 'USR001', selectedWalletId: 'W001' };
      return selector(state);
    });

    render(<OperationsPage />, { wrapper: makeWrapper() });

    expect(screen.getByText(/saldo insuficiente/i)).toBeInTheDocument();
  });
});
