import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import type { ReactNode } from 'react';
import { PointsPage } from '../PointsPage';

// Mock hooks
vi.mock('../hooks', () => ({
  useUserPointsQuery: vi.fn(),
  usePointsRankingQuery: vi.fn(),
}));

// Mock selection store
vi.mock('../../../stores/use-selection-store', () => ({
  useSelectionStore: vi.fn(),
}));

import { useUserPointsQuery, usePointsRankingQuery } from '../hooks';
import { useSelectionStore } from '../../../stores/use-selection-store';

const MOCK_POINTS = {
  userId: 'u1',
  points: 1500,
  loyaltyLevel: 'SILVER' as const,
};

const MOCK_RANKING = [
  { position: 1, userId: 'u1', userName: 'Alice', points: 1500, loyaltyLevel: 'SILVER' as const },
  { position: 2, userId: 'u2', userName: 'Bob', points: 800, loyaltyLevel: 'BRONZE' as const },
];

function makeWrapper() {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return ({ children }: { children: ReactNode }) => (
    <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
  );
}

describe('PointsPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(usePointsRankingQuery).mockReturnValue({
      data: MOCK_RANKING,
      isLoading: false,
      isError: false,
    } as any);
  });

  // Scenario 1: no user selected → PointsCard NOT rendered, RankingTable IS rendered
  it('with no selectedUserId: no PointsCard, RankingTable visible', () => {
    vi.mocked(useSelectionStore).mockReturnValue(null);
    vi.mocked(useUserPointsQuery).mockReturnValue({ data: undefined, isLoading: false } as any);

    render(<PointsPage />, { wrapper: makeWrapper() });

    // RankingTable should be visible (Alice in ranking)
    expect(screen.getByText('Alice')).toBeInTheDocument();
    // PointsCard should NOT be visible (no points balance shown)
    expect(screen.queryByText(/faltan/i)).not.toBeInTheDocument();
  });

  // Scenario 2: user selected → PointsCard rendered with points + badge + threshold hint
  it('with selectedUserId: PointsCard shows points, badge, and threshold hint', () => {
    vi.mocked(useSelectionStore).mockReturnValue('u1');
    vi.mocked(useUserPointsQuery).mockReturnValue({ data: MOCK_POINTS, isLoading: false } as any);

    render(<PointsPage />, { wrapper: makeWrapper() });

    // 1500.00 appears in both PointsCard and RankingTable
    expect(screen.getAllByText('1500.00').length).toBeGreaterThanOrEqual(1);
    // SILVER badge appears (may appear in both PointsCard and ranking row)
    expect(screen.getAllByText('SILVER').length).toBeGreaterThanOrEqual(1);
    // Hint: Faltan X para GOLD
    expect(screen.getByText(/faltan/i)).toBeInTheDocument();
  });

  // Scenario 3: PLATINUM user → no threshold hint
  it('PLATINUM user shows no threshold hint', () => {
    vi.mocked(useSelectionStore).mockReturnValue('u1');
    vi.mocked(useUserPointsQuery).mockReturnValue({
      data: { userId: 'u1', points: 20000, loyaltyLevel: 'PLATINUM' as const },
      isLoading: false,
    } as any);

    render(<PointsPage />, { wrapper: makeWrapper() });

    expect(screen.queryByText(/faltan/i)).not.toBeInTheDocument();
  });

  // Scenario 4: limit selector changes query
  it('limit selector triggers re-query with new limit', () => {
    vi.mocked(useSelectionStore).mockReturnValue(null);
    vi.mocked(useUserPointsQuery).mockReturnValue({ data: undefined, isLoading: false } as any);

    render(<PointsPage />, { wrapper: makeWrapper() });

    // Select 25 from limit selector
    const select = screen.getByRole('combobox');
    fireEvent.change(select, { target: { value: '25' } });

    expect(usePointsRankingQuery).toHaveBeenCalledWith(25);
  });
});
