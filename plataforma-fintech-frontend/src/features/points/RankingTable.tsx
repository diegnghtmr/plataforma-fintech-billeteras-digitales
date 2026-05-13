import { LoyaltyBadge } from './LoyaltyBadge';
import type { RankingItemResponse } from '../../api/points';

interface RankingTableProps {
  items: RankingItemResponse[];
}

export function RankingTable({ items }: RankingTableProps) {
  if (items.length === 0) {
    return <p className="text-surface-fg/60 text-sm">No hay datos de ranking.</p>;
  }

  return (
    <table className="w-full text-sm text-left">
      <thead>
        <tr className="text-surface-fg/60 border-b border-surface-fg/10">
          <th className="py-2 pr-4">#</th>
          <th className="py-2 pr-4">Usuario</th>
          <th className="py-2 pr-4">Puntos</th>
          <th className="py-2">Nivel</th>
        </tr>
      </thead>
      <tbody>
        {items.map((item) => (
          <tr key={item.userId} className="border-b border-surface-fg/5 hover:bg-surface-fg/5">
            <td className="py-2 pr-4 text-surface-fg/60 font-mono">{item.position}</td>
            <td className="py-2 pr-4 text-canvas-fg font-medium">{item.userName}</td>
            <td className="py-2 pr-4 text-canvas-fg font-mono">{item.points.toFixed(2)}</td>
            <td className="py-2">
              <LoyaltyBadge level={item.loyaltyLevel} />
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}
