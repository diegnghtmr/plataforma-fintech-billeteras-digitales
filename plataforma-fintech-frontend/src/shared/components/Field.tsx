import type { ReactNode } from 'react';
import { AlertCircle, CheckCircle2 } from 'lucide-react';

interface FieldProps {
  label: string;
  error?: string | undefined;
  success?: boolean;
  children: ReactNode;
}

export function Field({ label, error, success, children }: FieldProps) {
  return (
    <div className="flex flex-col gap-1.5">
      <label className="text-charcoal text-body-sm font-semibold tracking-wide">{label}</label>
      <div
        className={`rounded-md ${
          error
            ? 'border-l-4 border-l-accent-danger'
            : success
              ? 'border-l-4 border-l-accent-teal'
              : ''
        }`}
      >
        {children}
      </div>
      {error && (
        <span className="flex items-center gap-1.5 text-caption text-accent-danger">
          <AlertCircle size={13} className="shrink-0" />
          {error}
        </span>
      )}
      {!error && success && (
        <span className="flex items-center gap-1.5 text-caption text-accent-teal">
          <CheckCircle2 size={13} className="shrink-0" />
          Correcto
        </span>
      )}
    </div>
  );
}
