import { useMemo, useState } from 'react';
import { ShieldAlert } from 'lucide-react';
import { useFraudEventsQuery } from './hooks';
import { useUsersListQuery } from '../users/hooks';
import { FraudSeverityBadge } from './FraudSeverityBadge';
import { EmptyState } from '../../shared/components/EmptyState';
import { labelFraudSeverity, labelFraudType } from '../../shared/i18n/enum-labels';
import type { FraudSeverity } from '../../api/fraud';

const SEVERITY_OPTIONS = ['', 'LOW', 'MEDIUM', 'HIGH', 'CRITICAL'] as const;
type SeverityOption = FraudSeverity | '';

export function FraudPage() {
  const [userQuery, setUserQuery] = useState('');
  const [severityFilter, setSeverityFilter] = useState<SeverityOption>('');

  const serverFilters = severityFilter ? { severity: severityFilter } : undefined;
  const { data: events } = useFraudEventsQuery(serverFilters);
  const { data: users } = useUsersListQuery();

  const userNameById = useMemo(() => {
    const map = new Map<string, string>();
    (users ?? []).forEach((u) => map.set(u.id, u.name));
    return map;
  }, [users]);

  const filteredEvents = useMemo(() => {
    if (!events) return [];
    const q = userQuery.trim().toLowerCase();
    if (!q) return events;
    return events.filter((ev) => {
      const name = userNameById.get(ev.userId)?.toLowerCase() ?? '';
      return ev.userId.toLowerCase().includes(q) || name.includes(q);
    });
  }, [events, userQuery, userNameById]);

  return (
    <>
      <section className="bg-canvas-dark py-[88px]">
        <div className="max-w-[1200px] mx-auto px-6 sm:px-8 lg:px-12">
          <h1 className="text-display-xl text-on-dark mb-4">
            Eventos sospechosos
          </h1>
          <p className="text-body-lg text-on-dark-mute w-full max-w-[36rem]">
            Monitoreo de fraude en tiempo real. Filtrá por usuario o nivel de severidad.
          </p>

          <div className="flex flex-wrap gap-4 mt-8">
            <input
              type="text"
              placeholder="Buscar usuario (nombre o ID)"
              value={userQuery}
              onChange={(e) => setUserQuery(e.target.value)}
              aria-label="Buscar usuario"
              className="bg-surface-deep text-on-dark border border-hairline-dark rounded-[12px] px-4 h-12 text-body-sm focus:outline-none focus:border-on-dark w-full sm:w-64 placeholder:text-on-dark-mute"
            />
            <select
              value={severityFilter}
              onChange={(e) => setSeverityFilter(e.target.value as SeverityOption)}
              className="bg-surface-deep text-on-dark border border-hairline-dark rounded-[12px] px-4 h-12 text-sm focus:outline-none focus:border-on-dark"
            >
              <option value="">Todas las severidades</option>
              {SEVERITY_OPTIONS.filter(Boolean).map((s) => (
                <option key={s} value={s}>{labelFraudSeverity(s)}</option>
              ))}
            </select>
          </div>
        </div>
      </section>

      <section className="bg-canvas-dark pb-[88px]">
        <div className="max-w-[1200px] mx-auto px-6 sm:px-8 lg:px-12">
          {filteredEvents.length === 0 ? (
            <EmptyState
              icon={ShieldAlert}
              title="Sin eventos de fraude"
              description="No se encontraron eventos con los filtros actuales."
              tone="dark"
            />
          ) : (
            <div className="flex flex-col gap-4">
              {filteredEvents.map((event) => {
                const userName = userNameById.get(event.userId);
                return (
                  <div
                    key={event.id}
                    className="bg-surface-elevated rounded-[20px] p-8 flex flex-col gap-3"
                  >
                    <div className="flex items-start gap-4 flex-wrap">
                      <FraudSeverityBadge
                        severity={event.severity as 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'}
                      />
                      <span className="text-on-dark font-semibold">{event.description}</span>
                    </div>
                    <div className="grid grid-cols-2 gap-x-6 gap-y-1.5 text-sm">
                      <span className="text-on-dark-mute">Tipo</span>
                      <span className="text-on-dark font-medium">{labelFraudType(event.type)}</span>
                      <span className="text-on-dark-mute">Usuario</span>
                      <span className="text-on-dark">
                        {userName ? (
                          <>
                            {userName}{' '}
                            <span className="text-on-dark-mute font-mono text-xs">
                              ({event.userId})
                            </span>
                          </>
                        ) : (
                          <span className="font-mono text-xs">{event.userId}</span>
                        )}
                      </span>
                      <span className="text-on-dark-mute">Transacción</span>
                      <span className="text-on-dark font-mono text-xs">{event.transactionId ?? '—'}</span>
                      <span className="text-on-dark-mute">Fecha</span>
                      <span className="text-stone text-xs">
                        {new Date(event.createdAt).toLocaleString()}
                      </span>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>
      </section>
    </>
  );
}
