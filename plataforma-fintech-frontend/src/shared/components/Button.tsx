import type { ButtonHTMLAttributes } from 'react';

type ButtonVariant =
  | 'primary'
  | 'dark'
  | 'soft'
  | 'outline-light'
  | 'outline-dark'
  | 'pill-sm'
  | 'ghost';

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: ButtonVariant;
}

const BASE =
  'inline-flex items-center justify-center rounded-full font-semibold tracking-wide transition-opacity disabled:opacity-50 cursor-pointer disabled:cursor-not-allowed';

const VARIANTS: Record<ButtonVariant, string> = {
  /** White pill on dark canvas — primary CTA */
  primary:
    'bg-canvas-light text-canvas-dark text-base leading-snug px-7 py-3.5 h-12 hover:bg-faint',
  /** Dark pill on light canvas */
  dark:
    'bg-canvas-dark text-on-dark text-base leading-snug px-7 py-3.5 h-12 hover:opacity-90',
  /** Soft surface pill — tertiary action on light canvas */
  soft:
    'bg-surface-soft text-ink text-base leading-snug px-7 py-3.5 h-12 hover:opacity-90',
  /** Outlined pill on light canvas */
  'outline-light':
    'bg-canvas-light text-ink border border-hairline-strong text-base leading-snug px-7 py-3.5 h-12 hover:opacity-80',
  /** Outlined pill on dark canvas */
  'outline-dark':
    'bg-canvas-dark text-on-dark border border-on-dark text-base leading-snug px-7 py-3.5 h-12 hover:opacity-80',
  /** Small pill chip — sub-nav / filters */
  'pill-sm':
    'bg-surface-soft text-ink text-sm leading-snug font-semibold px-4 py-2 h-9 hover:opacity-90',
  /** Legacy ghost — kept for backward compatibility */
  ghost:
    'bg-transparent text-ink border border-hairline-strong text-base leading-snug px-7 py-3.5 h-12 hover:opacity-70',
};

export function Button({
  variant = 'primary',
  className = '',
  children,
  ...props
}: ButtonProps) {
  return (
    <button className={`${BASE} ${VARIANTS[variant]} ${className}`} {...props}>
      {children}
    </button>
  );
}
