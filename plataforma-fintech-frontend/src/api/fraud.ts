import { apiClient, extractApiError } from './index';
import type { components } from './generated/schema';

export type FraudEventResponse = components['schemas']['FraudEventResponse'];
export type FraudSeverity = components['schemas']['FraudSeverity'];

export interface FraudFilters {
  userId?: string;
  severity?: FraudSeverity;
}

export async function getFraudEvents(filters?: FraudFilters): Promise<FraudEventResponse[]> {
  const { data, error, response } = await apiClient.GET('/fraud/events', {
    params: {
      query: {
        ...(filters?.userId !== undefined ? { userId: filters.userId } : {}),
        ...(filters?.severity !== undefined ? { severity: filters.severity } : {}),
      },
    },
  });

  if (error !== undefined || !data) {
    const apiError = await extractApiError(response);
    throw apiError ?? new Error('Unknown error fetching fraud events');
  }

  return data;
}
