import { apiClient, extractApiError } from './index';
import type { components, operations } from './generated/schema';

export type TransactionResponse = components['schemas']['TransactionResponse'];
export type TransactionType = components['schemas']['TransactionType'];
export type TransactionStatus = components['schemas']['TransactionStatus'];

export type GetUserTransactionsQuery =
  operations['getUserTransactions']['parameters']['query'];

export interface GetUserTransactionsFilters {
  type?: TransactionType;
  status?: TransactionStatus;
}

export async function getUserTransactions(
  userId: string,
  filters?: GetUserTransactionsFilters
): Promise<TransactionResponse[]> {
  const { data, error, response } = await apiClient.GET(
    '/users/{userId}/transactions',
    {
      params: {
        path: { userId },
        ...(filters !== undefined ? { query: filters } : {}),
      },
    }
  );

  if (error !== undefined || !data) {
    const apiError = await extractApiError(response);
    throw apiError ?? new Error('Unknown error fetching user transactions');
  }

  return data;
}

export async function getWalletTransactions(
  userId: string,
  walletId: string
): Promise<TransactionResponse[]> {
  const { data, error, response } = await apiClient.GET(
    '/users/{userId}/wallets/{walletId}/transactions',
    {
      params: { path: { userId, walletId } },
    }
  );

  if (error !== undefined || !data) {
    const apiError = await extractApiError(response);
    throw apiError ?? new Error('Unknown error fetching wallet transactions');
  }

  return data;
}

export async function getTransaction(
  transactionId: string
): Promise<TransactionResponse> {
  const { data, error, response } = await apiClient.GET(
    '/transactions/{transactionId}',
    {
      params: { path: { transactionId } },
    }
  );

  if (error !== undefined || !data) {
    const apiError = await extractApiError(response);
    throw apiError ?? new Error('Unknown error fetching transaction');
  }

  return data;
}

export async function reverseTransaction(
  transactionId: string
): Promise<TransactionResponse> {
  const { data, error, response } = await apiClient.POST(
    '/transactions/{transactionId}/reverse',
    {
      params: { path: { transactionId } },
    }
  );

  if (error !== undefined || !data) {
    const apiError = await extractApiError(response);
    throw apiError ?? new Error('Unknown error reversing transaction');
  }

  return data;
}
