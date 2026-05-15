import { apiClient, extractApiError } from './index';
import type { components } from './generated/schema';

export type CreateWalletRequest = components['schemas']['CreateWalletRequest'];
export type UpdateWalletRequest = components['schemas']['UpdateWalletRequest'];
export type WalletResponse = components['schemas']['WalletResponse'];

export async function listUserWallets(userId: string): Promise<WalletResponse[]> {
  const { data, error, response } = await apiClient.GET('/users/{userId}/wallets', {
    params: { path: { userId } },
  });

  if (error !== undefined || !data) {
    const apiError = await extractApiError(response);
    throw apiError ?? new Error('Unknown error fetching wallets');
  }

  return data;
}

export async function createWallet(
  userId: string,
  payload: CreateWalletRequest
): Promise<WalletResponse> {
  const { data, error, response } = await apiClient.POST('/users/{userId}/wallets', {
    params: { path: { userId } },
    body: payload,
  });

  if (error !== undefined || !data) {
    const apiError = await extractApiError(response);
    throw apiError ?? new Error('Unknown error creating wallet');
  }

  return data;
}

export async function updateWallet(
  userId: string,
  walletId: string,
  payload: UpdateWalletRequest
): Promise<WalletResponse> {
  const { data, error, response } = await apiClient.PATCH(
    '/users/{userId}/wallets/{walletId}',
    {
      params: { path: { userId, walletId } },
      body: payload,
    }
  );

  if (error !== undefined || !data) {
    const apiError = await extractApiError(response);
    throw apiError ?? new Error('Unknown error updating wallet');
  }

  return data;
}
