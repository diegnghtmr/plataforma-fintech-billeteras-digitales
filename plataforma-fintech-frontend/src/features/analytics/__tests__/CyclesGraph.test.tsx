import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { CyclesGraph } from '../CyclesGraph';

// Mock @xyflow/react — requires canvas/ResizeObserver not available in jsdom
vi.mock('@xyflow/react', () => ({
  ReactFlow: ({ nodes }: { nodes: { data: { label: string } }[] }) => (
    <div data-testid="react-flow">
      {nodes.map((n) => (
        <span key={n.data.label}>{n.data.label}</span>
      ))}
    </div>
  ),
  MarkerType: { ArrowClosed: 'arrowclosed' },
}));

describe('CyclesGraph', () => {
  it('renders empty state when no cycles', () => {
    render(<CyclesGraph cycles={[]} />);
    expect(screen.getByText(/no se detectaron ciclos/i)).toBeInTheDocument();
  });

  it('renders cycle panels for each cycle', () => {
    const cycles = [
      ['USR_A', 'USR_B', 'USR_C'],
      ['USR_D', 'USR_E'],
    ];
    render(<CyclesGraph cycles={cycles} />);

    expect(screen.getByText(/ciclo #1 \(3 usuarios\)/i)).toBeInTheDocument();
    expect(screen.getByText(/ciclo #2 \(2 usuarios\)/i)).toBeInTheDocument();
  });

  it('renders nodes for each userId in a cycle', () => {
    const cycles = [['USR_A', 'USR_B', 'USR_C']];
    render(<CyclesGraph cycles={cycles} />);

    expect(screen.getByText('USR_A')).toBeInTheDocument();
    expect(screen.getByText('USR_B')).toBeInTheDocument();
    expect(screen.getByText('USR_C')).toBeInTheDocument();
  });

  it('renders accessibility fallback text', () => {
    const cycles = [['USR_A', 'USR_B']];
    render(<CyclesGraph cycles={cycles} />);

    // The sr-only details contains the cycle text path
    const details = document.querySelector('details');
    expect(details).toBeTruthy();
  });

  it('renders singular "usuario" for single-node cycle', () => {
    const cycles = [['USR_A']];
    render(<CyclesGraph cycles={cycles} />);

    expect(screen.getByText(/ciclo #1 \(1 usuario\)/i)).toBeInTheDocument();
  });
});
