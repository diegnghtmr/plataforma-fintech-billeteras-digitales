import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { MemoryRouter } from 'react-router-dom';
import type { ReactNode } from 'react';
import { UsersPage } from '../UsersPage';

vi.mock('../hooks', () => ({
  useCreateUserMutation: vi.fn(),
  useUsersListQuery: vi.fn(),
  useUpdateUserMutation: vi.fn(),
  useDeleteUserMutation: vi.fn(),
}));

import {
  useCreateUserMutation,
  useUsersListQuery,
  useUpdateUserMutation,
  useDeleteUserMutation,
} from '../hooks';

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
    vi.mocked(useUsersListQuery).mockReturnValue({
      data: [],
      isLoading: false,
    } as any);

    render(<UsersPage />, { wrapper: makeWrapper() });

    expect(screen.queryByRole('textbox', { name: /id/i })).not.toBeInTheDocument();
    expect(screen.getByRole('textbox', { name: /nombre/i })).toBeInTheDocument();
    expect(screen.getByRole('textbox', { name: /email/i })).toBeInTheDocument();
  });

  it('shows skeleton cards while loading', () => {
    vi.mocked(useCreateUserMutation).mockReturnValue({
      mutate: vi.fn(),
      isPending: false,
      isSuccess: false,
      isError: false,
      error: null,
    } as any);
    vi.mocked(useUsersListQuery).mockReturnValue({
      data: undefined,
      isLoading: true,
    } as any);

    const { container } = render(<UsersPage />, { wrapper: makeWrapper() });

    expect(container.querySelectorAll('[aria-hidden="true"]').length).toBeGreaterThan(0);
  });

  it('shows empty state when no users exist', () => {
    vi.mocked(useCreateUserMutation).mockReturnValue({
      mutate: vi.fn(),
      isPending: false,
      isSuccess: false,
      isError: false,
      error: null,
    } as any);
    vi.mocked(useUsersListQuery).mockReturnValue({
      data: [],
      isLoading: false,
    } as any);

    render(<UsersPage />, { wrapper: makeWrapper() });

    expect(screen.getByText('No hay usuarios')).toBeInTheDocument();
  });

  it('shows list of users from useUsersListQuery', () => {
    vi.mocked(useCreateUserMutation).mockReturnValue({
      mutate: vi.fn(),
      isPending: false,
      isSuccess: false,
      isError: false,
      error: null,
    } as any);
    vi.mocked(useUsersListQuery).mockReturnValue({
      data: [MOCK_USER],
      isLoading: false,
    } as any);

    render(<UsersPage />, { wrapper: makeWrapper() });

    expect(screen.getByText('Juan Pérez')).toBeInTheDocument();
    expect(screen.getByText('USR001')).toBeInTheDocument();
    expect(screen.getByText('Bronce')).toBeInTheDocument();
  });

  it('filters list by search query', async () => {
    const user = userEvent.setup();
    vi.mocked(useCreateUserMutation).mockReturnValue({
      mutate: vi.fn(),
      isPending: false,
      isSuccess: false,
      isError: false,
      error: null,
    } as any);
    vi.mocked(useUsersListQuery).mockReturnValue({
      data: [
        MOCK_USER,
        { ...MOCK_USER, id: 'USR002', name: 'Ana López', email: 'ana@test.com' },
      ],
      isLoading: false,
    } as any);

    render(<UsersPage />, { wrapper: makeWrapper() });

    expect(screen.getByText('Juan Pérez')).toBeInTheDocument();
    expect(screen.getByText('Ana López')).toBeInTheDocument();

    await user.type(screen.getByRole('textbox', { name: /buscar/i }), 'ana');

    expect(screen.queryByText('Juan Pérez')).not.toBeInTheDocument();
    expect(screen.getByText('Ana López')).toBeInTheDocument();
  });

  it('shows empty search state when search returns no results', async () => {
    const user = userEvent.setup();
    vi.mocked(useCreateUserMutation).mockReturnValue({
      mutate: vi.fn(),
      isPending: false,
      isSuccess: false,
      isError: false,
      error: null,
    } as any);
    vi.mocked(useUsersListQuery).mockReturnValue({
      data: [MOCK_USER],
      isLoading: false,
    } as any);

    render(<UsersPage />, { wrapper: makeWrapper() });

    await user.type(screen.getByRole('textbox', { name: /buscar/i }), 'zzz');

    expect(screen.getByText('Sin resultados')).toBeInTheDocument();
    expect(screen.getByText(/probá con otro id o nombre/i)).toBeInTheDocument();
  });

  it('clicking a user card selects it and shows Editar / Eliminar buttons', () => {
    vi.mocked(useCreateUserMutation).mockReturnValue({
      mutate: vi.fn(), isPending: false, isSuccess: false, isError: false, error: null,
    } as any);
    vi.mocked(useUsersListQuery).mockReturnValue({
      data: [MOCK_USER],
      isLoading: false,
    } as any);

    render(<UsersPage />, { wrapper: makeWrapper() });

    fireEvent.click(screen.getByRole('button', { name: /juan pérez/i }));

    expect(screen.getByRole('button', { name: /^editar$/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /eliminar/i })).toBeInTheDocument();
  });

  it('clicking Editar shows the edit form', () => {
    vi.mocked(useCreateUserMutation).mockReturnValue({
      mutate: vi.fn(), isPending: false, isSuccess: false, isError: false, error: null,
    } as any);
    vi.mocked(useUsersListQuery).mockReturnValue({
      data: [MOCK_USER],
      isLoading: false,
    } as any);

    render(<UsersPage />, { wrapper: makeWrapper() });

    fireEvent.click(screen.getByRole('button', { name: /juan pérez/i }));
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
    vi.mocked(useCreateUserMutation).mockReturnValue({
      mutate: vi.fn(), isPending: false, isSuccess: false, isError: false, error: null,
    } as any);
    vi.mocked(useUsersListQuery).mockReturnValue({
      data: [MOCK_USER],
      isLoading: false,
    } as any);

    render(<UsersPage />, { wrapper: makeWrapper() });

    fireEvent.click(screen.getByRole('button', { name: /juan pérez/i }));
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
    vi.mocked(useUsersListQuery).mockReturnValue({
      data: [MOCK_USER],
      isLoading: false,
    } as any);

    render(<UsersPage />, { wrapper: makeWrapper() });

    fireEvent.click(screen.getByRole('button', { name: /juan pérez/i }));
    fireEvent.click(screen.getByRole('button', { name: /^eliminar$/i }));

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
      mutate: vi.fn(), isPending: false, isSuccess: false, isError: false, error: null,
    } as any);
    vi.mocked(useUsersListQuery).mockReturnValue({
      data: [MOCK_USER],
      isLoading: false,
    } as any);

    render(<UsersPage />, { wrapper: makeWrapper() });

    fireEvent.click(screen.getByRole('button', { name: /juan pérez/i }));
    await user.click(screen.getByRole('button', { name: /^eliminar$/i }));
    await user.click(screen.getByRole('button', { name: /confirmar eliminación/i }));

    expect(deleteFn).toHaveBeenCalledWith('USR001', expect.any(Object));
  });

  it('canceling the edit form hides the edit inputs', () => {
    vi.mocked(useCreateUserMutation).mockReturnValue({
      mutate: vi.fn(), isPending: false, isSuccess: false, isError: false, error: null,
    } as any);
    vi.mocked(useUsersListQuery).mockReturnValue({
      data: [MOCK_USER],
      isLoading: false,
    } as any);

    render(<UsersPage />, { wrapper: makeWrapper() });

    fireEvent.click(screen.getByRole('button', { name: /juan pérez/i }));
    fireEvent.click(screen.getByRole('button', { name: /^editar$/i }));
    expect(screen.getByPlaceholderText(/nuevo nombre/i)).toBeInTheDocument();

    fireEvent.click(screen.getByRole('button', { name: /cancelar edición/i }));
    expect(screen.queryByPlaceholderText(/nuevo nombre/i)).not.toBeInTheDocument();
  });

  it('displays API error message when create fails', () => {
    vi.mocked(useCreateUserMutation).mockReturnValue({
      mutate: vi.fn(),
      isPending: false,
      isSuccess: false,
      isError: true,
      error: { code: 'DUPLICATED_RESOURCE', message: 'User already exists' },
    } as any);
    vi.mocked(useUsersListQuery).mockReturnValue({
      data: [],
      isLoading: false,
    } as any);

    render(<UsersPage />, { wrapper: makeWrapper() });

    expect(screen.getByText(/user already exists/i)).toBeInTheDocument();
  });
});
