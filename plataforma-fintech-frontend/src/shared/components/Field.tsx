import type { ReactNode } from 'react';

interface FieldProps {
  label: string;
  error?: string | undefined;
  children: ReactNode;
}

export function Field({ label, error, children }: FieldProps) {
  return (
    <div className="flex flex-col gap-1.5">
      <label className="text-charcoal text-sm font-semibold tracking-wide">{label}</label>
      {children}
      {error && (
        <span className="text-accent-danger text-xs">{error}</span>
      )}
    </div>
  );
}
