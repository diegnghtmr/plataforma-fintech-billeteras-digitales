import { useMutation, useQueryClient } from '@tanstack/react-query';
import { recharge, withdraw, internalTransfer, externalTransfer } from '../../api/operations';
import { queryKeys } from '../../api/query-keys';
import type {
  MoneyOperationRequest,
  InternalTransferRequest,
  ExternalTransferRequest,
} from '../../api/operations';

// ── Recharge ──────────────────────────────────────────────────────────────────

interface RechargePayload extends MoneyOperationRequest {
  userId: string;
  walletId: string;
}

export function useRechargeMutation() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ userId, walletId, ...body }: RechargePayload) =>
      recharge(userId, walletId, body),
    onSuccess: (_data, variables) => {
      const { userId } = variables;
      queryClient.invalidateQueries({ queryKey: queryKeys.wallets.byUser(userId) });
      queryClient.invalidateQueries({ queryKey: queryKeys.users.detail(userId) });
      queryClient.invalidateQueries({ queryKey: queryKeys.points.byUser(userId) });
      queryClient.invalidateQueries({
        predicate: (q) => q.queryKey[0] === 'points' && q.queryKey[1] === 'ranking',
      });
      queryClient.invalidateQueries({
        predicate: (q) => q.queryKey[0] === 'analytics' || q.queryKey[0] === 'fraud',
      });
    },
  });
}

// ── Withdraw ──────────────────────────────────────────────────────────────────

interface WithdrawPayload extends MoneyOperationRequest {
  userId: string;
  walletId: string;
}

export function useWithdrawMutation() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ userId, walletId, ...body }: WithdrawPayload) =>
      withdraw(userId, walletId, body),
    onSuccess: (_data, variables) => {
      const { userId } = variables;
      queryClient.invalidateQueries({ queryKey: queryKeys.wallets.byUser(userId) });
      queryClient.invalidateQueries({ queryKey: queryKeys.users.detail(userId) });
      queryClient.invalidateQueries({ queryKey: queryKeys.points.byUser(userId) });
      queryClient.invalidateQueries({
        predicate: (q) => q.queryKey[0] === 'points' && q.queryKey[1] === 'ranking',
      });
      queryClient.invalidateQueries({
        predicate: (q) => q.queryKey[0] === 'analytics' || q.queryKey[0] === 'fraud',
      });
    },
  });
}

// ── Internal Transfer ─────────────────────────────────────────────────────────

interface InternalTransferPayload extends InternalTransferRequest {
  userId: string;
}

export function useInternalTransferMutation() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ userId, ...body }: InternalTransferPayload) =>
      internalTransfer(userId, body),
    onSuccess: (_data, variables) => {
      const { userId } = variables;
      queryClient.invalidateQueries({ queryKey: queryKeys.wallets.byUser(userId) });
      queryClient.invalidateQueries({ queryKey: queryKeys.users.detail(userId) });
      queryClient.invalidateQueries({ queryKey: queryKeys.points.byUser(userId) });
      queryClient.invalidateQueries({
        predicate: (q) => q.queryKey[0] === 'points' && q.queryKey[1] === 'ranking',
      });
      queryClient.invalidateQueries({
        predicate: (q) => q.queryKey[0] === 'analytics' || q.queryKey[0] === 'fraud',
      });
    },
  });
}

// ── External Transfer ─────────────────────────────────────────────────────────

export function useExternalTransferMutation() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (body: ExternalTransferRequest) => externalTransfer(body),
    onSuccess: (_data, variables) => {
      const { sourceUserId, targetUserId } = variables;
      // Invalidate source user
      queryClient.invalidateQueries({ queryKey: queryKeys.wallets.byUser(sourceUserId) });
      queryClient.invalidateQueries({ queryKey: queryKeys.users.detail(sourceUserId) });
      // Invalidate target user
      queryClient.invalidateQueries({ queryKey: queryKeys.wallets.byUser(targetUserId) });
      queryClient.invalidateQueries({ queryKey: queryKeys.users.detail(targetUserId) });
      // Points: only sourceUserId receives points on EXTERNAL_TRANSFER_SENT
      queryClient.invalidateQueries({ queryKey: queryKeys.points.byUser(sourceUserId) });
      queryClient.invalidateQueries({
        predicate: (q) => q.queryKey[0] === 'points' && q.queryKey[1] === 'ranking',
      });
      queryClient.invalidateQueries({
        predicate: (q) => q.queryKey[0] === 'analytics' || q.queryKey[0] === 'fraud',
      });
    },
  });
}
