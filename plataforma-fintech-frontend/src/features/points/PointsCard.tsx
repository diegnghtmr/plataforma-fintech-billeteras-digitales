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
    <div className="bg-surface rounded-lg p-4 flex flex-col gap-2">
      <h3 className="text-surface-fg font-semibold text-sm uppercase tracking-wide">Mis Puntos</h3>
      <div className="flex items-center gap-3">
        <span className="text-canvas-fg text-3xl font-bold">{points.toFixed(2)}</span>
        <LoyaltyBadge level={loyaltyLevel} />
      </div>
      {hint && (
        <p className="text-surface-fg/70 text-sm">{hint}</p>
      )}
    </div>
  );
}
