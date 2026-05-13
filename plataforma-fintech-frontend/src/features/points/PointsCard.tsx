import { LoyaltyBadge } from './LoyaltyBadge';
import type { components } from '../../api/generated/schema';

type LoyaltyLevel = components['schemas']['LoyaltyLevel'];

interface PointsCardProps {
  points: number;
  loyaltyLevel: LoyaltyLevel;
}

const NEXT_LEVEL: Partial<Record<LoyaltyLevel, { level: LoyaltyLevel; threshold: number }>> = {
  BRONZE: { level: 'SILVER', threshold: 1000 },
  SILVER: { level: 'GOLD', threshold: 5000 },
  GOLD: { level: 'PLATINUM', threshold: 15000 },
};

export function PointsCard({ points, loyaltyLevel }: PointsCardProps) {
  const next = NEXT_LEVEL[loyaltyLevel];
  const hint = next ? `Faltan ${(next.threshold - points).toFixed(0)} puntos para ${next.level}` : null;

  return (
    <div className="w-full bg-surface-elevated rounded-[20px] p-8 flex flex-col gap-4">
      <h3
        className="text-on-dark-mute text-sm font-semibold uppercase tracking-widest"
      >
        Mis Puntos
      </h3>
      <div className="flex items-center gap-4">
        <span className="text-display-md text-on-dark">
          {points.toFixed(2)}
        </span>
        <LoyaltyBadge level={loyaltyLevel} />
      </div>
      {hint && (
        <p className="text-on-dark-mute text-sm w-full">{hint}</p>
      )}
    </div>
  );
}
