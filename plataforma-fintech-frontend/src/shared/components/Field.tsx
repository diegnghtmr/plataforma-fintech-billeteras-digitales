import type { ReactNode } from 'react';

interface FieldProps {
  label: string;
  error?: string | undefined;
  children: ReactNode;
}

export function Field({ label, error, children }: FieldProps) {
  return (
    <div className="flex flex-col gap-1">
      <label className="text-surface-fg text-sm font-medium">{label}</label>
      {children}
      {error && <span className="text-danger text-xs">{error}</span>}
    </div>
  );
}
