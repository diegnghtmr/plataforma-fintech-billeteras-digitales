import type { HTMLAttributes } from 'react';

interface CardProps extends HTMLAttributes<HTMLDivElement> {}

export function Card({ className = '', children, ...props }: CardProps) {
  return (
    <div
      className={`bg-surface border border-surface-fg/20 rounded-xl p-4 ${className}`}
      {...props}
    >
      {children}
    </div>
  );
}
