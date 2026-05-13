import { Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import {
  Zap,
  Trophy,
  ShieldCheck,
  CalendarClock,
  BarChart3,
  Globe,
  ArrowRight,
} from 'lucide-react';
import { getAnalyticsSummary } from '../../api/analytics';

/* ─── Live stats via TanStack Query (single aggregate endpoint) ─────────────── */

function useHomepageStats() {
  return useQuery({
    queryKey: ['analytics', 'summary'],
    queryFn: getAnalyticsSummary,
    staleTime: 60_000,
  });
}

/* ─── Stats strip ───────────────────────────────────────────────────────────── */

function StatCard({ label, value }: { label: string; value: number | null | undefined }) {
  const display = value === null || value === undefined ? '—' : value.toLocaleString();
  return (
    <div className="bg-surface-elevated rounded-[20px] p-8 flex flex-col gap-2">
      <span className="text-heading-md text-on-dark">{display}</span>
      <span className="text-body-sm text-on-dark-mute">{label}</span>
    </div>
  );
}

/* ─── Feature grid ──────────────────────────────────────────────────────────── */

const FEATURES = [
  {
    icon: Zap,
    title: 'Operaciones rápidas',
    description: 'Recargá, retirá y transferí fondos entre billeteras en segundos.',
  },
  {
    icon: Trophy,
    title: 'Puntos y niveles',
    description: 'Acumulá puntos por cada operación y ascendé en el ranking de fidelización.',
  },
  {
    icon: ShieldCheck,
    title: 'Detección de fraude',
    description: 'Monitoreo en tiempo real con alertas automáticas ante eventos sospechosos.',
  },
  {
    icon: CalendarClock,
    title: 'Operaciones programadas',
    description: 'Programá transferencias y recargas para ejecutarse de forma automática.',
  },
  {
    icon: BarChart3,
    title: 'Analítica clara',
    description: 'Visualizá movimientos, rutas frecuentes y el volumen total de tu plataforma.',
  },
  {
    icon: Globe,
    title: 'Transferencias externas',
    description: 'Enviá fondos hacia billeteras externas con trazabilidad completa.',
  },
] as const;

/* ─── Page ──────────────────────────────────────────────────────────────────── */

const STAT_DEFS = [
  { label: 'Usuarios', key: 'totalUsers' },
  { label: 'Billeteras', key: 'totalWallets' },
  { label: 'Transacciones', key: 'totalTransactions' },
  { label: 'Pendientes', key: 'pendingScheduledOperations' },
] as const;

export function HomePage() {
  const { data: summary, isError } = useHomepageStats();

  return (
    <>
      {/* ── Hero band — dark canvas ──────────────────────────────────────────── */}
      <section className="bg-canvas-dark py-[clamp(80px,12vw,160px)]">
        <div className="max-w-[1200px] mx-auto px-6 sm:px-8 lg:px-12">
          <p className="text-eyebrow text-on-dark-mute mb-6">Plataforma Fintech</p>

          <h1 className="text-display-xxl text-on-dark mb-8 max-w-[14ch]">
            Tu dinero.{' '}
            <br />
            Tu billetera digital.
          </h1>

          <p className="text-body-lg text-on-dark-mute mb-10 max-w-[60ch]">
            Gestioná usuarios, billeteras y operaciones financieras desde una
            sola plataforma. Segura, rápida y transparente.
          </p>

          <div className="flex flex-wrap gap-4">
            <Link
              to="/users"
              className="inline-flex items-center gap-2 rounded-full bg-canvas-light text-canvas-dark text-button-md px-7 py-3.5 h-12 hover:bg-faint transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand focus-visible:ring-offset-2 focus-visible:ring-offset-canvas-dark"
            >
              Empezar ahora
              <ArrowRight size={16} />
            </Link>
            <Link
              to="/operations"
              className="inline-flex items-center gap-2 rounded-full border border-on-dark text-on-dark text-button-md px-7 py-3.5 h-12 hover:opacity-80 transition-opacity focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand focus-visible:ring-offset-2 focus-visible:ring-offset-canvas-dark"
            >
              Ver operaciones
            </Link>
          </div>

          {/* Stats strip */}
          <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 mt-16">
            {STAT_DEFS.map(({ label, key }) => (
              <StatCard
                key={label}
                label={label}
                value={isError ? null : summary?.[key]}
              />
            ))}
          </div>
        </div>
      </section>

      {/* ── Feature catalogue band — light ───────────────────────────────────── */}
      <section className="bg-canvas-light py-[clamp(64px,10vw,120px)]">
        <div className="max-w-[1200px] mx-auto px-6 sm:px-8 lg:px-12">
          <p className="text-eyebrow text-mute mb-5">Capacidades</p>
          <h2 className="text-display-lg text-ink mb-16 max-w-[22ch]">
            Todo lo que necesitás, en un lugar.
          </h2>

          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
            {FEATURES.map(({ icon: Icon, title, description }) => (
              <div
                key={title}
                className="bg-surface-card border border-hairline-light rounded-[20px] p-8 flex flex-col gap-4"
              >
                <div className="flex items-center justify-center w-12 h-12 rounded-full bg-surface-soft">
                  <Icon size={22} className="text-ink" strokeWidth={1.5} />
                </div>
                <p className="text-heading-sm text-ink">{title}</p>
                <p className="text-body-md text-charcoal">{description}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* ── Closing CTA band — dark ───────────────────────────────────────────── */}
      <section className="bg-canvas-dark py-[clamp(80px,12vw,160px)]">
        <div className="max-w-[1200px] mx-auto px-6 sm:px-8 lg:px-12 flex flex-col gap-8">
          <h2 className="text-display-lg text-on-dark max-w-[18ch]">
            Empezá a operar hoy mismo.
          </h2>
          <p className="text-body-lg text-on-dark-mute max-w-[50ch]">
            Creá tu primer usuario, configurá billeteras y comenzá a mover fondos
            en minutos.
          </p>
          <div>
            <Link
              to="/users"
              className="inline-flex items-center gap-2 rounded-full bg-canvas-light text-canvas-dark text-button-md px-7 py-3.5 h-12 hover:bg-faint transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand focus-visible:ring-offset-2 focus-visible:ring-offset-canvas-dark"
            >
              Crear primer usuario
              <ArrowRight size={16} />
            </Link>
          </div>
        </div>
      </section>
    </>
  );
}
