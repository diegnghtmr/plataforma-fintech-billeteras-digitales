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
      <div className="max-w-[1200px] mx-auto px-6 sm:px-8 lg:px-12 py-[88px]">
        <h1
          className="text-4xl sm:text-5xl lg:text-[48px] font-medium leading-none tracking-tight text-ink mb-4"
          style={{ fontFamily: "'Inter Tight', 'Inter', system-ui, sans-serif" }}
        >
          Alertas y notificaciones
        </h1>
        <p className="text-base text-charcoal">
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
          <h1
            className="text-4xl sm:text-5xl lg:text-[48px] font-medium leading-none tracking-tight text-ink mb-3"
            style={{ fontFamily: "'Inter Tight', 'Inter', system-ui, sans-serif" }}
          >
            Alertas y notificaciones
          </h1>
          <p className="text-base text-charcoal">
            Usuario: <span className="text-ink font-semibold">{selectedUserId}</span>
          </p>
        </div>

        {/* Unread filter toggle */}
        <label className="flex items-center gap-2 text-ink text-sm font-semibold cursor-pointer mt-2">
          <input
            type="checkbox"
            aria-label="Solo no leídas"
            checked={unreadOnly}
            onChange={(e) => setUnreadOnly(e.target.checked)}
            className="rounded border-hairline-light accent-brand"
          />
          Solo no leídas
        </label>
      </div>

      {isLoading ? (
        <p className="text-stone text-sm">Cargando...</p>
      ) : notifications.length === 0 ? (
        <p className="text-stone text-sm">
          {unreadOnly ? 'No hay notificaciones sin leer.' : 'No hay notificaciones.'}
        </p>
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
                  onClick={() => markReadMutation.mutate(n.id)}
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
