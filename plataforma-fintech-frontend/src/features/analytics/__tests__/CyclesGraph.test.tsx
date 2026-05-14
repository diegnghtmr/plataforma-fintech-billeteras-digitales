import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { CyclesGraph } from '../CyclesGraph';

// Mock react-force-graph-2d — requires canvas/WebGL not available in jsdom
vi.mock('react-force-graph-2d', () => ({
  default: ({
    graphData,
    onNodeClick,
    onBackgroundClick,
  }: {
    graphData: { nodes: { id?: string | number; cycleIndex?: number }[]; links: unknown[] };
    onNodeClick?: (node: { id?: string | number }) => void;
    onBackgroundClick?: () => void;
  }) => (
    <canvas
      data-testid="force-graph-2d"
      data-node-count={graphData.nodes.length}
      data-link-count={graphData.links.length}
      data-nodes={JSON.stringify(graphData.nodes.map((n) => n.id))}
      onClick={(e) => {
        const target = e.target as HTMLElement;
        if (target.dataset.nodeId) {
          onNodeClick?.({ id: target.dataset.nodeId });
        } else {
          onBackgroundClick?.();
        }
      }}
    />
  ),
}));

describe('CyclesGraph', () => {
  it('renders empty state when no cycles', () => {
    render(<CyclesGraph cycles={[]} />);
    expect(screen.getByText(/sin ciclos detectados/i)).toBeInTheDocument();
  });

  it('renders zero canvas elements for empty cycles', () => {
    const { container } = render(<CyclesGraph cycles={[]} />);
    expect(container.querySelectorAll('canvas').length).toBe(0);
  });

  it('renders panel headers for each cycle', () => {
    const cycles = [
      ['USR_A', 'USR_B', 'USR_C'],
      ['USR_D', 'USR_E'],
    ];
    render(<CyclesGraph cycles={cycles} />);
    expect(screen.getByText(/ciclo #1/i)).toBeInTheDocument();
    expect(screen.getByText(/ciclo #2/i)).toBeInTheDocument();
  });

  it('renders correct user count chips', () => {
    const cycles = [
      ['USR_A', 'USR_B', 'USR_C'],
      ['USR_D', 'USR_E'],
    ];
    render(<CyclesGraph cycles={cycles} />);
    expect(screen.getByText(/3 usuarios/i)).toBeInTheDocument();
    expect(screen.getByText(/2 usuarios/i)).toBeInTheDocument();
  });

  it('renders singular "usuario" for single-node cycle', () => {
    const cycles = [['USR_A']];
    render(<CyclesGraph cycles={cycles} />);
    expect(screen.getByText(/1 usuario/i)).toBeInTheDocument();
  });

  it('renders sr-only details with cycle text for each cycle', () => {
    const cycles = [['A', 'B', 'C']];
    render(<CyclesGraph cycles={cycles} />);

    const detailsEl = document.querySelector('details');
    expect(detailsEl).toBeTruthy();
    expect(detailsEl!.textContent).toContain('A → B → C → A');
  });

  it('uses stacked layout wrapper when multiple cycles', () => {
    const cycles = [
      ['USR_A', 'USR_B'],
      ['USR_C', 'USR_D'],
    ];
    const { container } = render(<CyclesGraph cycles={cycles} />);
    const wrapper = container.querySelector('.flex.flex-col');
    expect(wrapper).toBeTruthy();
    expect(wrapper!.children.length).toBe(2);
  });

  it('renders correct node count data attribute on canvas', () => {
    const cycles = [['A', 'B', 'C']];
    render(<CyclesGraph cycles={cycles} />);
    const canvas = document.querySelector('[data-testid="force-graph-2d"]');
    expect(canvas).toBeTruthy();
    expect(canvas!.getAttribute('data-node-count')).toBe('3');
    expect(canvas!.getAttribute('data-link-count')).toBe('3');
  });
});
