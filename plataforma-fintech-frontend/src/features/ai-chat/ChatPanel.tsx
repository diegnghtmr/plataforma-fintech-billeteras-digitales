import { useEffect, useRef, useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useNavigate } from 'react-router-dom';
import { Bot, Loader2, Send, Sparkles, User } from 'lucide-react';
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

const MAX_LENGTH = 1000;

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
  const threadEndRef = useRef<HTMLDivElement | null>(null);

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

  const messageValue = watch('message') ?? '';
  const charCount = messageValue.length;
  const isOverLimit = charCount > MAX_LENGTH;

  useEffect(() => {
    const node = threadEndRef.current;
    if (node && typeof node.scrollIntoView === 'function') {
      node.scrollIntoView({ behavior: 'smooth', block: 'end' });
    }
  }, [messages.length, isPending]);

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

  function handleTextareaKeyDown(e: React.KeyboardEvent<HTMLTextAreaElement>) {
    if (e.key === 'Enter' && !e.shiftKey && !e.nativeEvent.isComposing) {
      e.preventDefault();
      if (!isSubmitDisabled) handleSubmit(onSubmit)();
    }
  }

  const isSubmitDisabled = !isValid || isPending || charCount === 0 || isOverLimit;

  return (
    <div className="flex flex-col gap-4">
      {/* Message thread */}
      <div
        role="log"
        aria-live="polite"
        aria-label="Conversación con el asistente IA"
        className="flex flex-col gap-4 min-h-[320px] rounded-2xl border border-hairline-light bg-surface-soft p-4 sm:p-5"
      >
        {messages.length === 0 && !apiError && !isPending && (
          <div className="py-12 px-4 text-center">
            <div className="mx-auto flex items-center justify-center w-12 h-12 rounded-full bg-brand/10 text-brand">
              <Sparkles size={22} strokeWidth={1.5} aria-hidden />
            </div>
            <p className="mt-3 text-sm font-medium text-ink">Probá una pregunta para empezar</p>
            <p className="mt-1 mx-auto max-w-md text-xs text-mute leading-relaxed text-balance">
              Por ejemplo:{' '}
              <span className="italic">«¿Cómo estuvo mi actividad esta semana?»</span> o{' '}
              <span className="italic">«Explicame mis alertas de fraude».</span>
            </p>
          </div>
        )}

        {messages.map((msg, i) => {
          const isUser = msg.role === 'user';
          return (
            <div
              key={i}
              className={`flex gap-2.5 ${isUser ? 'flex-row-reverse' : 'flex-row'}`}
            >
              {/* Avatar */}
              <div
                aria-hidden
                className={`shrink-0 mt-0.5 flex items-center justify-center w-8 h-8 rounded-full ${
                  isUser
                    ? 'bg-brand text-on-brand'
                    : 'bg-surface-card text-brand border border-hairline-light'
                }`}
              >
                {isUser ? (
                  <User size={16} strokeWidth={1.8} />
                ) : (
                  <Bot size={16} strokeWidth={1.8} />
                )}
              </div>

              {/* Message body */}
              <div className={`flex flex-col gap-1.5 max-w-[80%] ${isUser ? 'items-end' : 'items-start'}`}>
                <span className="text-[11px] text-mute font-medium uppercase tracking-wide">
                  {isUser ? 'Vos' : 'Asistente IA'}
                </span>
                <div
                  className={`rounded-2xl px-4 py-2.5 text-sm leading-relaxed shadow-sm ${
                    isUser
                      ? 'bg-brand text-on-brand rounded-tr-sm'
                      : 'bg-surface-card text-ink border border-hairline-light rounded-tl-sm'
                  }`}
                >
                  {msg.content}
                </div>

                {/* Suggested action */}
                {!isUser && msg.suggestedAction && (
                  <div className="flex flex-col gap-1.5 mt-1">
                    {msg.suggestedAction.requiresConfirmation && (
                      <p className="text-[11px] text-accent-warning font-medium">
                        Necesita confirmación antes de ejecutar.
                      </p>
                    )}
                    <button
                      type="button"
                      onClick={() => navigate(getSuggestedActionPath(msg.suggestedAction!))}
                      className="inline-flex items-center justify-center rounded-lg border border-brand text-brand text-[13px] font-medium px-3 py-1.5 transition-colors hover:bg-brand hover:text-on-brand focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand/40"
                    >
                      {msg.suggestedAction.label ?? msg.suggestedAction.type}
                    </button>
                  </div>
                )}
              </div>
            </div>
          );
        })}

        {/* Typing indicator while awaiting response */}
        {isPending && (
          <div className="flex gap-2.5">
            <div
              aria-hidden
              className="shrink-0 mt-0.5 flex items-center justify-center w-8 h-8 rounded-full bg-surface-card text-brand border border-hairline-light"
            >
              <Bot size={16} strokeWidth={1.8} />
            </div>
            <div className="flex flex-col gap-1.5">
              <span className="text-[11px] text-mute font-medium uppercase tracking-wide">
                Asistente IA
              </span>
              <div
                role="status"
                aria-label="El asistente está escribiendo"
                className="inline-flex items-center gap-1.5 rounded-2xl rounded-tl-sm bg-surface-card border border-hairline-light px-4 py-3 shadow-sm"
              >
                <span className="sr-only">El asistente está escribiendo…</span>
                <span className="block w-1.5 h-1.5 rounded-full bg-brand animate-bounce [animation-delay:-0.3s]" />
                <span className="block w-1.5 h-1.5 rounded-full bg-brand animate-bounce [animation-delay:-0.15s]" />
                <span className="block w-1.5 h-1.5 rounded-full bg-brand animate-bounce" />
              </div>
            </div>
          </div>
        )}

        {/* API error states */}
        {apiError && (
          <div
            role="alert"
            className="rounded-xl bg-accent-danger/10 border border-accent-danger/40 px-4 py-3 text-sm text-accent-danger"
          >
            {apiError.code === 'AI_UNAVAILABLE' && (
              <p>IA no disponible en este momento. Probá de nuevo en unos segundos.</p>
            )}
            {apiError.code === 'AI_MESSAGE_TOO_LONG' && (
              <p>El mensaje supera los {MAX_LENGTH} caracteres. Acortalo y reintentá.</p>
            )}
            {apiError.code !== 'AI_UNAVAILABLE' && apiError.code !== 'AI_MESSAGE_TOO_LONG' && (
              <p>{apiError.message}</p>
            )}
          </div>
        )}

        <div ref={threadEndRef} aria-hidden />
      </div>

      {/* Input form */}
      <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-1.5">
        <div className="flex items-end gap-2 rounded-2xl border border-hairline-light bg-surface-card p-2 shadow-sm transition-colors focus-within:border-brand focus-within:ring-2 focus-within:ring-brand/30">
          <textarea
            {...register('message')}
            aria-label="Escribí tu pregunta"
            placeholder="Pregunta algo sobre tus finanzas…"
            rows={2}
            maxLength={MAX_LENGTH + 50 /* allow brief overflow so Zod handles the message */}
            disabled={isPending}
            onKeyDown={handleTextareaKeyDown}
            style={{ colorScheme: 'light' }}
            className="flex-1 resize-none bg-transparent text-sm text-ink leading-relaxed placeholder:text-stone caret-brand selection:bg-brand selection:text-on-brand focus:outline-none disabled:opacity-60 disabled:cursor-not-allowed px-2 py-1.5"
          />
          <button
            type="submit"
            aria-label={isPending ? 'Enviando…' : 'Enviar pregunta'}
            aria-busy={isPending}
            disabled={isSubmitDisabled}
            className="shrink-0 inline-flex items-center justify-center gap-1.5 rounded-xl bg-brand text-on-brand px-4 h-10 text-sm font-medium transition-colors hover:bg-brand-bright focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand/40 disabled:opacity-40 disabled:cursor-not-allowed"
          >
            {isPending ? (
              <>
                <Loader2 size={16} strokeWidth={1.8} className="animate-spin" aria-hidden />
                <span>Enviando</span>
              </>
            ) : (
              <>
                <Send size={16} strokeWidth={1.8} aria-hidden />
                <span>Enviar</span>
              </>
            )}
          </button>
        </div>

        <div className="flex items-center justify-between gap-3 px-1 min-h-[18px]">
          <p
            role={errors.message ? 'alert' : undefined}
            className={`text-xs ${errors.message ? 'text-accent-danger' : 'text-mute'}`}
          >
            {errors.message?.message ?? 'Enter para enviar · Shift + Enter para nueva línea'}
          </p>
          <span
            aria-live="polite"
            className={`text-xs tabular-nums ${
              isOverLimit ? 'text-accent-danger font-medium' : 'text-mute'
            }`}
          >
            {charCount}/{MAX_LENGTH}
          </span>
        </div>
      </form>
    </div>
  );
}
