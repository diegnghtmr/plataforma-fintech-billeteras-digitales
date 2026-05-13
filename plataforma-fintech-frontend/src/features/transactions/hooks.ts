import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  getUserTransactions,
  getWalletTransactions,
  reverseTransaction,
} from '../../api/transactions';
import type { GetUserTransactionsFilters } from '../../api/transactions';
import { queryKeys } from '../../api/query-keys';

// ── useUserTransactionsQuery ──────────────────────────────────────────────────

export function useUserTransactionsQuery(
  userId: string,
  filters?: GetUserTransactionsFilters
) {
  return useQuery({
    queryKey: queryKeys.transactions.byUser(userId, filters as Record<string, unknown> | undefined),
    queryFn: () => getUserTransactions(userId, filters),
    enabled: Boolean(userId),
  });
}

// ── useWalletTransactionsQuery ────────────────────────────────────────────────

export function useWalletTransactionsQuery(userId: string, walletId: string) {
  return useQuery({
    queryKey: queryKeys.transactions.byWallet(userId, walletId),
    queryFn: () => getWalletTransactions(userId, walletId),
    enabled: Boolean(userId) && Boolean(walletId),
  });
}

// ── useReverseTransactionMutation ─────────────────────────────────────────────

interface ReversePayload {
  transactionId: string;
  userId: string;
  walletId?: string;
}

export function useReverseTransactionMutation() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ transactionId }: ReversePayload) => reverseTransaction(transactionId),
    onSuccess: (_data, variables) => {
      const { userId, walletId } = variables;

      // Invalidate 4 query keys per spec S10
      queryClient.invalidateQueries({
        queryKey: queryKeys.transactions.byUser(userId),
      });
      if (walletId) {
        queryClient.invalidateQueries({
          queryKey: queryKeys.transactions.byWallet(userId, walletId),
        });
      }
      queryClient.invalidateQueries({
        queryKey: queryKeys.wallets.byUser(userId),
      });
      queryClient.invalidateQueries({
        queryKey: queryKeys.users.detail(userId),
      });
      // T07-F09: points invalidations after reversal
      queryClient.invalidateQueries({
        queryKey: queryKeys.points.byUser(userId),
      });
      queryClient.invalidateQueries({
        predicate: (q) => q.queryKey[0] === 'points' && q.queryKey[1] === 'ranking',
      });
    },
  });
}
