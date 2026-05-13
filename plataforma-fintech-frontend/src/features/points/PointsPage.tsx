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
    <>
      {/* Hero — dark storytelling band */}
      <section className="bg-canvas-dark py-[88px]">
        <div className="max-w-[1200px] mx-auto px-6 sm:px-8 lg:px-12">
          <h1
            className="text-on-dark font-medium leading-none tracking-tight mb-4"
            style={{
              fontFamily: "'Inter Tight', 'Inter', system-ui, sans-serif",
              fontSize: 'clamp(2.5rem, 6vw, 5rem)',
            }}
          >
            Tus puntos
          </h1>
          <p className="text-on-dark-mute text-lg max-w-xl">
            Consulta tu nivel de fidelización, puntos acumulados y posición en el ranking global.
          </p>
        </div>
      </section>

      {/* Points card band — dark */}
      {selectedUserId && pointsData && (
        <section className="bg-canvas-dark pb-[88px]">
          <div className="max-w-[1200px] mx-auto px-6 sm:px-8 lg:px-12">
            <div className="max-w-sm">
              <PointsCard
                points={pointsData.points}
                loyaltyLevel={pointsData.loyaltyLevel}
              />
            </div>
          </div>
        </section>
      )}

      {/* Ranking band — light catalogue */}
      <section className="bg-canvas-light py-[88px]">
        <div className="max-w-[1200px] mx-auto px-6 sm:px-8 lg:px-12">
          <div className="flex flex-col gap-6">
            <div className="flex items-center justify-between flex-wrap gap-4">
              <h2
                className="text-2xl font-medium text-ink"
                style={{ fontFamily: "'Inter Tight', 'Inter', system-ui, sans-serif" }}
              >
                Ranking
              </h2>
              <div className="flex items-center gap-2">
                <label htmlFor="limit-select" className="text-stone text-sm">
                  Mostrar:
                </label>
                <select
                  id="limit-select"
                  value={limit}
                  onChange={(e) => setLimit(Number(e.target.value) as LimitOption)}
                  className="border border-hairline-light rounded-[12px] px-3 py-2 bg-canvas-light text-ink text-sm focus:outline-none focus:border-brand"
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
              <p className="text-stone text-sm">Cargando ranking...</p>
            )}
          </div>
        </div>
      </section>
    </>
  );
}
