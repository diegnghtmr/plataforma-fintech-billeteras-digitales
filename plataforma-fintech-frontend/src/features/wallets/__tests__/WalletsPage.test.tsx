import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { MemoryRouter } from 'react-router-dom';
import type { ReactNode } from 'react';
import { WalletsPage } from '../WalletsPage';

vi.mock('../hooks', () => ({
  useUserWalletsQuery: vi.fn(),
  useCreateWalletMutation: vi.fn(),
}));

vi.mock('../../../stores/use-selection-store', () => ({
  useSelectionStore: vi.fn(),
}));

import { useUserWalletsQuery, useCreateWalletMutation } from '../hooks';
import { useSelectionStore } from '../../../stores/use-selection-store';

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

describe('WalletsPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(useCreateWalletMutation).mockReturnValue({
      mutate: vi.fn(),
      isPending: false,
      isError: false,
      error: null,
    } as any);
  });

  it('shows empty state hint with link when no selectedUserId', () => {
    vi.mocked(useSelectionStore).mockReturnValue(null);
    vi.mocked(useUserWalletsQuery).mockReturnValue({
      data: undefined,
      isLoading: false,
    } as any);

    render(<WalletsPage />, { wrapper: makeWrapper() });

    expect(screen.getByRole('link', { name: /usuarios/i })).toBeInTheDocument();
  });

  it('renders wallet list when selectedUserId is set', () => {
    vi.mocked(useSelectionStore).mockReturnValue('USR001' as any);
    const mockWallets = [
      {
        code: 'W001', name: 'Ahorros', type: 'SAVINGS',
        ownerId: 'USR001', balance: 500, active: true,
        createdAt: '2026-01-01T00:00:00Z', transactionCount: 0,
      },
    ];
    vi.mocked(useUserWalletsQuery).mockReturnValue({
      data: mockWallets,
      isLoading: false,
    } as any);

    render(<WalletsPage />, { wrapper: makeWrapper() });

    expect(screen.getByText('W001')).toBeInTheDocument();
    expect(screen.getAllByText('Ahorros').length).toBeGreaterThan(0);
  });

  it('renders "Seleccionar" button per wallet card', () => {
    vi.mocked(useSelectionStore).mockReturnValue('USR001' as any);
    const mockWallets = [
      {
        code: 'W001', name: 'Ahorros', type: 'SAVINGS',
        ownerId: 'USR001', balance: 500, active: true,
        createdAt: '2026-01-01T00:00:00Z', transactionCount: 0,
      },
    ];
    vi.mocked(useUserWalletsQuery).mockReturnValue({
      data: mockWallets,
      isLoading: false,
    } as any);

    render(<WalletsPage />, { wrapper: makeWrapper() });

    expect(screen.getByRole('button', { name: /seleccionar/i })).toBeInTheDocument();
  });
});
