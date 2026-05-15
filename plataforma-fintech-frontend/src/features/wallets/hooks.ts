import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { createWallet, listUserWallets, updateWallet } from '../../api/wallets';
import { queryKeys } from '../../api/query-keys';
import type { CreateWalletRequest, UpdateWalletRequest } from '../../api/wallets';

export function useUserWalletsQuery(userId: string | undefined) {
  return useQuery({
    queryKey: queryKeys.wallets.byUser(userId ?? ''),
    queryFn: () => listUserWallets(userId!),
    enabled: userId !== undefined,
  });
}

interface CreateWalletPayload extends CreateWalletRequest {
  userId: string;
}

export function useCreateWalletMutation() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ userId, ...payload }: CreateWalletPayload) =>
      createWallet(userId, payload),
    onSuccess: (_data, variables) => {
      const { userId } = variables;
      queryClient.invalidateQueries({ queryKey: queryKeys.wallets.byUser(userId) });
      queryClient.invalidateQueries({ queryKey: queryKeys.users.detail(userId) });
    },
  });
}

interface UpdateWalletPayload extends UpdateWalletRequest {
  userId: string;
  walletId: string;
}

export function useUpdateWalletMutation() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ userId, walletId, ...payload }: UpdateWalletPayload) =>
      updateWallet(userId, walletId, payload),
    onSuccess: (_data, variables) => {
      const { userId } = variables;
      queryClient.invalidateQueries({ queryKey: queryKeys.wallets.byUser(userId) });
      queryClient.invalidateQueries({ queryKey: queryKeys.users.detail(userId) });
    },
  });
}
