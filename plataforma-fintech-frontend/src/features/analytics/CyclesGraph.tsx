import {
  ReactFlow,
  Background,
  BackgroundVariant,
  Controls,
  Handle,
  Position,
  type Node,
  type Edge,
  type NodeTypes,
  MarkerType,
} from '@xyflow/react';
import { Workflow, Loader2 } from 'lucide-react';

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
    <div className="flex flex-col items-center gap-2" style={{ width: 96 }}>
      {/* Invisible handles required by ReactFlow */}
      <Handle type="target" position={Position.Top} style={{ opacity: 0 }} />
      <Handle type="source" position={Position.Bottom} style={{ opacity: 0 }} />

      {/* Avatar */}
      <div
        className="relative flex items-center justify-center rounded-full font-semibold text-ink select-none"
        style={{
          width: 60,
          height: 60,
          fontSize: 16,
          background: 'linear-gradient(160deg, #ffffff 0%, #f4f4f4 100%)',
          border: '2px solid #494fdf',
          boxShadow:
            'inset 0 0 0 2px rgba(73,79,223,0.08), 0 0 0 6px rgba(73,79,223,0.08), 0 8px 24px -6px rgba(73,79,223,0.4)',
          transition: 'all 200ms cubic-bezier(0.22, 1, 0.36, 1)',
        }}
        onMouseEnter={(e) => {
          (e.currentTarget as HTMLDivElement).style.transform = 'scale(1.06)';
          (e.currentTarget as HTMLDivElement).style.boxShadow =
            'inset 0 0 0 2px rgba(73,79,223,0.12), 0 0 0 8px rgba(73,79,223,0.14), 0 12px 32px -6px rgba(73,79,223,0.55)';
        }}
        onMouseLeave={(e) => {
          (e.currentTarget as HTMLDivElement).style.transform = 'scale(1)';
          (e.currentTarget as HTMLDivElement).style.boxShadow =
            'inset 0 0 0 2px rgba(73,79,223,0.08), 0 0 0 6px rgba(73,79,223,0.08), 0 8px 24px -6px rgba(73,79,223,0.4)';
        }}
      >
        {initials}

        {/* Position badge */}
        <span
          className="absolute -top-2 -right-2 flex items-center justify-center rounded-full text-white font-bold"
          style={{
            width: 22,
            height: 22,
            fontSize: 10,
            background: 'linear-gradient(135deg, #4f55f1 0%, #494fdf 60%, #3a40c4 100%)',
            border: '1.5px solid #ffffff',
            boxShadow: '0 2px 6px rgba(73,79,223,0.4)',
          }}
        >
          {data.positionInCycle}
        </span>
      </div>

      {/* UserId caption */}
      <span
        className="text-stone text-center leading-tight"
        style={{
          fontFamily: 'var(--font-body)',
          fontSize: 11,
          fontWeight: 400,
          letterSpacing: '0.04em',
          maxWidth: 88,
          wordBreak: 'break-all',
        }}
      >
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
      x: PANEL_CX + NODE_RADIUS * Math.cos(i * step - Math.PI / 2) - 48,
      y: PANEL_CY + NODE_RADIUS * Math.sin(i * step - Math.PI / 2) - 48,
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
      style: {
        stroke: EDGE_COLOR,
        strokeWidth: 2.5,
        strokeDasharray: '6 4',
      },
      markerEnd: {
        type: MarkerType.ArrowClosed,
        color: EDGE_COLOR,
        width: 20,
        height: 20,
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
      className="rounded-[20px] border border-hairline-light overflow-hidden flex flex-col"
      style={{
        minHeight: 360,
        background: 'linear-gradient(135deg, #ffffff 0%, #f4f4f4 100%)',
        transition: 'box-shadow 200ms ease',
      }}
      onMouseEnter={(e) => {
        (e.currentTarget as HTMLDivElement).style.boxShadow =
          '0 8px 24px -12px rgba(73,79,223,0.18)';
      }}
      onMouseLeave={(e) => {
        (e.currentTarget as HTMLDivElement).style.boxShadow = 'none';
      }}
    >
      {/* Card header */}
      <div className="flex items-center gap-3 px-6 pt-5 pb-4 border-b border-hairline-light">
        <span
          className="inline-flex items-center px-3 py-1 rounded-full text-white font-semibold"
          style={{
            fontSize: 12,
            background: 'linear-gradient(135deg, #4f55f1 0%, #494fdf 60%, #3a40c4 100%)',
          }}
        >
          Ciclo #{index + 1}
        </span>

        {/* Flowing loader icon */}
        <Loader2
          size={14}
          style={{
            color: EDGE_COLOR,
            animation: 'spin 8s linear infinite',
          }}
        />

        {/* User count chip */}
        <span
          className="inline-flex items-center px-2 py-0.5 rounded-full text-ink font-medium"
          style={{
            fontSize: 12,
            background: '#f4f4f4',
            border: '1px solid #e2e2e7',
          }}
        >
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
          nodesDraggable={true}
          nodesConnectable={false}
          elementsSelectable={true}
          panOnDrag={true}
          panOnScroll={false}
          zoomOnScroll={true}
          zoomOnPinch={true}
          zoomOnDoubleClick={true}
          minZoom={0.4}
          maxZoom={2}
          proOptions={{ hideAttribution: true }}
        >
          <Background
            variant={BackgroundVariant.Dots}
            gap={20}
            size={1.2}
            color="rgba(73,79,223,0.06)"
          />
          <Controls
            position="bottom-right"
            showInteractive={false}
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
        <div
          className="flex items-center justify-center rounded-full"
          style={{
            width: 80,
            height: 80,
            background: '#f4f4f4',
            border: '1px solid #e2e2e7',
          }}
        >
          <Workflow
            style={{ color: '#8d969e' }}
            size={36}
            strokeWidth={1.5}
          />
        </div>
        <div className="flex flex-col gap-1.5">
          <p className="text-body-md-bold text-ink">Sin ciclos detectados</p>
          <p className="text-body-sm" style={{ color: '#8d969e', maxWidth: 300 }}>
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
