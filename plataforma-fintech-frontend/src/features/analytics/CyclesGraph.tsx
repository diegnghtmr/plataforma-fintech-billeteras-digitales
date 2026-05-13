import { ReactFlow, type Node, type Edge, MarkerType } from '@xyflow/react';

// ---------------------------------------------------------------------------
// Layout helpers
// ---------------------------------------------------------------------------

const EDGE_COLOR = '#494fdf';
const NODE_RADIUS = 130;
const PANEL_CX = 220;
const PANEL_CY = 160;

function layoutCycle(cycle: string[]): Node[] {
  const step = (2 * Math.PI) / cycle.length;
  return cycle.map((userId, i) => ({
    id: userId,
    position: {
      x: PANEL_CX + NODE_RADIUS * Math.cos(i * step - Math.PI / 2),
      y: PANEL_CY + NODE_RADIUS * Math.sin(i * step - Math.PI / 2),
    },
    data: { label: userId },
    style: {
      background: EDGE_COLOR,
      color: '#ffffff',
      border: '1px solid #3a40c4',
      borderRadius: '9999px',
      padding: '8px 16px',
      fontSize: '13px',
      fontWeight: 600,
      minWidth: 80,
      textAlign: 'center' as const,
    },
  }));
}

function buildEdges(cycle: string[]): Edge[] {
  return cycle.map((userId, i) => {
    const nextId = cycle[(i + 1) % cycle.length]!;
    return {
      id: `${userId}->${nextId}`,
      source: userId,
      target: nextId,
      animated: false,
      style: { stroke: EDGE_COLOR, strokeWidth: 2 },
      markerEnd: {
        type: MarkerType.ArrowClosed,
        color: EDGE_COLOR,
        width: 16,
        height: 16,
      },
    };
  });
}

// ---------------------------------------------------------------------------
// CyclePanel — one ReactFlow panel per cycle
// ---------------------------------------------------------------------------

interface CyclePanelProps {
  cycle: string[];
  index: number;
}

function CyclePanel({ cycle, index }: CyclePanelProps) {
  const nodes = layoutCycle(cycle);
  const edges = buildEdges(cycle);

  return (
    <div className="flex flex-col gap-3">
      <p className="text-stone text-xs font-semibold uppercase tracking-widest">
        Ciclo #{index + 1} ({cycle.length} usuario{cycle.length !== 1 ? 's' : ''})
      </p>
      <div
        className="w-full rounded-[16px] border border-hairline-light overflow-hidden"
        style={{ height: 340 }}
      >
        <ReactFlow
          nodes={nodes}
          edges={edges}
          fitView
          fitViewOptions={{ padding: 0.3 }}
          nodesDraggable={false}
          nodesConnectable={false}
          elementsSelectable={false}
          panOnDrag={false}
          zoomOnScroll={false}
          zoomOnPinch={false}
          preventScrolling={false}
          proOptions={{ hideAttribution: true }}
        />
      </div>
      {/* Accessibility fallback — hidden visually, readable by screen readers */}
      <details className="sr-only">
        <summary>Ciclo en texto</summary>
        <p>{[...cycle, cycle[0]].join(' → ')}</p>
      </details>
    </div>
  );
}

// ---------------------------------------------------------------------------
// CyclesGraph — public component
// ---------------------------------------------------------------------------

interface CyclesGraphProps {
  cycles: string[][];
}

export function CyclesGraph({ cycles }: CyclesGraphProps) {
  if (cycles.length === 0) {
    return (
      <p className="text-stone text-sm">No se detectaron ciclos</p>
    );
  }

  return (
    <div className="flex flex-col gap-8">
      {cycles.map((cycle, i) => (
        <CyclePanel key={i} cycle={cycle} index={i} />
      ))}
    </div>
  );
}
