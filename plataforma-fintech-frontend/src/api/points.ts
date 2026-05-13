import { apiClient, extractApiError } from './index';
import type { components } from './generated/schema';

export type PointsResponse = components['schemas']['PointsResponse'];
export type RankingItemResponse = components['schemas']['RankingItemResponse'];

export async function getUserPoints(userId: string): Promise<PointsResponse> {
  const { data, error, response } = await apiClient.GET('/users/{userId}/points', {
    params: { path: { userId } },
  });

  if (error !== undefined || !data) {
    const apiError = await extractApiError(response);
    throw apiError ?? new Error('Unknown error fetching user points');
  }

  return data;
}

export async function getPointsRanking(limit?: number): Promise<RankingItemResponse[]> {
  const { data, error, response } = await apiClient.GET('/points/ranking', {
    params: { query: { limit: limit ?? 10 } },
  });

  if (error !== undefined || !data) {
    const apiError = await extractApiError(response);
    throw apiError ?? new Error('Unknown error fetching points ranking');
  }

  return data;
}
