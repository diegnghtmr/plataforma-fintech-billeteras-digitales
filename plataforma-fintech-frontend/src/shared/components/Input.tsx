import { forwardRef } from 'react';
import type { InputHTMLAttributes } from 'react';

interface InputProps extends InputHTMLAttributes<HTMLInputElement> {}

export const Input = forwardRef<HTMLInputElement, InputProps>(
  ({ className = '', ...props }, ref) => {
    return (
      <input
        ref={ref}
        className={`w-full bg-canvas-light text-ink border border-hairline-light rounded-[12px] px-4 h-14 text-base leading-snug tracking-wide focus:outline-none focus:border-brand focus:ring-1 focus:ring-brand ${className}`}
        {...props}
      />
    );
  }
);

Input.displayName = 'Input';
