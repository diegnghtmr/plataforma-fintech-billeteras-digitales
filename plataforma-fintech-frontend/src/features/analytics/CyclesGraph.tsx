import {
  ReactFlow,
  Background,
  BackgroundVariant,
  Handle,
  Position,
  type Node,
  type Edge,
  type NodeTypes,
  MarkerType,
} from '@xyflow/react';
import { Workflow } from 'lucide-react';

// ---------------------------------------------------------------------------
// Constants
// ---------------------------------------------------------------------------

const EDGE_COLOR = '#494fdf';
const NODE_RADIUS = 140;
const PANEL_CX = 240;
const PANEL_CY = 180;

// ---------------------------------------------------------------------------
// Initials helper: USR001 → U1, USR012 → U12, USR_A → UA
// ---------------------------------------------------------------------------

function extractInitials(userId: string): string {
  const letters = userId.replace(/[^a-zA-Z]/g, '');
  const digits = userId.replace(/[^0-9]/g, '');
  const firstLetter = letters.charAt(0).toUpperCase();
  const trailingDigits = digits.slice(-2); // up to 2 trailing digits
  return trailingDigits ? `${firstLetter}${trailingDigits}` : firstLetter;
}

// ---------------------------------------------------------------------------
// Custom node — circle avatar with position badge
// ---------------------------------------------------------------------------

interface CycleNodeData extends Record<string, unknown> {
  userId: string;
  positionInCycle: number;
}

function CycleNodeRenderer({ data }: { data: CycleNodeData }) {
  const initials = extractInitials(data.userId);

  return (
    <div className="flex flex-col items-center gap-1.5" style={{ width: 88 }}>
      {/* Invisible handles required by ReactFlow */}
      <Handle type="target" position={Position.Top} style={{ opacity: 0 }} />
      <Handle type="source" position={Position.Bottom} style={{ opacity: 0 }} />

      {/* Avatar */}
      <div
        className="relative flex items-center justify-center rounded-full bg-canvas-light border-[1.5px] border-brand font-semibold text-ink select-none"
        style={{
          width: 52,
          height: 52,
          fontSize: 15,
          boxShadow: '0 0 24px rgba(73, 79, 223, 0.35)',
        }}
      >
        {initials}

        {/* Position badge */}
        <span
          className="absolute -top-1.5 -right-1.5 flex items-center justify-center rounded-full bg-brand text-white font-semibold"
          style={{ width: 18, height: 18, fontSize: 10 }}
        >
          {data.positionInCycle}
        </span>
      </div>

      {/* UserId caption */}
      <span className="text-stone text-center leading-tight" style={{ fontSize: 11, maxWidth: 80, wordBreak: 'break-all' }}>
        {data.userId}
      </span>
    </div>
  );
}

// NodeTypes must be stable (defined outside render) to prevent ReactFlow re-mounting nodes
const cycleNodeTypes: NodeTypes = {
  customCycleNode: CycleNodeRenderer as unknown as NodeTypes[string],
};

// ---------------------------------------------------------------------------
// Layout helpers
// ---------------------------------------------------------------------------

function layoutCycle(cycle: string[]): Node[] {
  const step = (2 * Math.PI) / cycle.length;
  return cycle.map((userId, i) => ({
    id: userId,
    type: 'customCycleNode',
    position: {
      x: PANEL_CX + NODE_RADIUS * Math.cos(i * step - Math.PI / 2) - 44,
      y: PANEL_CY + NODE_RADIUS * Math.sin(i * step - Math.PI / 2) - 44,
    },
    data: {
      userId,
      positionInCycle: i + 1,
    } satisfies CycleNodeData,
  }));
}

function buildEdges(cycle: string[]): Edge[] {
  return cycle.map((userId, i) => {
    const nextId = cycle[(i + 1) % cycle.length]!;
    return {
      id: `${userId}->${nextId}`,
      source: userId,
      target: nextId,
      type: 'smoothstep',
      animated: true,
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
// CyclePanel — one ReactFlow card per cycle
// ---------------------------------------------------------------------------

interface CyclePanelProps {
  cycle: string[];
  index: number;
}

function CyclePanel({ cycle, index }: CyclePanelProps) {
  const nodes = layoutCycle(cycle);
  const edges = buildEdges(cycle);
  const userCount = cycle.length;

  return (
    <div
      className="bg-canvas-light rounded-[20px] border border-hairline-light overflow-hidden flex flex-col"
      style={{ minHeight: 360 }}
    >
      {/* Card header */}
      <div className="flex items-center gap-3 px-6 pt-5 pb-4 border-b border-hairline-light">
        <span
          className="inline-flex items-center px-3 py-1 rounded-full text-white font-semibold"
          style={{ fontSize: 12, background: EDGE_COLOR }}
        >
          Ciclo #{index + 1}
        </span>
        <span className="text-stone" style={{ fontSize: 13 }}>
          {userCount} usuario{userCount !== 1 ? 's' : ''}
        </span>
      </div>

      {/* Graph area */}
      <div className="flex-1" style={{ minHeight: 300 }}>
        <ReactFlow
          nodes={nodes}
          edges={edges}
          nodeTypes={cycleNodeTypes}
          fitView
          fitViewOptions={{ padding: 0.2 }}
          nodesDraggable={false}
          nodesConnectable={false}
          elementsSelectable={false}
          panOnDrag={false}
          zoomOnScroll={false}
          zoomOnPinch={false}
          preventScrolling={false}
          proOptions={{ hideAttribution: true }}
        >
          <Background
            variant={BackgroundVariant.Dots}
            gap={16}
            size={1}
            color="#e2e2e7"
          />
        </ReactFlow>
      </div>

      {/* Accessibility fallback */}
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
      <div className="flex flex-col items-center justify-center gap-4 py-16 text-center">
        <Workflow className="text-stone" size={40} strokeWidth={1.5} />
        <div className="flex flex-col gap-1">
          <p className="text-ink font-semibold">Sin ciclos detectados</p>
          <p className="text-stone" style={{ fontSize: 13 }}>
            El grafo de transferencias no presenta ciclos en este momento.
          </p>
        </div>
      </div>
    );
  }

  return (
    <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
      {cycles.map((cycle, i) => (
        <CyclePanel key={i} cycle={cycle} index={i} />
      ))}
    </div>
  );
}
