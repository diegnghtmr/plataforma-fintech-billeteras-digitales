import type { ButtonHTMLAttributes } from 'react';

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'ghost';
}

export function Button({ variant = 'primary', className = '', children, ...props }: ButtonProps) {
  const base = 'px-4 py-2 rounded-full font-medium transition-opacity disabled:opacity-50';
  const variants = {
    primary: 'bg-accent text-accent-fg hover:opacity-90',
    ghost: 'bg-transparent text-surface-fg border border-surface-fg hover:opacity-70',
  };

  return (
    <button className={`${base} ${variants[variant]} ${className}`} {...props}>
      {children}
    </button>
  );
}
