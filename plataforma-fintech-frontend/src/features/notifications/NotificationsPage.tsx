import { useState } from 'react';
import { useSelectionStore } from '../../stores/use-selection-store';
import { useUserNotificationsQuery, useMarkNotificationReadMutation } from './hooks';
import { SeverityBadge } from './SeverityBadge';
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
      <div className="flex flex-col gap-4 text-canvas-fg">
        <h2 className="text-xl font-bold">Alertas / Notificaciones</h2>
        <p className="text-surface-fg/70">
          Por favor selecciona un usuario para ver sus notificaciones.
        </p>
      </div>
    );
  }

  return (
    <div className="flex flex-col gap-6">
      <div className="flex items-center justify-between">
        <h2 className="text-canvas-fg text-xl font-bold">
          Alertas — <span className="text-accent">{selectedUserId}</span>
        </h2>

        <label className="flex items-center gap-2 text-surface-fg text-sm cursor-pointer">
          <input
            type="checkbox"
            aria-label="Solo no leídas"
            checked={unreadOnly}
            onChange={(e) => setUnreadOnly(e.target.checked)}
            className="rounded"
          />
          Solo no leídas
        </label>
      </div>

      {isLoading ? (
        <p className="text-surface-fg/70 text-sm">Cargando...</p>
      ) : notifications.length === 0 ? (
        <p className="text-surface-fg/70 text-sm">
          {unreadOnly ? 'No hay notificaciones sin leer.' : 'No hay notificaciones.'}
        </p>
      ) : (
        <div className="flex flex-col gap-3">
          {notifications.map((n: NotificationResponse) => (
            <div
              key={n.id}
              className={`bg-surface rounded-lg p-4 flex items-start justify-between gap-4 ${
                n.read ? 'opacity-60' : ''
              }`}
            >
              <div className="flex flex-col gap-1 flex-1">
                <div className="flex items-center gap-2">
                  <SeverityBadge severity={n.severity as 'INFO' | 'WARNING' | 'CRITICAL'} />
                  <span className="text-surface-fg font-semibold text-sm">{n.title}</span>
                  {n.read && (
                    <span className="text-xs text-surface-fg/50">(leída)</span>
                  )}
                </div>
                <p className="text-surface-fg/70 text-sm">{n.message}</p>
                <span className="text-xs text-surface-fg/40">
                  {new Date(n.createdAt).toLocaleString()}
                </span>
              </div>

              {!n.read && (
                <button
                  onClick={() => markReadMutation.mutate(n.id)}
                  disabled={markReadMutation.isPending}
                  aria-label="Marcar como leída"
                  className="px-3 py-1 rounded text-xs bg-accent text-accent-fg hover:opacity-90 disabled:opacity-50 whitespace-nowrap"
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
