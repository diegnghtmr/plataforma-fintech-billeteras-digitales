import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { getUserNotifications, markNotificationAsRead } from '../../api/notifications';
import { queryKeys } from '../../api/query-keys';

/**
 * Query hook for user notifications.
 * Disabled when userId is undefined (falsy).
 * unreadOnly is included in the queryKey for cache isolation.
 */
export function useUserNotificationsQuery(
  userId: string | undefined,
  unreadOnly: boolean = false
) {
  return useQuery({
    queryKey: queryKeys.notifications.byUser(userId ?? '', unreadOnly),
    queryFn: () => getUserNotifications(userId!, unreadOnly),
    enabled: !!userId,
  });
}

/**
 * Mutation hook to mark a notification as read.
 * On success, invalidates both unreadOnly=true and unreadOnly=false variants
 * for the given userId via predicate matching all ['notifications', 'byUser', userId, *] keys.
 */
export function useMarkNotificationReadMutation(userId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (notificationId: string) => markNotificationAsRead(notificationId),
    onSuccess: () => {
      // Invalidate all notifications.byUser(userId, *) keys via predicate
      queryClient.invalidateQueries({
        predicate: (q) => {
          const key = q.queryKey;
          return (
            key[0] === 'notifications' &&
            key[1] === 'byUser' &&
            key[2] === userId
          );
        },
      });
    },
  });
}
