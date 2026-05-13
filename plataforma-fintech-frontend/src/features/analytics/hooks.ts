import { useQuery } from '@tanstack/react-query';
import {
  getAnalyticsSummary,
  getTopUsers,
  getTopWallets,
  getFrequentRoutes,
  getTopTransactions,
  getCycles,
  getTopWalletCategories,
  getMovementByType,
  getTotalMoved,
} from '../../api/analytics';
import { queryKeys } from '../../api/query-keys';

export function useAnalyticsSummaryQuery() {
  return useQuery({
    queryKey: queryKeys.analytics.summary,
    queryFn: () => getAnalyticsSummary(),
  });
}

export function useTopUsersQuery(limit: number = 10) {
  return useQuery({
    queryKey: [...queryKeys.analytics.topUsers, limit] as const,
    queryFn: () => getTopUsers(limit),
  });
}

export function useTopWalletsQuery(limit: number = 10) {
  return useQuery({
    queryKey: [...queryKeys.analytics.topWallets, limit] as const,
    queryFn: () => getTopWallets(limit),
  });
}

export function useFrequentRoutesQuery(minTransfers: number = 1) {
  return useQuery({
    queryKey: [...queryKeys.analytics.frequentRoutes, minTransfers] as const,
    queryFn: () => getFrequentRoutes(minTransfers),
  });
}

export function useTopTransactionsQuery(limit: number = 10) {
  return useQuery({
    queryKey: queryKeys.analytics.topTransactions(limit),
    queryFn: () => getTopTransactions(limit),
  });
}

export function useCyclesQuery() {
  return useQuery({
    queryKey: queryKeys.analytics.cycles,
    queryFn: () => getCycles(),
  });
}

export function useTopWalletCategoriesQuery(limit: number = 10) {
  return useQuery({
    queryKey: queryKeys.analytics.topWalletCategories(limit),
    queryFn: () => getTopWalletCategories(limit),
  });
}

export function useMovementByTypeQuery() {
  return useQuery({
    queryKey: queryKeys.analytics.movementByType,
    queryFn: () => getMovementByType(),
  });
}

export function useTotalMovedQuery(from: string, to: string) {
  return useQuery({
    queryKey: queryKeys.analytics.totalMoved(from, to),
    queryFn: () => getTotalMoved(from, to),
    enabled: from.length > 0 && to.length > 0,
  });
}
