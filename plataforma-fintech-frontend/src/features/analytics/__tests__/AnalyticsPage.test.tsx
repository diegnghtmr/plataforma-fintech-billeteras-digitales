import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import type { ReactNode } from 'react';
import { AnalyticsPage } from '../AnalyticsPage';

// Mock recharts — SVG/ResizeObserver are not available in jsdom
vi.mock('recharts', () => ({
  ResponsiveContainer: ({ children }: { children: ReactNode }) => <div>{children}</div>,
  BarChart: ({ children }: { children: ReactNode }) => <div>{children}</div>,
  Bar: () => null,
  XAxis: () => null,
  YAxis: () => null,
  Tooltip: () => null,
  PieChart: ({ children }: { children: ReactNode }) => <div>{children}</div>,
  Pie: () => null,
  Cell: () => null,
  Legend: () => null,
}));

// Mock CyclesGraph — ReactFlow requires canvas/ResizeObserver not available in jsdom
vi.mock('../CyclesGraph', () => ({
  CyclesGraph: ({ cycles }: { cycles: string[][] }) =>
    cycles.length === 0 ? (
      <p>No se detectaron ciclos</p>
    ) : (
      <ul>
        {cycles.map((cycle, i) => (
          <li key={i}>{[...cycle, cycle[0]].join(' → ')}</li>
        ))}
      </ul>
    ),
}));

// Mock the hooks module so we don't need real HTTP calls
vi.mock('../hooks', () => ({
  useAnalyticsSummaryQuery: vi.fn(),
  useTopUsersQuery: vi.fn(),
  useTopWalletsQuery: vi.fn(),
  useFrequentRoutesQuery: vi.fn(),
  useTopTransactionsQuery: vi.fn(),
  useCyclesQuery: vi.fn(),
  useTopWalletCategoriesQuery: vi.fn(),
  useMovementByTypeQuery: vi.fn(),
  useTotalMovedQuery: vi.fn(),
}));

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
} from '../hooks';

const MOCK_SUMMARY = {
  totalUsers: 3,
  totalWallets: 5,
  totalTransactions: 10,
  totalMovedAmount: 800.0,
  fraudEventCount: 2,
  unreadNotificationCount: 4,
};

const MOCK_TOP_TX = [
  { id: 'TX-001', type: 'RECHARGE', amount: 15000, riskLevel: 'HIGH' },
  { id: 'TX-002', type: 'WITHDRAWAL', amount: 5000, riskLevel: 'LOW' },
];

const MOCK_MOVEMENT_BY_TYPE = [
  { id: 'RECHARGE', label: 'RECHARGE', value: 12000 },
  { id: 'WITHDRAWAL', label: 'WITHDRAWAL', value: 3000 },
];

const MOCK_WALLET_CATEGORIES = [
  { id: 'SAVINGS', label: 'SAVINGS', value: 5 },
  { id: 'DAILY', label: 'DAILY', value: 3 },
];

const MOCK_CYCLES = [['USR_A', 'USR_B', 'USR_A'], ['USR_C', 'USR_D', 'USR_C']];

const MOCK_TOTAL_MOVED = { totalAmount: 45000.5, count: 8 };

function makeWrapper() {
  const qc = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return ({ children }: { children: ReactNode }) => (
    <QueryClientProvider client={qc}>{children}</QueryClientProvider>
  );
}

describe('AnalyticsPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(useAnalyticsSummaryQuery).mockReturnValue({ data: MOCK_SUMMARY } as any);
    vi.mocked(useTopUsersQuery).mockReturnValue({ data: [{ id: 'USR_A', label: 'Ana', value: 5 }] } as any);
    vi.mocked(useTopWalletsQuery).mockReturnValue({ data: [{ id: 'USR_A/W1', label: 'Savings', value: 10 }] } as any);
    vi.mocked(useFrequentRoutesQuery).mockReturnValue({ data: [] } as any);
    vi.mocked(useTopTransactionsQuery).mockReturnValue({ data: MOCK_TOP_TX } as any);
    vi.mocked(useCyclesQuery).mockReturnValue({ data: [] } as any);
    vi.mocked(useTopWalletCategoriesQuery).mockReturnValue({ data: MOCK_WALLET_CATEGORIES } as any);
    vi.mocked(useMovementByTypeQuery).mockReturnValue({ data: MOCK_MOVEMENT_BY_TYPE } as any);
    vi.mocked(useTotalMovedQuery).mockReturnValue({ data: undefined } as any);
  });

  it('renders page heading', () => {
    render(<AnalyticsPage />, { wrapper: makeWrapper() });
    expect(screen.getByText(/analítica/i)).toBeInTheDocument();
  });

  it('renders summary section', () => {
    render(<AnalyticsPage />, { wrapper: makeWrapper() });
    expect(screen.getAllByText(/resumen/i).length).toBeGreaterThan(0);
  });

  it('renders limit selector for top users', () => {
    render(<AnalyticsPage />, { wrapper: makeWrapper() });
    expect(screen.getAllByRole('combobox').length).toBeGreaterThan(0);
  });

  it('renders empty state without crashing when data is empty', () => {
    vi.mocked(useTopUsersQuery).mockReturnValue({ data: [] } as any);
    vi.mocked(useTopWalletsQuery).mockReturnValue({ data: [] } as any);
    vi.mocked(useFrequentRoutesQuery).mockReturnValue({ data: [] } as any);

    render(<AnalyticsPage />, { wrapper: makeWrapper() });
    expect(document.body).toBeTruthy();
  });

  // W2 — new tests

  it('renders "Top Transacciones por Valor" section with tx rows', () => {
    render(<AnalyticsPage />, { wrapper: makeWrapper() });

    expect(screen.getByText(/top transacciones por valor/i)).toBeInTheDocument();
    expect(screen.getByText('TX-001')).toBeInTheDocument();
    expect(screen.getByText('TX-002')).toBeInTheDocument();
  });

  it('renders "Movimientos por Tipo" section', () => {
    render(<AnalyticsPage />, { wrapper: makeWrapper() });

    expect(screen.getAllByText(/movimientos por tipo/i).length).toBeGreaterThan(0);
    // Labels are translated: RECHARGE → Recarga
    expect(screen.getAllByText('Recarga').length).toBeGreaterThan(0);
  });

  it('renders "Categorías de Billetera" section', () => {
    render(<AnalyticsPage />, { wrapper: makeWrapper() });

    expect(screen.getAllByText(/categorías de billetera/i).length).toBeGreaterThan(0);
    // SAVINGS → Ahorros; DAILY → unknown enum (stays 'DAILY' as fallback)
    expect(screen.getAllByText('Ahorros').length).toBeGreaterThan(0);
  });

  it('renders "Total Movido en Rango" section with date pickers', () => {
    render(<AnalyticsPage />, { wrapper: makeWrapper() });

    expect(screen.getByText(/total movido en rango/i)).toBeInTheDocument();
    // Labels use className text nodes, not htmlFor — query by text
    expect(screen.getByText(/^desde$/i)).toBeInTheDocument();
    expect(screen.getByText(/^hasta$/i)).toBeInTheDocument();
  });

  it('renders "Seleccioná un rango" prompt when dates are empty', () => {
    render(<AnalyticsPage />, { wrapper: makeWrapper() });

    expect(screen.getByText(/seleccioná un rango/i)).toBeInTheDocument();
  });

  it('renders totalMoved KPI card when dates are set and data is present', () => {
    vi.mocked(useTotalMovedQuery).mockReturnValue({
      data: MOCK_TOTAL_MOVED,
      isLoading: false,
    } as any);

    const { container } = render(<AnalyticsPage />, { wrapper: makeWrapper() });

    // Simulate user filling in the date inputs via fireEvent
    const dateInputs = container.querySelectorAll('input[type="date"]');
    const fromInput = dateInputs[0] as HTMLInputElement;
    const toInput = dateInputs[1] as HTMLInputElement;

    // Simulate change events so rangeFrom/rangeTo state updates
    fireEvent.change(fromInput, { target: { value: '2026-01-01' } });
    fireEvent.change(toInput, { target: { value: '2026-12-31' } });

    expect(screen.getByText('$45000.50')).toBeInTheDocument();
    expect(screen.getByText('8')).toBeInTheDocument();
  });

  it('renders "Ciclos en Grafo" section with cycle list', () => {
    vi.mocked(useCyclesQuery).mockReturnValue({ data: MOCK_CYCLES } as any);

    render(<AnalyticsPage />, { wrapper: makeWrapper() });

    expect(screen.getByText(/ciclos en grafo/i)).toBeInTheDocument();
    expect(screen.getByText(/USR_A → USR_B → USR_A/)).toBeInTheDocument();
  });
});
