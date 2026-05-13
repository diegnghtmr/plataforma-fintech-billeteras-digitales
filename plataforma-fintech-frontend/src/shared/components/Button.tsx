import type { ButtonHTMLAttributes, Ref } from 'react';

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
  ref?: Ref<HTMLButtonElement>;
}

const BASE =
  'inline-flex items-center justify-center gap-2 rounded-full font-semibold tracking-wide transition-opacity disabled:opacity-50 cursor-pointer disabled:cursor-not-allowed focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand focus-visible:ring-offset-2 focus-visible:ring-offset-canvas-light';

const VARIANTS: Record<ButtonVariant, string> = {
  /** White pill on dark canvas — primary CTA */
  primary:
    'bg-canvas-light text-canvas-dark text-button-md px-7 py-3.5 h-12 hover:bg-faint',
  /** Dark pill on light canvas */
  dark:
    'bg-canvas-dark text-on-dark text-button-md px-7 py-3.5 h-12 hover:opacity-90',
  /** Soft surface pill — tertiary action on light canvas */
  soft:
    'bg-surface-soft text-ink text-button-md px-7 py-3.5 h-12 hover:opacity-90',
  /** Outlined pill on light canvas */
  'outline-light':
    'bg-canvas-light text-ink border border-hairline-strong text-button-md px-7 py-3.5 h-12 hover:opacity-80',
  /** Outlined pill on dark canvas */
  'outline-dark':
    'bg-canvas-dark text-on-dark border border-on-dark text-button-md px-7 py-3.5 h-12 hover:opacity-80',
  /** Small pill chip — sub-nav / filters */
  'pill-sm':
    'bg-surface-soft text-ink text-button-sm px-4 py-2 h-9 hover:opacity-90',
  /** Legacy ghost — kept for backward compatibility */
  ghost:
    'bg-transparent text-ink border border-hairline-strong text-button-md px-7 py-3.5 h-12 hover:opacity-70',
};

export function Button({
  variant = 'primary',
  className = '',
  children,
  ref,
  ...props
}: ButtonProps) {
  return (
    <button ref={ref} className={`${BASE} ${VARIANTS[variant]} ${className}`} {...props}>
      {children}
    </button>
  );
}
