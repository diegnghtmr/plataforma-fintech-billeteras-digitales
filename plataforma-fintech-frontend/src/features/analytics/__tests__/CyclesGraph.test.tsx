import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { CyclesGraph } from '../CyclesGraph';

// Mock @xyflow/react — requires canvas/ResizeObserver not available in jsdom
vi.mock('@xyflow/react', () => ({
  ReactFlow: ({ nodes }: { nodes: { data: { userId?: string; label?: string } }[] }) => (
    <div data-testid="react-flow">
      {nodes.map((n) => {
        const text = n.data.userId ?? n.data.label ?? '';
        return <span key={text}>{text}</span>;
      })}
    </div>
  ),
  Background: () => null,
  BackgroundVariant: { Dots: 'dots', Lines: 'lines', Cross: 'cross' },
  Handle: () => null,
  Position: { Top: 'top', Bottom: 'bottom', Left: 'left', Right: 'right' },
  MarkerType: { ArrowClosed: 'arrowclosed' },
}));

describe('CyclesGraph', () => {
  it('renders empty state when no cycles', () => {
    render(<CyclesGraph cycles={[]} />);
    expect(screen.getByText(/sin ciclos detectados/i)).toBeInTheDocument();
  });

  it('renders cycle panels for each cycle', () => {
    const cycles = [
      ['USR_A', 'USR_B', 'USR_C'],
      ['USR_D', 'USR_E'],
    ];
    render(<CyclesGraph cycles={cycles} />);

    expect(screen.getByText(/ciclo #1/i)).toBeInTheDocument();
    expect(screen.getByText(/3 usuarios/i)).toBeInTheDocument();
    expect(screen.getByText(/ciclo #2/i)).toBeInTheDocument();
    expect(screen.getByText(/2 usuarios/i)).toBeInTheDocument();
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

    const details = document.querySelector('details');
    expect(details).toBeTruthy();
  });

  it('renders singular "usuario" for single-node cycle', () => {
    const cycles = [['USR_A']];
    render(<CyclesGraph cycles={cycles} />);

    expect(screen.getByText(/ciclo #1/i)).toBeInTheDocument();
    expect(screen.getByText(/1 usuario/i)).toBeInTheDocument();
  });

  it('uses grid layout when there are multiple cycles', () => {
    const cycles = [
      ['USR_A', 'USR_B'],
      ['USR_C', 'USR_D'],
    ];
    const { container } = render(<CyclesGraph cycles={cycles} />);
    // Grid wrapper exists
    const grid = container.querySelector('.grid');
    expect(grid).toBeTruthy();
  });
});
