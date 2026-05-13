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

const selectCls =
  'border border-hairline-light rounded-[12px] px-3 py-2 bg-canvas-light text-ink text-sm focus:outline-none focus:border-brand';

function SummaryCards({ summary }: { summary: Record<string, number> | undefined }) {
  if (!summary) {
    return <p className="text-stone text-sm">Cargando resumen...</p>;
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
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
      {cards.map((card) => (
        <div
          key={card.label}
          className="bg-surface-elevated rounded-[20px] p-8 flex flex-col gap-3"
        >
          <p className="text-on-dark-mute text-sm font-semibold uppercase tracking-widest">{card.label}</p>
          <p
            className="text-on-dark font-medium leading-none"
            style={{
              fontFamily: "'Inter Tight', 'Inter', system-ui, sans-serif",
              fontSize: '2.5rem',
            }}
          >
            {card.value}
          </p>
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
    <div className="flex flex-col gap-4">
      <div className="flex items-center justify-between flex-wrap gap-3">
        <h3
          className="text-xl font-medium text-ink"
          style={{ fontFamily: "'Inter Tight', 'Inter', system-ui, sans-serif" }}
        >
          {title}
        </h3>
        <select
          value={limit}
          onChange={(e) => onLimitChange(Number(e.target.value) as LimitOption)}
          className={selectCls}
        >
          {LIMIT_OPTIONS.map((opt) => (
            <option key={opt} value={opt}>{opt}</option>
          ))}
        </select>
      </div>
      {!items || items.length === 0 ? (
        <p className="text-stone text-sm">Sin datos</p>
      ) : (
        <div className="rounded-[20px] border border-hairline-light overflow-hidden">
          <table className="w-full text-sm">
            <thead className="bg-surface-soft">
              <tr className="text-stone border-b border-hairline-light">
                <th className="py-3 px-4 text-left font-semibold tracking-wide">ID</th>
                <th className="py-3 px-4 text-left font-semibold tracking-wide">Nombre</th>
                <th className="py-3 px-4 text-right font-semibold tracking-wide">Valor</th>
              </tr>
            </thead>
            <tbody>
              {items.map((item) => (
                <tr key={item.id} className="border-b border-hairline-light">
                  <td className="py-3 px-4 text-stone font-mono text-xs">{item.id}</td>
                  <td className="py-3 px-4 text-ink">{item.label}</td>
                  <td className="py-3 px-4 text-right text-ink font-semibold">{item.value}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
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
    <div className="flex flex-col gap-4">
      <div className="flex items-center justify-between flex-wrap gap-3">
        <h3
          className="text-xl font-medium text-ink"
          style={{ fontFamily: "'Inter Tight', 'Inter', system-ui, sans-serif" }}
        >
          Rutas Frecuentes
        </h3>
        <div className="flex items-center gap-2">
          <label className="text-stone text-sm">Min. transferencias:</label>
          <input
            type="number"
            min={1}
            value={minTransfers}
            onChange={(e) => onMinTransfersChange(Math.max(1, Number(e.target.value)))}
            className="border border-hairline-light rounded-[12px] px-3 py-2 bg-canvas-light text-ink text-sm focus:outline-none focus:border-brand w-20"
          />
        </div>
      </div>
      {!routes || routes.length === 0 ? (
        <p className="text-stone text-sm">Sin rutas frecuentes</p>
      ) : (
        <div className="rounded-[20px] border border-hairline-light overflow-hidden">
          <table className="w-full text-sm">
            <thead className="bg-surface-soft">
              <tr className="text-stone border-b border-hairline-light">
                <th className="py-3 px-4 text-left font-semibold tracking-wide">Origen</th>
                <th className="py-3 px-4 text-left font-semibold tracking-wide">Destino</th>
                <th className="py-3 px-4 text-right font-semibold tracking-wide">Transferencias</th>
                <th className="py-3 px-4 text-right font-semibold tracking-wide">Monto Total</th>
              </tr>
            </thead>
            <tbody>
              {routes.map((r) => (
                <tr key={`${r.sourceUserId}-${r.targetUserId}`} className="border-b border-hairline-light">
                  <td className="py-3 px-4 text-stone font-mono text-xs">{r.sourceUserId}</td>
                  <td className="py-3 px-4 text-stone font-mono text-xs">{r.targetUserId}</td>
                  <td className="py-3 px-4 text-right text-ink">{r.transferCount}</td>
                  <td className="py-3 px-4 text-right text-ink font-semibold">${r.totalAmount.toFixed(2)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
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
    <>
      {/* Hero — dark band */}
      <section className="bg-canvas-dark py-[88px]">
        <div className="max-w-[1200px] mx-auto px-6 sm:px-8 lg:px-12">
          <h1
            className="text-on-dark font-medium leading-none tracking-tight mb-4"
            style={{
              fontFamily: "'Inter Tight', 'Inter', system-ui, sans-serif",
              fontSize: 'clamp(2.5rem, 6vw, 5rem)',
            }}
          >
            Analítica financiera
          </h1>
          <p className="text-on-dark-mute text-lg max-w-xl">
            Métricas en tiempo real sobre usuarios, billeteras, transacciones y patrones de fraude.
          </p>
        </div>
      </section>

      {/* Summary stat cards — dark band */}
      <section className="bg-canvas-dark pb-[88px]">
        <div className="max-w-[1200px] mx-auto px-6 sm:px-8 lg:px-12 flex flex-col gap-6">
          <h2
            className="text-on-dark-mute text-sm font-semibold uppercase tracking-widest"
            aria-label="Resumen"
          >
            Resumen
          </h2>
          <SummaryCards summary={summary as Record<string, number> | undefined} />
        </div>
      </section>

      {/* Detail sections — light catalogue band */}
      <section className="bg-canvas-light py-[88px]">
        <div className="max-w-[1200px] mx-auto px-6 sm:px-8 lg:px-12 flex flex-col gap-12">

          <MetricTable
            title="Top Usuarios"
            items={topUsers}
            limit={usersLimit}
            onLimitChange={setUsersLimit}
          />

          <MetricTable
            title="Top Billeteras"
            items={topWallets}
            limit={walletsLimit}
            onLimitChange={setWalletsLimit}
          />

          <FrequentRoutesTable
            routes={routes}
            minTransfers={minTransfers}
            onMinTransfersChange={setMinTransfers}
          />

          {/* Top Transactions */}
          <div className="flex flex-col gap-4">
            <div className="flex items-center justify-between flex-wrap gap-3">
              <h3
                className="text-xl font-medium text-ink"
                style={{ fontFamily: "'Inter Tight', 'Inter', system-ui, sans-serif" }}
              >
                Top Transacciones por Valor
              </h3>
              <select
                value={txLimit}
                onChange={(e) => setTxLimit(Number(e.target.value) as LimitOption)}
                className={selectCls}
              >
                {LIMIT_OPTIONS.map((opt) => <option key={opt} value={opt}>{opt}</option>)}
              </select>
            </div>
            {!topTransactions || topTransactions.length === 0 ? (
              <p className="text-stone text-sm">Sin datos</p>
            ) : (
              <div className="rounded-[20px] border border-hairline-light overflow-hidden">
                <table className="w-full text-sm">
                  <thead className="bg-surface-soft">
                    <tr className="text-stone border-b border-hairline-light">
                      <th className="py-3 px-4 text-left font-semibold tracking-wide">ID</th>
                      <th className="py-3 px-4 text-left font-semibold tracking-wide">Tipo</th>
                      <th className="py-3 px-4 text-right font-semibold tracking-wide">Monto</th>
                      <th className="py-3 px-4 text-left font-semibold tracking-wide">Riesgo</th>
                    </tr>
                  </thead>
                  <tbody>
                    {topTransactions.map((tx) => (
                      <tr key={tx.id} className="border-b border-hairline-light">
                        <td className="py-3 px-4 text-stone font-mono text-xs">{tx.id}</td>
                        <td className="py-3 px-4 text-charcoal text-xs">{tx.type}</td>
                        <td className="py-3 px-4 text-right text-ink font-semibold">${tx.amount.toFixed(2)}</td>
                        <td className="py-3 px-4 text-xs text-stone">{tx.riskLevel}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>

          <MetricTable
            title="Movimientos por Tipo"
            items={movementByType}
            limit={10}
            onLimitChange={() => {}}
          />

          <MetricTable
            title="Categorías de Billetera"
            items={walletCategories}
            limit={walletCatLimit}
            onLimitChange={setWalletCatLimit}
          />

          {/* Total Moved in Range */}
          <div className="flex flex-col gap-4">
            <h3
              className="text-xl font-medium text-ink"
              style={{ fontFamily: "'Inter Tight', 'Inter', system-ui, sans-serif" }}
            >
              Total Movido en Rango
            </h3>
            <div className="flex gap-4 flex-wrap">
              <div className="flex flex-col gap-1.5">
                <label className="text-charcoal text-sm font-semibold">Desde</label>
                <input
                  type="datetime-local"
                  value={rangeFrom}
                  onChange={(e) => setRangeFrom(e.target.value)}
                  className="border border-hairline-light rounded-[12px] px-3 h-14 bg-canvas-light text-ink text-sm focus:outline-none focus:border-brand"
                />
              </div>
              <div className="flex flex-col gap-1.5">
                <label className="text-charcoal text-sm font-semibold">Hasta</label>
                <input
                  type="datetime-local"
                  value={rangeTo}
                  onChange={(e) => setRangeTo(e.target.value)}
                  className="border border-hairline-light rounded-[12px] px-3 h-14 bg-canvas-light text-ink text-sm focus:outline-none focus:border-brand"
                />
              </div>
            </div>
            {totalMoved && (
              <div className="bg-surface-card border border-hairline-light rounded-[20px] p-8 flex gap-8">
                <div>
                  <p className="text-stone text-sm font-semibold uppercase tracking-widest">Total</p>
                  <p className="text-ink font-semibold text-2xl mt-1">${totalMoved.totalAmount.toFixed(2)}</p>
                </div>
                <div>
                  <p className="text-stone text-sm font-semibold uppercase tracking-widest">Transacciones</p>
                  <p className="text-ink font-semibold text-2xl mt-1">{totalMoved.count}</p>
                </div>
              </div>
            )}
          </div>

          {/* Cycles */}
          <div className="flex flex-col gap-4">
            <h3
              className="text-xl font-medium text-ink"
              style={{ fontFamily: "'Inter Tight', 'Inter', system-ui, sans-serif" }}
            >
              Ciclos en Grafo de Transferencias
            </h3>
            {!cycles || cycles.length === 0 ? (
              <p className="text-stone text-sm">No se detectaron ciclos</p>
            ) : (
              <ul className="flex flex-col gap-3">
                {cycles.map((cycle, i) => (
                  <li
                    key={i}
                    className="bg-surface-soft rounded-[12px] px-4 py-3 text-sm text-ink font-mono"
                  >
                    {cycle.join(' → ')}
                  </li>
                ))}
              </ul>
            )}
          </div>

        </div>
      </section>
    </>
  );
}
