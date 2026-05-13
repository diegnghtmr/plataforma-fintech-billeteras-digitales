import { useEffect } from 'react';
import { create } from 'zustand';
import { CheckCircle2, XCircle, Info, X } from 'lucide-react';

/* ─── Types ─────────────────────────────────────────────────────────────────── */

export type ToastVariant = 'success' | 'error' | 'info';

export interface Toast {
  id: string;
  variant: ToastVariant;
  message: string;
}

/* ─── Store ──────────────────────────────────────────────────────────────────── */

interface ToastStore {
  toasts: Toast[];
  pushToast: (t: Omit<Toast, 'id'>) => void;
  dismissToast: (id: string) => void;
}

export const useToastStore = create<ToastStore>((set) => ({
  toasts: [],
  pushToast: (t) =>
    set((state) => ({
      toasts: [
        ...state.toasts,
        { ...t, id: `${Date.now()}-${Math.random().toString(36).slice(2)}` },
      ],
    })),
  dismissToast: (id) =>
    set((state) => ({ toasts: state.toasts.filter((t) => t.id !== id) })),
}));

/* ─── Convenience helpers ────────────────────────────────────────────────────── */

export function pushToast(t: Omit<Toast, 'id'>) {
  useToastStore.getState().pushToast(t);
}

/* ─── Individual toast item ──────────────────────────────────────────────────── */

const VARIANT_STYLES: Record<ToastVariant, string> = {
  success: 'bg-surface-elevated border-accent-teal/40 text-on-dark',
  error: 'bg-surface-elevated border-accent-danger/40 text-on-dark',
  info: 'bg-surface-elevated border-brand/40 text-on-dark',
};

const VARIANT_ICONS: Record<ToastVariant, typeof CheckCircle2> = {
  success: CheckCircle2,
  error: XCircle,
  info: Info,
};

const ICON_COLORS: Record<ToastVariant, string> = {
  success: 'text-accent-teal',
  error: 'text-accent-danger',
  info: 'text-brand',
};

const AUTO_DISMISS_MS = 4500;

function ToastItem({ toast }: { toast: Toast }) {
  const dismiss = useToastStore((s) => s.dismissToast);
  const Icon = VARIANT_ICONS[toast.variant];

  useEffect(() => {
    const timer = setTimeout(() => dismiss(toast.id), AUTO_DISMISS_MS);
    return () => clearTimeout(timer);
  }, [toast.id, dismiss]);

  return (
    <div
      role="alert"
      aria-live="polite"
      className={`
        flex items-start gap-3 px-4 py-3.5 rounded-[12px] border min-w-[280px] max-w-[360px]
        shadow-lg animate-in slide-in-from-bottom-2 fade-in duration-200
        ${VARIANT_STYLES[toast.variant]}
      `}
    >
      <Icon size={18} className={`shrink-0 mt-0.5 ${ICON_COLORS[toast.variant]}`} />
      <p className="text-body-sm flex-1 leading-snug">{toast.message}</p>
      <button
        onClick={() => dismiss(toast.id)}
        className="shrink-0 ml-1 text-on-dark-mute hover:text-on-dark transition-colors"
        aria-label="Cerrar notificación"
      >
        <X size={14} />
      </button>
    </div>
  );
}

/* ─── Viewport (mount this in AppLayout) ────────────────────────────────────── */

export function ToastViewport() {
  const toasts = useToastStore((s) => s.toasts);

  if (toasts.length === 0) return null;

  return (
    <div
      aria-label="Notificaciones"
      className="
        fixed z-[200]
        bottom-6 right-6 sm:bottom-6 sm:right-6
        left-4 right-4 sm:left-auto
        flex flex-col items-center sm:items-end gap-2
        pointer-events-none
      "
    >
      {toasts.map((t) => (
        <div key={t.id} className="pointer-events-auto">
          <ToastItem toast={t} />
        </div>
      ))}
    </div>
  );
}
