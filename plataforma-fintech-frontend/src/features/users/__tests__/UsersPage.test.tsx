import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { MemoryRouter } from 'react-router-dom';
import type { ReactNode } from 'react';
import { UsersPage } from '../UsersPage';

vi.mock('../hooks', () => ({
  useCreateUserMutation: vi.fn(),
  useUserQuery: vi.fn(),
  useUpdateUserMutation: vi.fn(),
  useDeleteUserMutation: vi.fn(),
}));

import { useCreateUserMutation, useUserQuery, useUpdateUserMutation, useDeleteUserMutation } from '../hooks';

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

const MOCK_USER = {
  id: 'USR001', name: 'Juan Pérez', email: 'juan@test.com',
  registeredAt: '2026-01-01T00:00:00Z', points: 0,
  loyaltyLevel: 'BRONZE' as const, walletCount: 0, totalBalance: 0,
};

describe('UsersPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(useUpdateUserMutation).mockReturnValue({
      mutate: vi.fn(),
      isPending: false,
      isSuccess: false,
      isError: false,
    } as any);
    vi.mocked(useDeleteUserMutation).mockReturnValue({
      mutate: vi.fn(),
      isPending: false,
      isSuccess: false,
      isError: false,
    } as any);
  });

  it('renders the create user form in empty state', () => {
    vi.mocked(useCreateUserMutation).mockReturnValue({
      mutate: vi.fn(),
      isPending: false,
      isSuccess: false,
      isError: false,
      error: null,
    } as any);
    vi.mocked(useUserQuery).mockReturnValue({
      data: undefined,
      isLoading: false,
    } as any);

    render(<UsersPage />, { wrapper: makeWrapper() });

    expect(screen.getByRole('textbox', { name: /id/i })).toBeInTheDocument();
    expect(screen.getByRole('textbox', { name: /nombre/i })).toBeInTheDocument();
    expect(screen.getByRole('textbox', { name: /email/i })).toBeInTheDocument();
  });

  it('shows user card after successful creation', () => {
    vi.mocked(useCreateUserMutation).mockReturnValue({
      mutate: vi.fn(),
      isPending: false,
      isSuccess: true,
      isError: false,
      error: null,
    } as any);
    vi.mocked(useUserQuery).mockReturnValue({
      data: MOCK_USER,
      isLoading: false,
    } as any);

    render(<UsersPage />, { wrapper: makeWrapper() });

    expect(screen.getByText('USR001')).toBeInTheDocument();
    expect(screen.getByText('Juan Pérez')).toBeInTheDocument();
    expect(screen.getByText('Bronce')).toBeInTheDocument();
  });

  it('displays API error message on error', () => {
    vi.mocked(useCreateUserMutation).mockReturnValue({
      mutate: vi.fn(),
      isPending: false,
      isSuccess: false,
      isError: true,
      error: { code: 'DUPLICATED_RESOURCE', message: 'User already exists' },
    } as any);
    vi.mocked(useUserQuery).mockReturnValue({
      data: undefined,
      isLoading: false,
    } as any);

    render(<UsersPage />, { wrapper: makeWrapper() });

    expect(screen.getByText(/user already exists/i)).toBeInTheDocument();
  });

  // W2 — new tests

  it('shows Editar and Eliminar buttons when user is loaded', () => {
    vi.mocked(useCreateUserMutation).mockReturnValue({
      mutate: vi.fn(), isPending: false, isSuccess: false, isError: false, error: null,
    } as any);
    vi.mocked(useUserQuery).mockReturnValue({ data: MOCK_USER, isLoading: false } as any);

    render(<UsersPage />, { wrapper: makeWrapper() });

    expect(screen.getByRole('button', { name: /^editar$/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /eliminar/i })).toBeInTheDocument();
  });

  it('clicking Editar shows the edit form', () => {
    vi.mocked(useCreateUserMutation).mockReturnValue({
      mutate: vi.fn(), isPending: false, isSuccess: false, isError: false, error: null,
    } as any);
    vi.mocked(useUserQuery).mockReturnValue({ data: MOCK_USER, isLoading: false } as any);

    render(<UsersPage />, { wrapper: makeWrapper() });

    fireEvent.click(screen.getByRole('button', { name: /^editar$/i }));

    expect(screen.getByPlaceholderText(/nuevo nombre/i)).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/nuevo email/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /guardar cambios/i })).toBeInTheDocument();
  });

  it('submit edit form calls useUpdateUserMutation.mutate with correct data', async () => {
    const user = userEvent.setup();
    const mutateFn = vi.fn();
    vi.mocked(useUpdateUserMutation).mockReturnValue({
      mutate: mutateFn, isPending: false, isSuccess: false, isError: false,
    } as any);
    // Simulate create mutation that fires onSuccess immediately to set createdUserId
    vi.mocked(useCreateUserMutation).mockReturnValue({
      mutate: vi.fn().mockImplementation((_data: unknown, opts: { onSuccess?: (r: typeof MOCK_USER) => void }) => {
        opts?.onSuccess?.(MOCK_USER);
      }),
      isPending: false, isSuccess: false, isError: false, error: null,
    } as any);
    vi.mocked(useUserQuery).mockReturnValue({ data: MOCK_USER, isLoading: false } as any);

    render(<UsersPage />, { wrapper: makeWrapper() });

    // Fill and submit create form so createdUserId is set via onSuccess callback
    await user.type(screen.getByRole('textbox', { name: /id/i }), 'USR001');
    await user.type(screen.getByRole('textbox', { name: /nombre/i }), 'Juan Pérez');
    await user.type(screen.getByRole('textbox', { name: /email/i }), 'juan@test.com');
    await user.click(screen.getByRole('button', { name: /crear usuario/i }));

    fireEvent.click(screen.getByRole('button', { name: /^editar$/i }));
    await user.type(screen.getByPlaceholderText(/nuevo nombre/i), 'Nuevo Nombre');
    await user.type(screen.getByPlaceholderText(/nuevo email/i), 'nuevo@test.com');
    await user.click(screen.getByRole('button', { name: /guardar cambios/i }));

    expect(mutateFn).toHaveBeenCalledWith(
      { userId: 'USR001', body: { name: 'Nuevo Nombre', email: 'nuevo@test.com' } },
      expect.any(Object)
    );
  });

  it('clicking Eliminar shows delete confirmation dialog', () => {
    vi.mocked(useCreateUserMutation).mockReturnValue({
      mutate: vi.fn(), isPending: false, isSuccess: false, isError: false, error: null,
    } as any);
    vi.mocked(useUserQuery).mockReturnValue({ data: MOCK_USER, isLoading: false } as any);

    render(<UsersPage />, { wrapper: makeWrapper() });

    fireEvent.click(screen.getByRole('button', { name: /eliminar/i }));

    expect(screen.getByRole('button', { name: /confirmar eliminación/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /cancelar/i })).toBeInTheDocument();
  });

  it('confirming delete calls useDeleteUserMutation.mutate with userId', async () => {
    const user = userEvent.setup();
    const deleteFn = vi.fn();
    vi.mocked(useDeleteUserMutation).mockReturnValue({
      mutate: deleteFn, isPending: false, isSuccess: false, isError: false,
    } as any);
    vi.mocked(useCreateUserMutation).mockReturnValue({
      mutate: vi.fn().mockImplementation((_data: unknown, opts: { onSuccess?: (r: typeof MOCK_USER) => void }) => {
        opts?.onSuccess?.(MOCK_USER);
      }),
      isPending: false, isSuccess: false, isError: false, error: null,
    } as any);
    vi.mocked(useUserQuery).mockReturnValue({ data: MOCK_USER, isLoading: false } as any);

    render(<UsersPage />, { wrapper: makeWrapper() });

    // Fill and submit create form so createdUserId is set to 'USR001'
    await user.type(screen.getByRole('textbox', { name: /id/i }), 'USR001');
    await user.type(screen.getByRole('textbox', { name: /nombre/i }), 'Juan Pérez');
    await user.type(screen.getByRole('textbox', { name: /email/i }), 'juan@test.com');
    await user.click(screen.getByRole('button', { name: /crear usuario/i }));

    await user.click(screen.getByRole('button', { name: /^eliminar$/i }));
    await user.click(screen.getByRole('button', { name: /confirmar eliminación/i }));

    expect(deleteFn).toHaveBeenCalledWith('USR001', expect.any(Object));
  });

  it('canceling the edit form hides the edit inputs', () => {
    vi.mocked(useCreateUserMutation).mockReturnValue({
      mutate: vi.fn(), isPending: false, isSuccess: false, isError: false, error: null,
    } as any);
    vi.mocked(useUserQuery).mockReturnValue({ data: MOCK_USER, isLoading: false } as any);

    render(<UsersPage />, { wrapper: makeWrapper() });

    fireEvent.click(screen.getByRole('button', { name: /^editar$/i }));
    expect(screen.getByPlaceholderText(/nuevo nombre/i)).toBeInTheDocument();

    fireEvent.click(screen.getByRole('button', { name: /cancelar edición/i }));
    expect(screen.queryByPlaceholderText(/nuevo nombre/i)).not.toBeInTheDocument();
  });
});
