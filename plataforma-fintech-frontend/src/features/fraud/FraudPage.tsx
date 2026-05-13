import { useState } from 'react';
import { useFraudEventsQuery } from './hooks';
import { FraudSeverityBadge } from './FraudSeverityBadge';
import type { FraudSeverity } from '../../api/fraud';

const SEVERITY_OPTIONS = ['', 'LOW', 'MEDIUM', 'HIGH', 'CRITICAL'] as const;
type SeverityOption = FraudSeverity | '';

export function FraudPage() {
  const [userIdFilter, setUserIdFilter] = useState('');
  const [severityFilter, setSeverityFilter] = useState<SeverityOption>('');

  const filters = {
    ...(userIdFilter.trim() ? { userId: userIdFilter.trim() } : {}),
    ...(severityFilter ? { severity: severityFilter } : {}),
  };

  const { data: events } = useFraudEventsQuery(
    Object.keys(filters).length > 0 ? filters : undefined
  );

  return (
    <div className="flex flex-col gap-6 max-w-3xl">
      <h2 className="text-canvas-fg text-xl font-bold">Fraude</h2>

      {/* Filter form */}
      <div className="flex flex-wrap gap-3 items-center">
        <input
          type="text"
          placeholder="Usuario ID"
          value={userIdFilter}
          onChange={(e) => setUserIdFilter(e.target.value)}
          className="bg-surface text-surface-fg text-sm rounded px-3 py-1.5 border border-surface-fg/20 w-48"
        />
        <select
          value={severityFilter}
          onChange={(e) => setSeverityFilter(e.target.value as SeverityOption)}
          className="bg-surface text-surface-fg text-sm rounded px-2 py-1.5 border border-surface-fg/20"
        >
          <option value="">Todas las severidades</option>
          {SEVERITY_OPTIONS.filter(Boolean).map((s) => (
            <option key={s} value={s}>
              {s}
            </option>
          ))}
        </select>
      </div>

      {/* Events table */}
      {!events || events.length === 0 ? (
        <p className="text-surface-fg/60 text-sm">No hay eventos de fraude</p>
      ) : (
        <table className="w-full text-sm">
          <thead>
            <tr className="text-surface-fg/60 text-left">
              <th className="pb-2">ID</th>
              <th className="pb-2">Usuario</th>
              <th className="pb-2">Tipo</th>
              <th className="pb-2">Severidad</th>
              <th className="pb-2">Descripción</th>
              <th className="pb-2">Fecha</th>
            </tr>
          </thead>
          <tbody>
            {events.map((event) => (
              <tr key={event.id} className="border-t border-surface-fg/10">
                <td className="py-1.5 text-surface-fg/70 font-mono text-xs">{event.id}</td>
                <td className="py-1.5 text-surface-fg/70 font-mono text-xs">{event.userId}</td>
                <td className="py-1.5 text-canvas-fg">{event.type}</td>
                <td className="py-1.5">
                  <FraudSeverityBadge
                    severity={event.severity as 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'}
                  />
                </td>
                <td className="py-1.5 text-surface-fg/80">{event.description}</td>
                <td className="py-1.5 text-surface-fg/60 text-xs">
                  {new Date(event.createdAt).toLocaleString()}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
