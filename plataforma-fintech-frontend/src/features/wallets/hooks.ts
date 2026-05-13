import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { createWallet, listUserWallets } from '../../api/wallets';
import { queryKeys } from '../../api/query-keys';
import type { CreateWalletRequest } from '../../api/wallets';

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
