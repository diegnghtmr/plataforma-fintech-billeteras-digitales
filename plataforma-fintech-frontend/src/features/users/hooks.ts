import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { createUser, getUserById, listUsers, updateUser, deleteUser } from '../../api/users';
import { queryKeys } from '../../api/query-keys';
import type { CreateUserRequest, UpdateUserRequest } from '../../api/users';

export function useUsersListQuery() {
  return useQuery({
    queryKey: [...queryKeys.users.all, 'list'] as const,
    queryFn: listUsers,
  });
}

export function useUserQuery(userId: string | undefined) {
  return useQuery({
    queryKey: queryKeys.users.detail(userId ?? ''),
    queryFn: () => getUserById(userId!),
    enabled: userId !== undefined,
  });
}

export function useCreateUserMutation() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (payload: CreateUserRequest) => createUser(payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.users.all });
    },
  });
}

export function useUpdateUserMutation() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ userId, body }: { userId: string; body: UpdateUserRequest }) =>
      updateUser(userId, body),
    onSuccess: (_data, { userId }) => {
      queryClient.invalidateQueries({ queryKey: queryKeys.users.all });
      queryClient.invalidateQueries({ queryKey: queryKeys.users.detail(userId) });
    },
  });
}

export function useDeleteUserMutation() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (userId: string) => deleteUser(userId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.users.all });
    },
  });
}
