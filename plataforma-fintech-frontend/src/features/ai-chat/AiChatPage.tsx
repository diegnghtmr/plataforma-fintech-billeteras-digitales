import { useRef, useState } from 'react';
import { ChatPanel } from './ChatPanel';
import type { AiScopeValue } from './schemas';

const SCOPE_OPTIONS: { value: AiScopeValue; label: string }[] = [
  { value: 'USER', label: 'Salud financiera personal' },
  { value: 'ADMIN', label: 'Vista de plataforma (Admin)' },
];

// v1: actorRole and actorUserId are hardcoded for demo purposes.
// TODO: replace with real auth context once Spring Security is integrated.
const DEMO_ACTOR_USER_ID = 'USR001';
const DEMO_ACTOR_ROLE: 'USER' | 'ADMIN' = 'USER';

/**
 * Page shell for the AI Insight Chat feature.
 * Generates a per-session conversationId via crypto.randomUUID().
 * Embeds ChatPanel with scope selector.
 */
export function AiChatPage() {
  const conversationId = useRef<string>(crypto.randomUUID());
  const [scope, setScope] = useState<AiScopeValue>('USER');

  return (
    <div className="max-w-2xl mx-auto px-4 py-8 flex flex-col gap-6">
      <header className="flex flex-col gap-2">
        <h1 className="font-display font-semibold text-2xl text-ink">Asistente IA</h1>
        <p className="text-sm text-mute">
          Pregunta sobre tu actividad financiera, saldo, puntos, alertas de fraude o programa
          operaciones con asistencia inteligente.
        </p>
      </header>

      {/* Scope selector */}
      <div className="flex flex-col gap-1.5">
        <label
          htmlFor="ai-scope-select"
          className="text-xs font-medium text-mute uppercase tracking-wide"
        >
          Contexto
        </label>
        <select
          id="ai-scope-select"
          value={scope}
          onChange={(e) => setScope(e.target.value as AiScopeValue)}
          style={{ colorScheme: 'light' }}
          className="rounded-lg border border-hairline-light bg-surface-card px-3 h-11 text-sm text-ink shadow-sm transition-colors hover:border-stone focus:outline-none focus:border-brand focus:ring-2 focus:ring-brand/30"
        >
          {SCOPE_OPTIONS.map((opt) => (
            <option key={opt.value} value={opt.value}>
              {opt.label}
            </option>
          ))}
        </select>
      </div>

      <ChatPanel
        actorUserId={DEMO_ACTOR_USER_ID}
        actorRole={DEMO_ACTOR_ROLE}
        scope={scope}
        conversationId={conversationId.current}
      />
    </div>
  );
}
