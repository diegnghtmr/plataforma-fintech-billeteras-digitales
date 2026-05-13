import { useState } from 'react';
import { Bell, Check } from 'lucide-react';
import { useSelectionStore } from '../../stores/use-selection-store';
import { useUserNotificationsQuery, useMarkNotificationReadMutation } from './hooks';
import { SeverityBadge } from './SeverityBadge';
import { EmptyState } from '../../shared/components/EmptyState';
import { Skeleton } from '../../shared/components/Skeleton';
import { pushToast } from '../../shared/components/Toast';
import type { NotificationResponse } from '../../api/notifications';

export function NotificationsPage() {
  const selectedUserId = useSelectionStore((s) => s.selectedUserId);
  const [unreadOnly, setUnreadOnly] = useState(false);

  const { data: notifications = [], isLoading } = useUserNotificationsQuery(
    selectedUserId ?? undefined,
    unreadOnly
  );
  const markReadMutation = useMarkNotificationReadMutation(selectedUserId ?? '');

  if (!selectedUserId) {
    return (
      <div className="max-w-[1200px] mx-auto px-6 sm:px-8 lg:px-12 py-[88px]">
        <h1 className="text-display-lg text-ink mb-4">
          Alertas y notificaciones
        </h1>
        <p className="text-body-md text-charcoal">
          Por favor selecciona un usuario para ver sus notificaciones.
        </p>
      </div>
    );
  }

  return (
    <div className="max-w-[1200px] mx-auto px-6 sm:px-8 lg:px-12 py-[88px]">
      {/* Hero */}
      <div className="mb-12 flex items-start justify-between flex-wrap gap-4">
        <div>
          <h1 className="text-display-lg text-ink mb-3">
            Alertas y notificaciones
          </h1>
          <p className="text-body-md text-charcoal">
            Usuario: <span className="text-ink font-semibold">{selectedUserId}</span>
          </p>
        </div>

        {/* Unread filter chip */}
        <label className="inline-flex items-center gap-2 px-3 py-1.5 rounded-full bg-surface-soft hover:bg-faint cursor-pointer transition-colors mt-2 select-none">
          <input
            type="checkbox"
            aria-label="Solo no leídas"
            checked={unreadOnly}
            onChange={(e) => setUnreadOnly(e.target.checked)}
            className="peer sr-only"
          />
          {/* visible checkbox box */}
          <span className="w-4 h-4 rounded flex items-center justify-center border border-hairline-light bg-canvas-light peer-checked:bg-canvas-dark peer-checked:border-canvas-dark transition-colors shrink-0">
            <Check
              size={10}
              strokeWidth={3}
              className={unreadOnly ? 'text-on-dark' : 'invisible'}
            />
          </span>
          <span className="text-ink text-sm font-semibold">Solo no leídas</span>
        </label>
      </div>

      {isLoading ? (
        <div className="flex flex-col gap-4">
          {Array.from({ length: 3 }).map((_, i) => (
            <Skeleton key={i} className="h-24 w-full rounded-[20px]" />
          ))}
        </div>
      ) : notifications.length === 0 ? (
        <EmptyState
          icon={Bell}
          title={unreadOnly ? 'Sin notificaciones no leídas' : 'Sin notificaciones'}
          description="No hay nada que mostrar por ahora."
        />
      ) : (
        <div className="flex flex-col gap-4">
          {notifications.map((n: NotificationResponse) => (
            <div
              key={n.id}
              className={`bg-surface-card border border-hairline-light rounded-[20px] p-8 flex items-start justify-between gap-6 ${
                n.read ? 'opacity-60' : ''
              }`}
            >
              <div className="flex flex-col gap-2 flex-1">
                <div className="flex items-center gap-3 flex-wrap">
                  <SeverityBadge severity={n.severity as 'INFO' | 'WARNING' | 'CRITICAL'} />
                  <span className="text-ink font-semibold">{n.title}</span>
                  {n.read && (
                    <span className="text-xs text-stone">(leída)</span>
                  )}
                </div>
                <p className="text-charcoal text-sm">{n.message}</p>
                <span className="text-xs text-stone">
                  {new Date(n.createdAt).toLocaleString()}
                </span>
              </div>

              {!n.read && (
                <button
                  onClick={() => markReadMutation.mutate(n.id, {
                    onSuccess: () => pushToast({ variant: 'success', message: 'Notificación marcada como leída.' }),
                    onError: () => pushToast({ variant: 'error', message: 'No se pudo marcar la notificación.' }),
                  })}
                  disabled={markReadMutation.isPending}
                  aria-label="Marcar como leída"
                  className="inline-flex items-center justify-center rounded-full text-xs font-semibold tracking-wide px-4 py-2 h-9 bg-surface-soft text-ink hover:opacity-90 disabled:opacity-50 whitespace-nowrap shrink-0"
                >
                  Marcar como leída
                </button>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
