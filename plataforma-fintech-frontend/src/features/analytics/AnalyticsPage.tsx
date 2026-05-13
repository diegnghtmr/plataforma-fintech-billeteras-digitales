import { useState } from 'react';
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
  Legend,
} from 'recharts';
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
import { labelOperationType, labelWalletType } from '../../shared/i18n/enum-labels';
import { CyclesGraph } from './CyclesGraph';

const LIMIT_OPTIONS = [5, 10, 25, 50] as const;
type LimitOption = (typeof LIMIT_OPTIONS)[number];

const selectCls =
  'border border-hairline-light rounded-[12px] px-3 py-2 bg-canvas-light text-ink text-sm focus:outline-none focus:border-brand';

// Brand + accent palette for charts
const BRAND_COBALT = '#494fdf';
const PIE_PALETTE = [
  '#00a87e', // teal
  '#376cd5', // blue-link
  '#428619', // light-green
  '#b09000', // yellow
  '#e61e49', // pink
  '#007bc2', // light-blue
];

const tooltipStyle: React.CSSProperties = {
  backgroundColor: '#ffffff',
  border: '1px solid #e2e2e7',
  borderRadius: '8px',
  fontSize: '13px',
  color: '#191c1f',
};

// ---------------------------------------------------------------------------
// Sub-components
// ---------------------------------------------------------------------------

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
          <p className="text-display-md text-on-dark">
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
  formatId,
  formatLabel,
}: {
  title: string;
  items: { id: string; label: string; value: number }[] | undefined;
  limit: LimitOption;
  onLimitChange: (v: LimitOption) => void;
  formatId?: (v: string) => string;
  formatLabel?: (v: string) => string;
}) {
  return (
    <div className="flex flex-col gap-4">
      <div className="flex items-center justify-between flex-wrap gap-3">
        <h3 className="text-heading-sm text-ink">
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
                  <td className="py-3 px-4 text-stone font-mono text-xs">
                    {formatId ? formatId(item.id) : item.id}
                  </td>
                  <td className="py-3 px-4 text-ink">
                    {formatLabel ? formatLabel(item.label) : item.label}
                  </td>
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
        <h3 className="text-heading-sm text-ink">
          Rutas Frecuentes
        </h3>
        <div className="flex items-center gap-2">
          <label className="text-stone text-sm">Min. transferencias:</label>
          <input
            type="number"
            inputMode="numeric"
            min={1}
            step={1}
            value={minTransfers}
            onChange={(e) => onMinTransfersChange(Math.max(1, Number(e.target.value)))}
            onKeyDown={(e) => {
              if (['e', 'E', '+', '-', ',', '.'].includes(e.key)) {
                e.preventDefault();
              }
            }}
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

// ---------------------------------------------------------------------------
// Chart components
// ---------------------------------------------------------------------------

function MovementBarChart({
  items,
}: {
  items: { id: string; label: string; value: number }[] | undefined;
}) {
  if (!items || items.length === 0) {
    return (
      <div className="flex items-center justify-center h-[300px] text-stone text-sm">
        Sin datos
      </div>
    );
  }

  const data = items.map((item) => ({
    name: labelOperationType(item.label),
    cantidad: item.value,
  }));

  return (
    <ResponsiveContainer width="100%" height={300}>
      <BarChart data={data} margin={{ top: 8, right: 16, left: 0, bottom: 56 }}>
        <XAxis
          dataKey="name"
          tick={{ fontSize: 11, fill: '#8d969e' }}
          tickLine={false}
          axisLine={false}
          interval={0}
          angle={-22}
          textAnchor="end"
          height={64}
        />
        <YAxis
          tick={{ fontSize: 11, fill: '#8d969e' }}
          tickLine={false}
          axisLine={false}
          allowDecimals={false}
        />
        <Tooltip
          contentStyle={tooltipStyle}
          cursor={{ fill: '#f4f4f4' }}
        />
        <Bar dataKey="cantidad" fill={BRAND_COBALT} radius={[4, 4, 0, 0]} />
      </BarChart>
    </ResponsiveContainer>
  );
}

function WalletCategoryPieChart({
  items,
}: {
  items: { id: string; label: string; value: number }[] | undefined;
}) {
  if (!items || items.length === 0) {
    return (
      <div className="flex items-center justify-center h-[300px] text-stone text-sm">
        Sin datos
      </div>
    );
  }

  const data = items.map((item) => ({
    name: labelWalletType(item.label),
    value: item.value,
  }));

  return (
    <ResponsiveContainer width="100%" height={300}>
      <PieChart>
        <Pie
          data={data}
          cx="50%"
          cy="50%"
          innerRadius={60}
          outerRadius={100}
          paddingAngle={3}
          dataKey="value"
        >
          {data.map((_entry, index) => (
            <Cell key={`cell-${index}`} fill={PIE_PALETTE[index % PIE_PALETTE.length] as string} />
          ))}
        </Pie>
        <Tooltip contentStyle={tooltipStyle} />
        <Legend
          iconType="circle"
          iconSize={8}
          wrapperStyle={{ fontSize: '12px', color: '#8d969e' }}
        />
      </PieChart>
    </ResponsiveContainer>
  );
}

// ---------------------------------------------------------------------------
// Main page
// ---------------------------------------------------------------------------

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
  // input type="date" yields "YYYY-MM-DD" — anchor "Desde" at 00:00 local and
  // "Hasta" at 23:59:59 local so the day-range is inclusive on both ends.
  const fromIso = rangeFrom ? new Date(`${rangeFrom}T00:00:00`).toISOString() : '';
  const toIso = rangeTo ? new Date(`${rangeTo}T23:59:59`).toISOString() : '';
  const { data: totalMoved, isLoading: totalMovedLoading } = useTotalMovedQuery(fromIso, toIso);

  return (
    <>
      {/* Hero — dark band */}
      <section className="bg-canvas-dark py-[88px]">
        <div className="max-w-[1200px] mx-auto px-6 sm:px-8 lg:px-12">
          <h1 className="text-display-xl text-on-dark mb-4">
            Analítica financiera
          </h1>
          <p className="text-body-lg text-on-dark-mute w-full max-w-[36rem]">
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

      {/* Charts band — light */}
      <section className="bg-canvas-light py-[88px]">
        <div className="max-w-[1200px] mx-auto px-6 sm:px-8 lg:px-12 flex flex-col gap-6">
          <h2 className="text-heading-lg text-ink">Visualizaciones</h2>

          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            {/* Bar chart — Movimientos por Tipo */}
            <div className="bg-surface-card border border-hairline-light rounded-[20px] p-8 flex flex-col gap-4">
              <h3 className="text-heading-sm text-ink">Movimientos por Tipo</h3>
              <MovementBarChart items={movementByType} />
            </div>

            {/* Pie chart — Categorías de Billetera */}
            <div className="bg-surface-card border border-hairline-light rounded-[20px] p-8 flex flex-col gap-4">
              <h3 className="text-heading-sm text-ink">Categorías de Billetera</h3>
              <WalletCategoryPieChart items={walletCategories} />
            </div>
          </div>
        </div>
      </section>

      {/* Detail sections — light catalogue band */}
      <section className="bg-canvas-light pb-[88px]">
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
              <h3 className="text-heading-sm text-ink">
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
            formatId={labelOperationType}
            formatLabel={labelOperationType}
          />

          <MetricTable
            title="Categorías de Billetera"
            items={walletCategories}
            limit={walletCatLimit}
            onLimitChange={setWalletCatLimit}
            formatId={labelWalletType}
            formatLabel={labelWalletType}
          />

          {/* Total Moved in Range */}
          <div className="flex flex-col gap-4">
            <h3 className="text-heading-sm text-ink">
              Total Movido en Rango
            </h3>

            {/* Date filters */}
            <div className="flex gap-4 flex-wrap">
              <div className="flex flex-col gap-1.5">
                <label className="text-charcoal text-sm font-semibold">Desde</label>
                <input
                  type="date"
                  value={rangeFrom}
                  onChange={(e) => setRangeFrom(e.target.value)}
                  className="border border-hairline-light rounded-[12px] px-3 h-14 bg-canvas-light text-ink text-sm focus:outline-none focus:border-brand w-full max-w-[14rem]"
                />
              </div>
              <div className="flex flex-col gap-1.5">
                <label className="text-charcoal text-sm font-semibold">Hasta</label>
                <input
                  type="date"
                  value={rangeTo}
                  onChange={(e) => setRangeTo(e.target.value)}
                  className="border border-hairline-light rounded-[12px] px-3 h-14 bg-canvas-light text-ink text-sm focus:outline-none focus:border-brand w-full max-w-[14rem]"
                />
              </div>
            </div>

            {/* KPI result card */}
            {!rangeFrom || !rangeTo ? (
              <p className="text-stone text-sm">
                Seleccioná un rango de fechas para ver el total movido.
              </p>
            ) : totalMovedLoading ? (
              <p className="text-stone text-sm">Calculando...</p>
            ) : totalMoved ? (
              <div className="bg-surface-elevated rounded-[20px] p-8 flex flex-col sm:flex-row gap-8">
                <div className="flex flex-col gap-2">
                  <p className="text-on-dark-mute text-xs font-semibold uppercase tracking-widest">
                    Total Movido
                  </p>
                  <p className="text-display-md text-on-dark">
                    ${totalMoved.totalAmount.toFixed(2)}
                  </p>
                </div>
                <div className="flex flex-col gap-2">
                  <p className="text-on-dark-mute text-xs font-semibold uppercase tracking-widest">
                    Transacciones
                  </p>
                  <p className="text-display-md text-on-dark">
                    {totalMoved.count}
                  </p>
                </div>
              </div>
            ) : (
              <p className="text-stone text-sm">Sin datos para el rango seleccionado.</p>
            )}
          </div>

          {/* Cycles — interactive graph */}
          <div className="flex flex-col gap-4">
            <h3 className="text-heading-sm text-ink">
              Ciclos en Grafo de Transferencias
            </h3>
            <CyclesGraph cycles={cycles ?? []} />
          </div>

        </div>
      </section>
    </>
  );
}
