import type { components } from '../../api/generated/schema';

type LoyaltyLevel = components['schemas']['LoyaltyLevel'];

interface LoyaltyBadgeProps {
  level: LoyaltyLevel;
}

const LEVEL_CLASSES: Record<LoyaltyLevel, string> = {
  BRONZE: 'text-bronze bg-bronze/20 border-bronze/40',
  SILVER: 'text-silver bg-silver/20 border-silver/40',
  GOLD: 'text-gold bg-gold/20 border-gold/40',
  PLATINUM: 'text-platinum bg-platinum/20 border-platinum/40',
};

export function LoyaltyBadge({ level }: LoyaltyBadgeProps) {
  return (
    <span
      role="status"
      aria-label={`Loyalty level: ${level}`}
      className={`inline-flex items-center px-2 py-0.5 rounded text-xs font-semibold border ${LEVEL_CLASSES[level]}`}
    >
      {level}
    </span>
  );
}
