import { useState } from 'react';
import {
  useAnalyticsSummaryQuery,
  useTopUsersQuery,
  useTopWalletsQuery,
  useFrequentRoutesQuery,
  useTopTransactionsQuery,
  useCyclesQuery,
  useTopWalletCategoriesQuery,
  useMovementByTypeQuery,
  useTotalMovedQuery,
} from './hooks';

const LIMIT_OPTIONS = [5, 10, 25, 50] as const;
type LimitOption = (typeof LIMIT_OPTIONS)[number];

function SummaryCards({ summary }: { summary: Record<string, number> | undefined }) {
  if (!summary) {
    return <p className="text-surface-fg/60 text-sm">Cargando resumen...</p>;
  }

  const cards = [
    { label: 'Usuarios', value: summary['totalUsers'] },
    { label: 'Billeteras', value: summary['totalWallets'] },
    { label: 'Transacciones', value: summary['totalTransactions'] },
    { label: 'Monto Movido', value: `$${summary['totalMovedAmount']?.toFixed(2)}` },
    { label: 'Eventos de Fraude', value: summary['fraudEventCount'] },
    { label: 'Notif. no leídas', value: summary['unreadNotificationCount'] },
  ];

  return (
    <div className="grid grid-cols-2 gap-3 sm:grid-cols-3">
      {cards.map((card) => (
        <div
          key={card.label}
          className="bg-surface rounded-lg p-4 border border-surface-fg/10"
        >
          <p className="text-surface-fg/60 text-xs">{card.label}</p>
          <p className="text-canvas-fg font-bold text-xl">{card.value}</p>
        </div>
      ))}
    </div>
  );
}

function MetricTable({
  title,
  items,
  limit,
  onLimitChange,
}: {
  title: string;
  items: { id: string; label: string; value: number }[] | undefined;
  limit: LimitOption;
  onLimitChange: (v: LimitOption) => void;
}) {
  return (
    <div className="flex flex-col gap-2">
      <div className="flex items-center justify-between">
        <h3 className="text-canvas-fg font-semibold">{title}</h3>
        <select
          value={limit}
          onChange={(e) => onLimitChange(Number(e.target.value) as LimitOption)}
          className="bg-surface text-surface-fg text-sm rounded px-2 py-1 border border-surface-fg/20"
        >
          {LIMIT_OPTIONS.map((opt) => (
            <option key={opt} value={opt}>
              {opt}
            </option>
          ))}
        </select>
      </div>
      {!items || items.length === 0 ? (
        <p className="text-surface-fg/60 text-sm">Sin datos</p>
      ) : (
        <table className="w-full text-sm">
          <thead>
            <tr className="text-surface-fg/60 text-left">
              <th className="pb-1">ID</th>
              <th className="pb-1">Nombre</th>
              <th className="pb-1 text-right">Valor</th>
            </tr>
          </thead>
          <tbody>
            {items.map((item) => (
              <tr key={item.id} className="border-t border-surface-fg/10">
                <td className="py-1 text-surface-fg/70 font-mono text-xs">{item.id}</td>
                <td className="py-1 text-canvas-fg">{item.label}</td>
                <td className="py-1 text-right text-canvas-fg font-semibold">{item.value}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}

function FrequentRoutesTable({
  routes,
  minTransfers,
  onMinTransfersChange,
}: {
  routes: { sourceUserId: string; targetUserId: string; transferCount: number; totalAmount: number }[] | undefined;
  minTransfers: number;
  onMinTransfersChange: (v: number) => void;
}) {
  return (
    <div className="flex flex-col gap-2">
      <div className="flex items-center justify-between">
        <h3 className="text-canvas-fg font-semibold">Rutas Frecuentes</h3>
        <div className="flex items-center gap-2">
          <label className="text-surface-fg/70 text-sm">Min. transferencias:</label>
          <input
            type="number"
            min={1}
            value={minTransfers}
            onChange={(e) => onMinTransfersChange(Math.max(1, Number(e.target.value)))}
            className="bg-surface text-surface-fg text-sm rounded px-2 py-1 border border-surface-fg/20 w-16"
          />
        </div>
      </div>
      {!routes || routes.length === 0 ? (
        <p className="text-surface-fg/60 text-sm">Sin rutas frecuentes</p>
      ) : (
        <table className="w-full text-sm">
          <thead>
            <tr className="text-surface-fg/60 text-left">
              <th className="pb-1">Origen</th>
              <th className="pb-1">Destino</th>
              <th className="pb-1 text-right">Transferencias</th>
              <th className="pb-1 text-right">Monto Total</th>
            </tr>
          </thead>
          <tbody>
            {routes.map((r) => (
              <tr key={`${r.sourceUserId}-${r.targetUserId}`} className="border-t border-surface-fg/10">
                <td className="py-1 text-surface-fg/70 font-mono text-xs">{r.sourceUserId}</td>
                <td className="py-1 text-surface-fg/70 font-mono text-xs">{r.targetUserId}</td>
                <td className="py-1 text-right text-canvas-fg">{r.transferCount}</td>
                <td className="py-1 text-right text-canvas-fg">${r.totalAmount.toFixed(2)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}

export function AnalyticsPage() {
  const [usersLimit, setUsersLimit] = useState<LimitOption>(10);
  const [walletsLimit, setWalletsLimit] = useState<LimitOption>(10);
  const [txLimit, setTxLimit] = useState<LimitOption>(10);
  const [walletCatLimit, setWalletCatLimit] = useState<LimitOption>(10);
  const [minTransfers, setMinTransfers] = useState(1);
  const [rangeFrom, setRangeFrom] = useState('');
  const [rangeTo, setRangeTo] = useState('');

  const { data: summary } = useAnalyticsSummaryQuery();
  const { data: topUsers } = useTopUsersQuery(usersLimit);
  const { data: topWallets } = useTopWalletsQuery(walletsLimit);
  const { data: routes } = useFrequentRoutesQuery(minTransfers);
  const { data: topTransactions } = useTopTransactionsQuery(txLimit);
  const { data: cycles } = useCyclesQuery();
  const { data: walletCategories } = useTopWalletCategoriesQuery(walletCatLimit);
  const { data: movementByType } = useMovementByTypeQuery();
  const fromIso = rangeFrom ? new Date(rangeFrom).toISOString() : '';
  const toIso = rangeTo ? new Date(rangeTo).toISOString() : '';
  const { data: totalMoved } = useTotalMovedQuery(fromIso, toIso);

  return (
    <div className="flex flex-col gap-6 max-w-3xl">
      <h2 className="text-canvas-fg text-xl font-bold">Analítica</h2>

      <section className="flex flex-col gap-3">
        <h3 className="text-canvas-fg font-semibold">Resumen</h3>
        <SummaryCards summary={summary as Record<string, number> | undefined} />
      </section>

      <section>
        <MetricTable
          title="Top Usuarios"
          items={topUsers}
          limit={usersLimit}
          onLimitChange={setUsersLimit}
        />
      </section>

      <section>
        <MetricTable
          title="Top Billeteras"
          items={topWallets}
          limit={walletsLimit}
          onLimitChange={setWalletsLimit}
        />
      </section>

      <section>
        <FrequentRoutesTable
          routes={routes}
          minTransfers={minTransfers}
          onMinTransfersChange={setMinTransfers}
        />
      </section>

      {/* Top Transactions */}
      <section className="flex flex-col gap-2">
        <div className="flex items-center justify-between">
          <h3 className="text-canvas-fg font-semibold">Top Transacciones por Valor</h3>
          <select
            value={txLimit}
            onChange={(e) => setTxLimit(Number(e.target.value) as LimitOption)}
            className="bg-surface text-surface-fg text-sm rounded px-2 py-1 border border-surface-fg/20"
          >
            {LIMIT_OPTIONS.map((opt) => <option key={opt} value={opt}>{opt}</option>)}
          </select>
        </div>
        {!topTransactions || topTransactions.length === 0 ? (
          <p className="text-surface-fg/60 text-sm">Sin datos</p>
        ) : (
          <table className="w-full text-sm">
            <thead>
              <tr className="text-surface-fg/60 text-left">
                <th className="pb-1">ID</th>
                <th className="pb-1">Tipo</th>
                <th className="pb-1 text-right">Monto</th>
                <th className="pb-1">Riesgo</th>
              </tr>
            </thead>
            <tbody>
              {topTransactions.map((tx) => (
                <tr key={tx.id} className="border-t border-surface-fg/10">
                  <td className="py-1 text-surface-fg/70 font-mono text-xs">{tx.id}</td>
                  <td className="py-1 text-canvas-fg text-xs">{tx.type}</td>
                  <td className="py-1 text-right text-canvas-fg font-semibold">${tx.amount.toFixed(2)}</td>
                  <td className="py-1 text-xs text-surface-fg/70">{tx.riskLevel}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </section>

      {/* Movement by Type */}
      <section>
        <MetricTable
          title="Movimientos por Tipo"
          items={movementByType}
          limit={10}
          onLimitChange={() => {}}
        />
      </section>

      {/* Top Wallet Categories */}
      <section>
        <MetricTable
          title="Categorías de Billetera"
          items={walletCategories}
          limit={walletCatLimit}
          onLimitChange={setWalletCatLimit}
        />
      </section>

      {/* Total Moved in Range */}
      <section className="flex flex-col gap-2">
        <h3 className="text-canvas-fg font-semibold">Total Movido en Rango</h3>
        <div className="flex gap-3 flex-wrap">
          <div className="flex flex-col gap-1">
            <label className="text-surface-fg/70 text-xs">Desde</label>
            <input
              type="datetime-local"
              value={rangeFrom}
              onChange={(e) => setRangeFrom(e.target.value)}
              className="bg-surface text-surface-fg text-sm rounded px-2 py-1 border border-surface-fg/20"
            />
          </div>
          <div className="flex flex-col gap-1">
            <label className="text-surface-fg/70 text-xs">Hasta</label>
            <input
              type="datetime-local"
              value={rangeTo}
              onChange={(e) => setRangeTo(e.target.value)}
              className="bg-surface text-surface-fg text-sm rounded px-2 py-1 border border-surface-fg/20"
            />
          </div>
        </div>
        {totalMoved && (
          <div className="bg-surface rounded-lg p-4 flex gap-6">
            <div>
              <p className="text-surface-fg/60 text-xs">Total</p>
              <p className="text-canvas-fg font-bold">${totalMoved.totalAmount.toFixed(2)}</p>
            </div>
            <div>
              <p className="text-surface-fg/60 text-xs">Transacciones</p>
              <p className="text-canvas-fg font-bold">{totalMoved.count}</p>
            </div>
          </div>
        )}
      </section>

      {/* Cycles */}
      <section className="flex flex-col gap-2">
        <h3 className="text-canvas-fg font-semibold">Ciclos en Grafo de Transferencias</h3>
        {!cycles || cycles.length === 0 ? (
          <p className="text-surface-fg/60 text-sm">No se detectaron ciclos</p>
        ) : (
          <ul className="flex flex-col gap-2">
            {cycles.map((cycle, i) => (
              <li key={i} className="bg-surface rounded px-3 py-2 text-sm text-canvas-fg font-mono">
                {cycle.join(' → ')}
              </li>
            ))}
          </ul>
        )}
      </section>
    </div>
  );
}
