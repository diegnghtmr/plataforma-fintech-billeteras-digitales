import type { HTMLAttributes } from 'react';

type CardVariant = 'light' | 'dark' | 'featured';

interface CardProps extends HTMLAttributes<HTMLDivElement> {
  variant?: CardVariant;
}

const VARIANTS: Record<CardVariant, string> = {
  /** feature-card-light: white card with hairline border */
  light:
    'bg-surface-card border border-hairline-light rounded-[20px] p-8',
  /** feature-card-dark: elevated dark card, no border */
  dark:
    'bg-surface-elevated rounded-[20px] p-8',
  /** plan-card-featured: brand cobalt fill */
  featured:
    'bg-brand rounded-[20px] p-8',
};

export function Card({
  variant = 'light',
  className = '',
  children,
  ...props
}: CardProps) {
  return (
    <div className={`${VARIANTS[variant]} ${className}`} {...props}>
      {children}
    </div>
  );
}
