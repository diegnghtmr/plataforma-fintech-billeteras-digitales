import { apiClient, extractApiError } from './index';
import type { components } from './generated/schema';

export type CreateScheduledOperationRequest =
  components['schemas']['CreateScheduledOperationRequest'];
export type ScheduledOperationResponse =
  components['schemas']['ScheduledOperationResponse'];
export type ExecutionReportResponse =
  components['schemas']['ExecutionReportResponse'];

export async function getScheduledOperations(): Promise<ScheduledOperationResponse[]> {
  const { data, error, response } = await apiClient.GET('/scheduled-operations');

  if (error !== undefined || !data) {
    const apiError = await extractApiError(response);
    throw apiError ?? new Error('Unknown error fetching scheduled operations');
  }

  return data;
}

export async function createScheduledOperation(
  body: CreateScheduledOperationRequest
): Promise<ScheduledOperationResponse> {
  const { data, error, response } = await apiClient.POST('/scheduled-operations', { body });

  if (error !== undefined || !data) {
    const apiError = await extractApiError(response);
    throw apiError ?? new Error('Unknown error creating scheduled operation');
  }

  return data;
}

export async function cancelScheduledOperation(
  operationId: string
): Promise<ScheduledOperationResponse> {
  const { data, error, response } = await apiClient.POST(
    '/scheduled-operations/{operationId}/cancel',
    { params: { path: { operationId } } }
  );

  if (error !== undefined || !data) {
    const apiError = await extractApiError(response);
    throw apiError ?? new Error('Unknown error cancelling scheduled operation');
  }

  return data;
}

export async function runScheduledOps(): Promise<ExecutionReportResponse> {
  const { data, error, response } = await apiClient.POST('/scheduled-operations/run', {});

  if (error !== undefined || !data) {
    const apiError = await extractApiError(response);
    throw apiError ?? new Error('Unknown error running scheduled operations');
  }

  return data;
}
