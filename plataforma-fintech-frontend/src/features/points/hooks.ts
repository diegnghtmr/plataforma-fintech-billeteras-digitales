import { useQuery } from '@tanstack/react-query';
import { getUserPoints, getPointsRanking } from '../../api/points';
import { queryKeys } from '../../api/query-keys';

/**
 * Query hook for a specific user's current points and loyalty level.
 * Disabled when userId is undefined.
 */
export function useUserPointsQuery(userId: string | undefined) {
  return useQuery({
    queryKey: queryKeys.points.byUser(userId ?? ''),
    queryFn: () => getUserPoints(userId!),
    enabled: userId !== undefined,
  });
}

/**
 * Query hook for the points ranking.
 * limit defaults to 10 if not provided.
 */
export function usePointsRankingQuery(limit?: number) {
  return useQuery({
    queryKey: queryKeys.points.ranking(limit),
    queryFn: () => getPointsRanking(limit),
  });
}
