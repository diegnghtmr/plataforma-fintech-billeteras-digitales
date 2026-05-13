import { describe, it, expect, vi, beforeEach } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import type { ReactNode } from 'react';
import { useUserQuery, useUsersListQuery, useCreateUserMutation, useUpdateUserMutation, useDeleteUserMutation } from '../hooks';

// Mock the API module
vi.mock('../../../api/users', () => ({
  getUserById: vi.fn(),
  listUsers: vi.fn(),
  createUser: vi.fn(),
  updateUser: vi.fn(),
  deleteUser: vi.fn(),
}));

import { getUserById, listUsers, createUser, updateUser, deleteUser } from '../../../api/users';

function makeWrapper() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  });
  return function Wrapper({ children }: { children: ReactNode }) {
    return <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>;
  };
}

describe('useUserQuery', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('is disabled when userId is undefined', () => {
    const { result } = renderHook(() => useUserQuery(undefined), {
      wrapper: makeWrapper(),
    });
    expect(result.current.fetchStatus).toBe('idle');
    expect(result.current.status).toBe('pending');
  });

  it('fetches when userId is defined', async () => {
    const mockUser = {
      id: 'USR001', name: 'Juan', email: 'juan@test.com',
      registeredAt: '2026-01-01T00:00:00Z', points: 0,
      loyaltyLevel: 'BRONZE' as const, walletCount: 0, totalBalance: 0,
    };
    vi.mocked(getUserById).mockResolvedValue(mockUser);

    const { result } = renderHook(() => useUserQuery('USR001'), {
      wrapper: makeWrapper(),
    });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(result.current.data).toEqual(mockUser);
  });

  it('error state parses ApiError', async () => {
    const apiError = { code: 'USER_NOT_FOUND', message: 'not found' };
    vi.mocked(getUserById).mockRejectedValue(apiError);

    const { result } = renderHook(() => useUserQuery('UNKNOWN'), {
      wrapper: makeWrapper(),
    });

    await waitFor(() => expect(result.current.isError).toBe(true));
    expect(result.current.error).toEqual(apiError);
  });
});

describe('useUsersListQuery', () => {
  beforeEach(() => { vi.clearAllMocks(); });

  it('fetches and returns the list of users', async () => {
    const mockUsers = [
      { id: 'USR001', name: 'Juan', email: 'juan@test.com', registeredAt: '2026-01-01T00:00:00Z', points: 0, loyaltyLevel: 'BRONZE' as const, walletCount: 0, totalBalance: 0 },
    ];
    vi.mocked(listUsers).mockResolvedValue(mockUsers);

    const { result } = renderHook(() => useUsersListQuery(), { wrapper: makeWrapper() });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(result.current.data).toEqual(mockUsers);
  });
});

describe('useCreateUserMutation', () => {
  it('exists and returns mutation object', () => {
    const { result } = renderHook(() => useCreateUserMutation(), {
      wrapper: makeWrapper(),
    });
    expect(result.current.mutate).toBeDefined();
    expect(result.current.mutateAsync).toBeDefined();
  });

  it('exists with mutate function', () => {
    const { result } = renderHook(() => useCreateUserMutation(), {
      wrapper: makeWrapper(),
    });
    expect(result.current.mutate).toBeDefined();
  });

  it('calls createUser and invalidates on success', async () => {
    const mockUser = {
      id: 'USR001', name: 'Juan', email: 'juan@test.com',
      registeredAt: '2026-01-01T00:00:00Z', points: 0,
      loyaltyLevel: 'BRONZE' as const, walletCount: 0, totalBalance: 0,
    };
    vi.mocked(createUser).mockResolvedValue(mockUser);

    const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
    const invalidateSpy = vi.spyOn(queryClient, 'invalidateQueries');

    const wrapper = ({ children }: { children: ReactNode }) => (
      <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
    );

    const { result } = renderHook(() => useCreateUserMutation(), { wrapper });
    result.current.mutate({ id: 'USR001', name: 'Juan', email: 'juan@test.com' });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(invalidateSpy).toHaveBeenCalledWith(expect.objectContaining({
      queryKey: ['users'],
    }));
  });
});

describe('useUpdateUserMutation', () => {
  beforeEach(() => { vi.clearAllMocks(); });

  it('exists and returns mutation object', () => {
    const { result } = renderHook(() => useUpdateUserMutation(), {
      wrapper: makeWrapper(),
    });
    expect(result.current.mutate).toBeDefined();
  });

  it('calls updateUser and invalidates on success', async () => {
    const mockUser = {
      id: 'USR001', name: 'Updated', email: 'updated@test.com',
      registeredAt: '2026-01-01T00:00:00Z', points: 0,
      loyaltyLevel: 'BRONZE' as const, walletCount: 0, totalBalance: 0,
    };
    vi.mocked(updateUser).mockResolvedValue(mockUser);

    const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
    const invalidateSpy = vi.spyOn(queryClient, 'invalidateQueries');

    const wrapper = ({ children }: { children: ReactNode }) => (
      <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
    );

    const { result } = renderHook(() => useUpdateUserMutation(), { wrapper });
    result.current.mutate({ userId: 'USR001', body: { name: 'Updated' } });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(invalidateSpy).toHaveBeenCalledWith(expect.objectContaining({
      queryKey: ['users'],
    }));
  });
});

describe('useDeleteUserMutation', () => {
  beforeEach(() => { vi.clearAllMocks(); });

  it('exists and returns mutation object', () => {
    const { result } = renderHook(() => useDeleteUserMutation(), {
      wrapper: makeWrapper(),
    });
    expect(result.current.mutate).toBeDefined();
  });

  it('calls deleteUser and invalidates on success', async () => {
    vi.mocked(deleteUser).mockResolvedValue(undefined);

    const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
    const invalidateSpy = vi.spyOn(queryClient, 'invalidateQueries');

    const wrapper = ({ children }: { children: ReactNode }) => (
      <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
    );

    const { result } = renderHook(() => useDeleteUserMutation(), { wrapper });
    result.current.mutate('USR001');

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(invalidateSpy).toHaveBeenCalledWith(expect.objectContaining({
      queryKey: ['users'],
    }));
  });
});
