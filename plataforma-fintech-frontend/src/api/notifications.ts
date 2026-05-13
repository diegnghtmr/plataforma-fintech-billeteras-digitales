import { apiClient, extractApiError } from './index';
import type { components } from './generated/schema';

export type NotificationResponse = components['schemas']['NotificationResponse'];

export async function getUserNotifications(
  userId: string,
  unreadOnly: boolean = false
): Promise<NotificationResponse[]> {
  const { data, error, response } = await apiClient.GET(
    '/notifications/users/{userId}',
    {
      params: {
        path: { userId },
        query: { unreadOnly },
      },
    }
  );

  if (error !== undefined || !data) {
    const apiError = await extractApiError(response);
    throw apiError ?? new Error('Unknown error fetching notifications');
  }

  return data;
}

export async function markNotificationAsRead(
  notificationId: string
): Promise<NotificationResponse> {
  const { data, error, response } = await apiClient.POST(
    '/notifications/{notificationId}/read',
    { params: { path: { notificationId } } }
  );

  if (error !== undefined || !data) {
    const apiError = await extractApiError(response);
    throw apiError ?? new Error('Unknown error marking notification as read');
  }

  return data;
}
