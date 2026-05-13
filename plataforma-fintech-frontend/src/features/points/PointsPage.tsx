import { useState } from 'react';
import { useSelectionStore } from '../../stores/use-selection-store';
import { useUserPointsQuery, usePointsRankingQuery } from './hooks';
import { PointsCard } from './PointsCard';
import { RankingTable } from './RankingTable';

const LIMIT_OPTIONS = [10, 25, 50, 100] as const;
type LimitOption = (typeof LIMIT_OPTIONS)[number];

export function PointsPage() {
  const [limit, setLimit] = useState<LimitOption>(10);
  const selectedUserId = useSelectionStore((s) => s.selectedUserId);

  const { data: pointsData } = useUserPointsQuery(selectedUserId ?? undefined);
  const { data: rankingData } = usePointsRankingQuery(limit);

  return (
    <div className="flex flex-col gap-6 max-w-2xl">
      <h2 className="text-canvas-fg text-xl font-bold">Puntos y Ranking</h2>

      {selectedUserId && pointsData && (
        <PointsCard
          points={pointsData.points}
          loyaltyLevel={pointsData.loyaltyLevel}
        />
      )}

      <div className="flex flex-col gap-3">
        <div className="flex items-center justify-between">
          <h3 className="text-canvas-fg font-semibold">Ranking</h3>
          <div className="flex items-center gap-2">
            <label htmlFor="limit-select" className="text-surface-fg/70 text-sm">
              Mostrar:
            </label>
            <select
              id="limit-select"
              value={limit}
              onChange={(e) => setLimit(Number(e.target.value) as LimitOption)}
              className="bg-surface text-surface-fg text-sm rounded px-2 py-1 border border-surface-fg/20"
            >
              {LIMIT_OPTIONS.map((opt) => (
                <option key={opt} value={opt}>
                  {opt}
                </option>
              ))}
            </select>
          </div>
        </div>

        {rankingData ? (
          <RankingTable items={rankingData} />
        ) : (
          <p className="text-surface-fg/60 text-sm">Cargando ranking...</p>
        )}
      </div>
    </div>
  );
}
