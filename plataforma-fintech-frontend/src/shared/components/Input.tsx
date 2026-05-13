import { forwardRef } from 'react';
import type { InputHTMLAttributes } from 'react';

interface InputProps extends InputHTMLAttributes<HTMLInputElement> {}

export const Input = forwardRef<HTMLInputElement, InputProps>(
  ({ className = '', ...props }, ref) => {
    return (
      <input
        ref={ref}
        className={`w-full bg-surface text-surface-fg border border-surface-fg/30 rounded-lg px-3 py-2 focus:outline-none focus:border-accent ${className}`}
        {...props}
      />
    );
  }
);

Input.displayName = 'Input';
