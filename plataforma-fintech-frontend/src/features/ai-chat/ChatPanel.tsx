import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useNavigate } from 'react-router-dom';
import { Send } from 'lucide-react';
import { useAiChatMutation } from './hooks';
import { aiChatInputSchema, type AiChatInput } from './schemas';
import type { AiChatResponse, AiSuggestedAction } from '../../api/ai';

interface Message {
  role: 'user' | 'assistant';
  content: string;
  suggestedAction?: AiSuggestedAction | null;
}

interface ChatPanelProps {
  actorUserId: string;
  actorRole: 'USER' | 'ADMIN';
  scope: 'USER' | 'ADMIN';
  conversationId: string;
}

/**
 * Single-conversation chat thread panel.
 * Controlled via react-hook-form + Zod resolver.
 * Accessible: label/aria for textarea, message list role=log aria-live=polite.
 * Suggested actions render as buttons that navigate only (never execute mutations).
 */
export function ChatPanel({ actorUserId, actorRole, scope, conversationId }: ChatPanelProps) {
  const navigate = useNavigate();
  const [messages, setMessages] = useState<Message[]>([]);
  const [apiError, setApiError] = useState<{ code: string; message: string } | null>(null);

  const { mutateAsync, isPending } = useAiChatMutation();

  const {
    register,
    handleSubmit,
    reset,
    watch,
    formState: { errors, isValid },
  } = useForm<AiChatInput>({
    resolver: zodResolver(aiChatInputSchema),
    defaultValues: { message: '', scope },
    mode: 'onChange',
  });

  const messageValue = watch('message');

  async function onSubmit(values: AiChatInput) {
    setApiError(null);

    const userMessage: Message = { role: 'user', content: values.message };
    setMessages((prev) => [...prev, userMessage]);
    reset({ message: '', scope: values.scope });

    try {
      const response: AiChatResponse = await mutateAsync({
        message: values.message,
        actorRole,
        actorUserId,
        scope,
        conversationId,
      });

      const assistantMessage: Message = {
        role: 'assistant',
        content: response.answer ?? '',
        suggestedAction: response.suggestedAction ?? null,
      };
      setMessages((prev) => [...prev, assistantMessage]);
    } catch (err: unknown) {
      const error = err as { code?: string; message?: string };
      setApiError({
        code: error?.code ?? 'UNKNOWN',
        message: error?.message ?? 'Error desconocido',
      });
    }
  }

  function getSuggestedActionPath(action: AiSuggestedAction): string {
    switch (action.type) {
      case 'DRAFT_TRANSFER':
      case 'DRAFT_SCHEDULED_OPERATION':
        return '/operations';
      case 'EXPLAIN_FRAUD_EVENT':
        return '/fraud';
      case 'SHOW_NOTIFICATIONS':
        return '/notifications';
      default:
        return '/';
    }
  }

  const isSubmitDisabled = !isValid || isPending || !messageValue || messageValue.length === 0;

  return (
    <div className="flex flex-col gap-4">
      {/* Message thread */}
      <div
        role="log"
        aria-live="polite"
        aria-label="Conversación con el asistente IA"
        className="flex flex-col gap-3 min-h-[200px]"
      >
        {messages.length === 0 && !apiError && (
          <p className="text-ink-mute text-sm">
            Hacé una pregunta sobre tu actividad financiera.
          </p>
        )}

        {messages.map((msg, i) => (
          <div key={i} className={`flex flex-col gap-1 ${msg.role === 'user' ? 'items-end' : 'items-start'}`}>
            <span className="text-xs text-ink-mute font-medium uppercase tracking-wide">
              {msg.role === 'user' ? 'Vos' : 'Asistente IA'}
            </span>
            <div
              className={`rounded-xl px-4 py-2 max-w-[80%] text-sm ${
                msg.role === 'user'
                  ? 'bg-brand text-on-brand'
                  : 'bg-faint text-ink'
              }`}
            >
              {msg.content}
            </div>

            {/* Suggested action */}
            {msg.role === 'assistant' && msg.suggestedAction && (
              <div className="flex flex-col gap-2 mt-1 max-w-[80%]">
                {msg.suggestedAction.requiresConfirmation && (
                  <p className="text-xs text-warning font-medium">
                    Necesita confirmación antes de ejecutar esta acción.
                  </p>
                )}
                <button
                  type="button"
                  onClick={() => navigate(getSuggestedActionPath(msg.suggestedAction!))}
                  className="inline-flex items-center justify-center rounded-lg border border-brand text-brand text-button-sm px-3 py-1.5 hover:bg-brand hover:text-on-brand transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand"
                >
                  {msg.suggestedAction.label ?? msg.suggestedAction.type}
                </button>
              </div>
            )}
          </div>
        ))}

        {/* API error states */}
        {apiError && (
          <div
            role="alert"
            className="rounded-xl bg-error-subtle border border-error px-4 py-3 text-sm text-error"
          >
            {apiError.code === 'AI_UNAVAILABLE' && (
              <p>IA no disponible. Intentá de nuevo en unos momentos.</p>
            )}
            {apiError.code === 'AI_MESSAGE_TOO_LONG' && (
              <p>El mensaje supera los 1000 caracteres. Por favor, acortalo.</p>
            )}
            {apiError.code !== 'AI_UNAVAILABLE' && apiError.code !== 'AI_MESSAGE_TOO_LONG' && (
              <p>{apiError.message}</p>
            )}
          </div>
        )}

        {isPending && (
          <div className="flex items-start gap-2">
            <div className="rounded-xl bg-faint px-4 py-2 text-sm text-ink-mute animate-pulse">
              El asistente está pensando...
            </div>
          </div>
        )}
      </div>

      {/* Input form */}
      <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-2">
        <div className="flex gap-2">
          <textarea
            {...register('message')}
            aria-label="Escribe tu pregunta"
            placeholder="Pregunta algo sobre tus finanzas..."
            rows={2}
            disabled={isPending}
            className="flex-1 resize-none rounded-lg border border-hairline bg-surface px-3 py-2 text-sm text-ink placeholder-ink-mute focus:outline-none focus:ring-2 focus:ring-brand disabled:opacity-50"
          />
          <button
            type="submit"
            aria-label="Enviar pregunta"
            disabled={isSubmitDisabled}
            className="shrink-0 inline-flex items-center justify-center rounded-lg bg-brand text-on-brand px-4 py-2 text-button-sm hover:bg-brand-hover transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand disabled:opacity-40 disabled:cursor-not-allowed"
          >
            <Send size={16} strokeWidth={1.5} />
            <span className="ml-1">Enviar</span>
          </button>
        </div>

        {errors.message && (
          <p role="alert" className="text-xs text-error">
            {errors.message.message}
          </p>
        )}
      </form>
    </div>
  );
}
