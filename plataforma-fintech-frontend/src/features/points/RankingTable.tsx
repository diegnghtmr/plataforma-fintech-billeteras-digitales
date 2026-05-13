import { LoyaltyBadge } from './LoyaltyBadge';
import type { RankingItemResponse } from '../../api/points';

interface RankingTableProps {
  items: RankingItemResponse[];
}

export function RankingTable({ items }: RankingTableProps) {
  if (items.length === 0) {
    return <p className="text-stone text-sm">No hay datos de ranking.</p>;
  }

  return (
    <div className="overflow-x-auto rounded-[20px] border border-hairline-light">
      <table className="w-full text-sm text-left">
        <thead className="bg-surface-soft">
          <tr className="text-stone border-b border-hairline-light">
            <th className="py-3 px-4 font-semibold tracking-wide">#</th>
            <th className="py-3 px-4 font-semibold tracking-wide">Usuario</th>
            <th className="py-3 px-4 font-semibold tracking-wide">Puntos</th>
            <th className="py-3 px-4 font-semibold tracking-wide">Nivel</th>
          </tr>
        </thead>
        <tbody>
          {items.map((item) => (
            <tr key={item.userId} className="border-b border-hairline-light hover:bg-surface-soft/50">
              <td className="py-3 px-4 text-stone font-mono">{item.position}</td>
              <td className="py-3 px-4 text-ink font-medium">{item.userName}</td>
              <td className="py-3 px-4 text-ink font-semibold font-mono">{item.points.toFixed(2)}</td>
              <td className="py-3 px-4">
                <LoyaltyBadge level={item.loyaltyLevel} />
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
