import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { TransferNetworkGraph } from '../TransferNetworkGraph';

// Mock dagre — no DOM layout needed in tests
vi.mock('dagre', () => {
  const nodePositions: Record<string, { x: number; y: number; width: number; height: number }> = {};
  return {
    default: {
      graphlib: {
        Graph: class {
          setGraph() {}
          setDefaultEdgeLabel() {}
          setNode(id: string, attrs: { width: number; height: number }) {
            nodePositions[id] = { x: 100, y: 100, ...attrs };
          }
          setEdge() {}
          node(id: string) {
            return nodePositions[id] ?? { x: 100, y: 100, width: 88, height: 72 };
          }
        },
      },
      layout() {},
    },
  };
});

// Mock @xyflow/react — canvas/ResizeObserver not available in jsdom
vi.mock('@xyflow/react', () => ({
  ReactFlow: ({ nodes }: { nodes: { data: { userId?: string } }[] }) => (
    <div data-testid="react-flow">
      {nodes.map((n) => (
        <span key={n.data.userId}>{n.data.userId}</span>
      ))}
    </div>
  ),
  Background: () => null,
  BackgroundVariant: { Dots: 'dots', Lines: 'lines', Cross: 'cross' },
  Handle: () => null,
  Position: { Top: 'top', Bottom: 'bottom', Left: 'left', Right: 'right' },
  MarkerType: { ArrowClosed: 'arrowclosed' },
}));

const MOCK_ROUTES = [
  { sourceUserId: 'USR001', targetUserId: 'USR002', transferCount: 10, totalAmount: 5000 },
  { sourceUserId: 'USR002', targetUserId: 'USR003', transferCount: 3, totalAmount: 1200 },
  { sourceUserId: 'USR003', targetUserId: 'USR001', transferCount: 1, totalAmount: 300 },
];

describe('TransferNetworkGraph', () => {
  it('renders empty state when routes is undefined', () => {
    render(<TransferNetworkGraph routes={undefined} />);
    expect(screen.getByText(/sin rutas frecuentes/i)).toBeInTheDocument();
  });

  it('renders empty state when routes is empty array', () => {
    render(<TransferNetworkGraph routes={[]} />);
    expect(screen.getByText(/sin rutas frecuentes/i)).toBeInTheDocument();
  });

  it('renders the network graph container with routes', () => {
    render(<TransferNetworkGraph routes={MOCK_ROUTES} />);
    expect(screen.getByTestId('react-flow')).toBeInTheDocument();
  });

  it('renders a node for each unique user', () => {
    render(<TransferNetworkGraph routes={MOCK_ROUTES} />);
    expect(screen.getByText('USR001')).toBeInTheDocument();
    expect(screen.getByText('USR002')).toBeInTheDocument();
    expect(screen.getByText('USR003')).toBeInTheDocument();
  });

  it('renders the header with node and edge counts', () => {
    render(<TransferNetworkGraph routes={MOCK_ROUTES} />);
    // Multiple elements contain "red de transferencias" (header span + sr-only summary)
    expect(screen.getAllByText(/red de transferencias/i).length).toBeGreaterThan(0);
    expect(screen.getByText(/3 usuarios/i)).toBeInTheDocument();
    expect(screen.getByText(/3 rutas/i)).toBeInTheDocument();
  });

  it('renders accessibility fallback with route list', () => {
    render(<TransferNetworkGraph routes={MOCK_ROUTES} />);
    const details = document.querySelector('details');
    expect(details).toBeTruthy();
  });

  it('renders legend items', () => {
    render(<TransferNetworkGraph routes={MOCK_ROUTES} />);
    expect(screen.getByText(/ruta normal/i)).toBeInTheDocument();
    expect(screen.getByText(/top 25%/i)).toBeInTheDocument();
    expect(screen.getByText(/ruta más frecuente/i)).toBeInTheDocument();
  });
});
