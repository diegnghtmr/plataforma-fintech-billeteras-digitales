import { apiClient, extractApiError } from './index';
import type { components } from './generated/schema';

export type AnalyticsSummaryResponse = components['schemas']['AnalyticsSummaryResponse'];
export type MetricItemResponse = components['schemas']['MetricItemResponse'];
export type RouteMetricResponse = components['schemas']['RouteMetricResponse'];
export type TransactionResponse = components['schemas']['TransactionResponse'];
export type RangeTotalResponse = components['schemas']['RangeTotalResponse'];

export async function getAnalyticsSummary(): Promise<AnalyticsSummaryResponse> {
  const { data, error, response } = await apiClient.GET('/analytics/summary');

  if (error !== undefined || !data) {
    const apiError = await extractApiError(response);
    throw apiError ?? new Error('Unknown error fetching analytics summary');
  }

  return data;
}

export async function getTopUsers(limit?: number): Promise<MetricItemResponse[]> {
  const { data, error, response } = await apiClient.GET('/analytics/top-users', {
    params: { query: { limit: limit ?? 10 } },
  });

  if (error !== undefined || !data) {
    const apiError = await extractApiError(response);
    throw apiError ?? new Error('Unknown error fetching top users');
  }

  return data;
}

export async function getTopWallets(limit?: number): Promise<MetricItemResponse[]> {
  const { data, error, response } = await apiClient.GET('/analytics/top-wallets', {
    params: { query: { limit: limit ?? 10 } },
  });

  if (error !== undefined || !data) {
    const apiError = await extractApiError(response);
    throw apiError ?? new Error('Unknown error fetching top wallets');
  }

  return data;
}

export async function getFrequentRoutes(minTransfers?: number): Promise<RouteMetricResponse[]> {
  const { data, error, response } = await apiClient.GET('/analytics/frequent-routes', {
    params: { query: { minTransfers: minTransfers ?? 1 } },
  });

  if (error !== undefined || !data) {
    const apiError = await extractApiError(response);
    throw apiError ?? new Error('Unknown error fetching frequent routes');
  }

  return data;
}

export async function getTopTransactions(limit?: number): Promise<TransactionResponse[]> {
  const { data, error, response } = await apiClient.GET('/analytics/top-transactions', {
    params: { query: { limit: limit ?? 10 } },
  });

  if (error !== undefined || !data) {
    const apiError = await extractApiError(response);
    throw apiError ?? new Error('Unknown error fetching top transactions');
  }

  return data;
}

export async function getCycles(): Promise<string[][]> {
  const { data, error, response } = await apiClient.GET('/analytics/cycles');

  if (error !== undefined || !data) {
    const apiError = await extractApiError(response);
    throw apiError ?? new Error('Unknown error fetching cycles');
  }

  return data as string[][];
}

export async function getTopWalletCategories(limit?: number): Promise<MetricItemResponse[]> {
  const { data, error, response } = await apiClient.GET('/analytics/top-wallet-categories', {
    params: { query: { limit: limit ?? 10 } },
  });

  if (error !== undefined || !data) {
    const apiError = await extractApiError(response);
    throw apiError ?? new Error('Unknown error fetching top wallet categories');
  }

  return data;
}

export async function getMovementByType(): Promise<MetricItemResponse[]> {
  const { data, error, response } = await apiClient.GET('/analytics/movement-by-type');

  if (error !== undefined || !data) {
    const apiError = await extractApiError(response);
    throw apiError ?? new Error('Unknown error fetching movement by type');
  }

  return data;
}

export async function getTotalMoved(from: string, to: string): Promise<RangeTotalResponse> {
  const { data, error, response } = await apiClient.GET('/analytics/total-moved', {
    params: { query: { from, to } },
  });

  if (error !== undefined || !data) {
    const apiError = await extractApiError(response);
    throw apiError ?? new Error('Unknown error fetching total moved');
  }

  return data;
}
