import type { components } from '../../api/generated/schema';

type LoyaltyLevel = components['schemas']['LoyaltyLevel'];

interface LoyaltyBadgeProps {
  level: LoyaltyLevel;
}

const LEVEL_CLASSES: Record<LoyaltyLevel, string> = {
  BRONZE: 'text-accent-brown bg-accent-brown/15',
  SILVER: 'text-stone bg-stone/15',
  GOLD: 'text-accent-yellow bg-accent-yellow/15',
  PLATINUM: 'text-on-brand bg-brand',
};

export function LoyaltyBadge({ level }: LoyaltyBadgeProps) {
  return (
    <span
      role="status"
      aria-label={`Loyalty level: ${level}`}
      className={`inline-flex items-center px-3 py-1 rounded-full text-xs font-semibold tracking-wide ${LEVEL_CLASSES[level]}`}
    >
      {level}
    </span>
  );
}
