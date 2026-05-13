import { Crown, Award, Star, Gem } from 'lucide-react';
import type { components } from '../../api/generated/schema';
import { labelLoyaltyLevel } from '../../shared/i18n/enum-labels';

type LoyaltyLevel = components['schemas']['LoyaltyLevel'];

interface LoyaltyBadgeProps {
  level: LoyaltyLevel;
}

const LEVEL_CONFIG: Record<LoyaltyLevel, { className: string; Icon: typeof Crown }> = {
  BRONZE: { className: 'text-accent-brown bg-accent-brown/15', Icon: Award },
  SILVER: { className: 'text-stone bg-stone/15', Icon: Star },
  GOLD: { className: 'text-accent-yellow bg-accent-yellow/15', Icon: Crown },
  PLATINUM: { className: 'text-on-brand bg-brand', Icon: Gem },
};

export function LoyaltyBadge({ level }: LoyaltyBadgeProps) {
  const { className, Icon } = LEVEL_CONFIG[level];
  return (
    <span
      role="status"
      aria-label={`Loyalty level: ${level}`}
      className={`inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-semibold tracking-wide ${className}`}
    >
      <Icon size={12} strokeWidth={2} />
      {labelLoyaltyLevel(level)}
    </span>
  );
}
