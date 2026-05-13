import { useEffect, useRef, type ReactNode } from 'react';
import { X } from 'lucide-react';
import { Button } from './Button';

interface ModalProps {
  open: boolean;
  onClose: () => void;
  title: string;
  description?: string;
  children?: ReactNode;
  confirmLabel?: string;
  cancelLabel?: string;
  onConfirm?: () => void;
  tone?: 'default' | 'danger';
  isPending?: boolean;
}

export function Modal({
  open,
  onClose,
  title,
  description,
  children,
  confirmLabel = 'Confirmar',
  cancelLabel = 'Cancelar',
  onConfirm,
  tone = 'default',
  isPending = false,
}: ModalProps) {
  const firstFocusRef = useRef<HTMLButtonElement>(null);

  // Move focus to the first interactive element on open
  useEffect(() => {
    if (open) {
      requestAnimationFrame(() => firstFocusRef.current?.focus());
    }
  }, [open]);

  // Close on ESC key
  useEffect(() => {
    if (!open) return;
    function handleKeyDown(e: KeyboardEvent) {
      if (e.key === 'Escape') onClose();
    }
    document.addEventListener('keydown', handleKeyDown);
    return () => document.removeEventListener('keydown', handleKeyDown);
  }, [open, onClose]);

  if (!open) return null;

  return (
    <>
      {/* Backdrop */}
      <div
        className="fixed inset-0 z-[100] bg-canvas-dark/60 backdrop-blur-sm"
        aria-hidden="true"
        onClick={onClose}
      />

      {/* Dialog box */}
      <div
        role="dialog"
        aria-modal="true"
        aria-labelledby="modal-title"
        className="
          fixed inset-0 z-[101] flex items-center justify-center p-4
          pointer-events-none
        "
      >
        <div
          className="
            pointer-events-auto w-full max-w-md bg-surface-card rounded-[20px] p-8
            border border-hairline-light flex flex-col gap-6
          "
          onClick={(e) => e.stopPropagation()}
        >
          {/* Header */}
          <div className="flex items-start justify-between gap-4">
            <div className="flex flex-col gap-1.5">
              <h2 id="modal-title" className="text-heading-sm text-ink">{title}</h2>
              {description && (
                <p className="text-body-sm text-stone">{description}</p>
              )}
            </div>
            <button
              onClick={onClose}
              className="shrink-0 inline-flex items-center justify-center w-8 h-8 rounded-full text-stone hover:text-ink hover:bg-surface-soft transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand focus-visible:ring-offset-2 focus-visible:ring-offset-canvas-light"
              aria-label="Cerrar"
            >
              <X size={16} />
            </button>
          </div>

          {/* Body */}
          {children && <div className="text-body-md text-ink">{children}</div>}

          {/* Footer */}
          {onConfirm && (
            <div className="flex gap-3 justify-end">
              <Button
                ref={firstFocusRef}
                variant="outline-light"
                onClick={onClose}
                className="h-10 px-5 text-sm"
              >
                {cancelLabel}
              </Button>
              <Button
                variant={tone === 'danger' ? 'soft' : 'dark'}
                onClick={onConfirm}
                disabled={isPending}
                className={`h-10 px-5 text-sm ${tone === 'danger' ? 'text-accent-danger' : ''}`}
              >
                {isPending ? 'Procesando...' : confirmLabel}
              </Button>
            </div>
          )}
        </div>
      </div>
    </>
  );
}
