import dagre from 'dagre';
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
import { Network } from 'lucide-react';

// ---------------------------------------------------------------------------
// Constants
// ---------------------------------------------------------------------------

const COBALT = '#494fdf';
const WARNING = '#ec7e00';
const DANGER = '#e23b4a';

const NODE_W = 88;
const NODE_H = 72;

// ---------------------------------------------------------------------------
// Initials helper (shared pattern with CyclesGraph)
// ---------------------------------------------------------------------------

function extractInitials(userId: string): string {
  const letters = userId.replace(/[^a-zA-Z]/g, '');
  const digits = userId.replace(/[^0-9]/g, '');
  const firstLetter = letters.charAt(0).toUpperCase();
  const trailingDigits = digits.slice(-2);
  return trailingDigits ? `${firstLetter}${trailingDigits}` : firstLetter;
}

// ---------------------------------------------------------------------------
// Edge color — top 25% = warning, top 1 route = danger
// ---------------------------------------------------------------------------

function resolveEdgeColor(
  transferCount: number,
  p75: number,
  maxCount: number,
): string {
  if (transferCount === maxCount) return DANGER;
  if (transferCount >= p75) return WARNING;
  return COBALT;
}

// ---------------------------------------------------------------------------
// Dagre auto-layout
// ---------------------------------------------------------------------------

interface RouteItem {
  sourceUserId: string;
  targetUserId: string;
  transferCount: number;
  totalAmount: number;
}

function buildGraph(routes: RouteItem[]): { nodes: Node[]; edges: Edge[] } {
  if (routes.length === 0) return { nodes: [], edges: [] };

  // Collect unique user IDs
  const userSet = new Set<string>();
  for (const r of routes) {
    userSet.add(r.sourceUserId);
    userSet.add(r.targetUserId);
  }

  // Determine edge color thresholds
  const counts = routes.map((r) => r.transferCount);
  const sorted = [...counts].sort((a, b) => a - b);
  const p75Index = Math.floor(sorted.length * 0.75);
  const p75 = sorted[p75Index] ?? 0;
  const maxCount = Math.max(...counts);

  // Build dagre graph
  const g = new dagre.graphlib.Graph();
  g.setGraph({ rankdir: 'LR', nodesep: 60, ranksep: 100, marginx: 40, marginy: 40 });
  g.setDefaultEdgeLabel(() => ({}));

  for (const userId of userSet) {
    g.setNode(userId, { width: NODE_W, height: NODE_H });
  }

  for (const r of routes) {
    g.setEdge(r.sourceUserId, r.targetUserId);
  }

  dagre.layout(g);

  const nodes: Node[] = [];
  for (const userId of userSet) {
    const pos = g.node(userId);
    nodes.push({
      id: userId,
      type: 'networkNode',
      position: { x: pos.x - NODE_W / 2, y: pos.y - NODE_H / 2 },
      data: { userId } satisfies NetworkNodeData,
    });
  }

  const edges: Edge[] = routes.map((r) => {
    const count = r.transferCount;
    const strokeWidth = 1 + Math.log(count + 1) * 1.5;
    const color = resolveEdgeColor(count, p75, maxCount);

    return {
      id: `${r.sourceUserId}->${r.targetUserId}`,
      source: r.sourceUserId,
      target: r.targetUserId,
      type: 'smoothstep',
      animated: count === maxCount,
      label: `${count}`,
      labelStyle: { fontSize: 11, fill: color, fontWeight: 600 },
      labelBgStyle: { fill: '#ffffff', fillOpacity: 0.85 },
      style: { stroke: color, strokeWidth },
      markerEnd: {
        type: MarkerType.ArrowClosed,
        color,
        width: 14,
        height: 14,
      },
    };
  });

  return { nodes, edges };
}

// ---------------------------------------------------------------------------
// Custom network node
// ---------------------------------------------------------------------------

interface NetworkNodeData extends Record<string, unknown> {
  userId: string;
}

function NetworkNodeRenderer({ data }: { data: NetworkNodeData }) {
  const initials = extractInitials(data.userId);

  return (
    <div className="flex flex-col items-center gap-1.5" style={{ width: NODE_W }}>
      <Handle type="target" position={Position.Left} style={{ opacity: 0 }} />
      <Handle type="source" position={Position.Right} style={{ opacity: 0 }} />

      <div
        className="relative flex items-center justify-center rounded-full bg-canvas-light border-[1.5px] border-brand font-semibold text-ink select-none"
        style={{
          width: 48,
          height: 48,
          fontSize: 14,
          boxShadow: '0 0 20px rgba(73, 79, 223, 0.28)',
        }}
      >
        {initials}
      </div>

      <span
        className="text-stone text-center leading-tight"
        style={{ fontSize: 11, maxWidth: NODE_W, wordBreak: 'break-all' }}
      >
        {data.userId}
      </span>
    </div>
  );
}

// Stable outside render
const networkNodeTypes: NodeTypes = {
  networkNode: NetworkNodeRenderer as unknown as NodeTypes[string],
};

// ---------------------------------------------------------------------------
// Legend
// ---------------------------------------------------------------------------

function EdgeLegend() {
  return (
    <div className="flex flex-wrap items-center gap-4 px-6 pb-5" style={{ fontSize: 12 }}>
      <LegendItem color={COBALT} label="Ruta normal" />
      <LegendItem color={WARNING} label="Top 25%" />
      <LegendItem color={DANGER} label="Ruta más frecuente" animated />
    </div>
  );
}

function LegendItem({
  color,
  label,
  animated = false,
}: {
  color: string;
  label: string;
  animated?: boolean;
}) {
  return (
    <div className="flex items-center gap-1.5">
      <span
        className="inline-block rounded-full"
        style={{ width: 24, height: 3, background: color, opacity: animated ? 0.9 : 1 }}
      />
      <span className="text-stone">{label}</span>
    </div>
  );
}

// ---------------------------------------------------------------------------
// TransferNetworkGraph — public component
// ---------------------------------------------------------------------------

interface TransferNetworkGraphProps {
  routes: RouteItem[] | undefined;
}

export function TransferNetworkGraph({ routes }: TransferNetworkGraphProps) {
  if (!routes || routes.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center gap-4 py-16 text-center">
        <Network className="text-stone" size={40} strokeWidth={1.5} />
        <div className="flex flex-col gap-1">
          <p className="text-ink font-semibold">Sin rutas frecuentes</p>
          <p className="text-stone" style={{ fontSize: 13 }}>
            No hay rutas de transferencia que superen el umbral mínimo.
          </p>
        </div>
      </div>
    );
  }

  const { nodes, edges } = buildGraph(routes);

  return (
    <div
      className="bg-canvas-light rounded-[20px] border border-hairline-light overflow-hidden flex flex-col"
      style={{ minHeight: 440 }}
    >
      {/* Header */}
      <div className="flex items-center gap-3 px-6 pt-5 pb-4 border-b border-hairline-light">
        <Network size={18} style={{ color: COBALT }} strokeWidth={2} />
        <span className="text-ink font-semibold" style={{ fontSize: 14 }}>
          Red de Transferencias
        </span>
        <span className="text-stone" style={{ fontSize: 13 }}>
          {nodes.length} usuarios · {edges.length} rutas
        </span>
      </div>

      {/* Graph area */}
      <div className="flex-1" style={{ minHeight: 360 }}>
        <ReactFlow
          nodes={nodes}
          edges={edges}
          nodeTypes={networkNodeTypes}
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

      {/* Legend */}
      <EdgeLegend />

      {/* Accessibility fallback */}
      <details className="sr-only">
        <summary>Red de transferencias en texto</summary>
        <ul>
          {routes.map((r) => (
            <li key={`${r.sourceUserId}-${r.targetUserId}`}>
              {r.sourceUserId} → {r.targetUserId}: {r.transferCount} transferencias
            </li>
          ))}
        </ul>
      </details>
    </div>
  );
}
