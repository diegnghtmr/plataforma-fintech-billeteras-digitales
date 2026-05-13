import { apiClient, extractApiError } from './index';
import type { components } from './generated/schema';

export type MoneyOperationRequest = components['schemas']['MoneyOperationRequest'];
export type InternalTransferRequest = components['schemas']['InternalTransferRequest'];
export type ExternalTransferRequest = components['schemas']['ExternalTransferRequest'];
export type TransactionResponse = components['schemas']['TransactionResponse'];
export type ExternalTransferResponse = components['schemas']['ExternalTransferResponse'];

export async function recharge(
  userId: string,
  walletId: string,
  body: MoneyOperationRequest
): Promise<TransactionResponse> {
  const { data, error, response } = await apiClient.POST(
    '/users/{userId}/wallets/{walletId}/recharge',
    { params: { path: { userId, walletId } }, body }
  );

  if (error !== undefined || !data) {
    const apiError = await extractApiError(response);
    throw apiError ?? new Error('Unknown error during recharge');
  }

  return data;
}

export async function withdraw(
  userId: string,
  walletId: string,
  body: MoneyOperationRequest
): Promise<TransactionResponse> {
  const { data, error, response } = await apiClient.POST(
    '/users/{userId}/wallets/{walletId}/withdraw',
    { params: { path: { userId, walletId } }, body }
  );

  if (error !== undefined || !data) {
    const apiError = await extractApiError(response);
    throw apiError ?? new Error('Unknown error during withdrawal');
  }

  return data;
}

export async function internalTransfer(
  userId: string,
  body: InternalTransferRequest
): Promise<TransactionResponse> {
  const { data, error, response } = await apiClient.POST(
    '/users/{userId}/transfers/internal',
    { params: { path: { userId } }, body }
  );

  if (error !== undefined || !data) {
    const apiError = await extractApiError(response);
    throw apiError ?? new Error('Unknown error during internal transfer');
  }

  return data;
}

export async function externalTransfer(
  body: ExternalTransferRequest
): Promise<ExternalTransferResponse> {
  const { data, error, response } = await apiClient.POST(
    '/transfers/external',
    { body }
  );

  if (error !== undefined || !data) {
    const apiError = await extractApiError(response);
    throw apiError ?? new Error('Unknown error during external transfer');
  }

  return data;
}
