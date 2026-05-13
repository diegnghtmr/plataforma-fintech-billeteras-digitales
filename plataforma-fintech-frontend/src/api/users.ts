import { apiClient, extractApiError } from './index';
import type { components } from './generated/schema';

export type CreateUserRequest = components['schemas']['CreateUserRequest'];
export type UpdateUserRequest = components['schemas']['UpdateUserRequest'];
export type UserResponse = components['schemas']['UserResponse'];

export async function createUser(payload: CreateUserRequest): Promise<UserResponse> {
  const { data, error, response } = await apiClient.POST('/users', { body: payload });

  if (error !== undefined || !data) {
    const apiError = await extractApiError(response);
    throw apiError ?? new Error('Unknown error creating user');
  }

  return data;
}

export async function getUserById(userId: string): Promise<UserResponse> {
  const { data, error, response } = await apiClient.GET('/users/{userId}', {
    params: { path: { userId } },
  });

  if (error !== undefined || !data) {
    const apiError = await extractApiError(response);
    throw apiError ?? new Error('Unknown error fetching user');
  }

  return data;
}

export async function updateUser(userId: string, body: UpdateUserRequest): Promise<UserResponse> {
  const { data, error, response } = await apiClient.PUT('/users/{userId}', {
    params: { path: { userId } },
    body,
  });

  if (error !== undefined || !data) {
    const apiError = await extractApiError(response);
    throw apiError ?? new Error('Unknown error updating user');
  }

  return data;
}

export async function deleteUser(userId: string): Promise<void> {
  const { error, response } = await apiClient.DELETE('/users/{userId}', {
    params: { path: { userId } },
  });

  if (error !== undefined) {
    const apiError = await extractApiError(response);
    throw apiError ?? new Error('Unknown error deleting user');
  }
}
