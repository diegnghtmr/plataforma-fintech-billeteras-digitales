import dagre from 'dagre';
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
import { Network } from 'lucide-react';

// ---------------------------------------------------------------------------
// Constants
// ---------------------------------------------------------------------------

const COBALT = '#494fdf';
const WARNING = '#ec7e00';
const DANGER = '#e23b4a';

const NODE_W = 96;
const NODE_H = 80;

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
    const strokeWidth = 1.5 + Math.log(count + 1) * 1.2;
    const color = resolveEdgeColor(count, p75, maxCount);
    const isTop = count === maxCount;
    const isHighVolume = !isTop && count >= p75;

    return {
      id: `${r.sourceUserId}->${r.targetUserId}`,
      source: r.sourceUserId,
      target: r.targetUserId,
      type: 'smoothstep',
      animated: isTop,
      label: `${count}`,
      labelStyle: { fontSize: 10, fill: color, fontWeight: 700, fontFamily: 'var(--font-body)' },
      labelBgStyle: { fill: '#ffffff', fillOpacity: 0.9, rx: 4 },
      style: {
        stroke: color,
        strokeWidth,
        strokeDasharray: isTop ? '6 4' : undefined,
        strokeOpacity: isTop || isHighVolume ? 1 : 0.6,
      },
      markerEnd: {
        type: MarkerType.ArrowClosed,
        color,
        width: 18,
        height: 18,
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
    <div className="flex flex-col items-center gap-2" style={{ width: NODE_W }}>
      <Handle type="target" position={Position.Left} style={{ opacity: 0 }} />
      <Handle type="source" position={Position.Right} style={{ opacity: 0 }} />

      <div
        className="relative flex items-center justify-center rounded-full font-semibold text-ink select-none"
        style={{
          width: 56,
          height: 56,
          fontSize: 15,
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
      </div>

      <span
        className="text-stone text-center leading-tight"
        style={{
          fontFamily: 'var(--font-body)',
          fontSize: 11,
          fontWeight: 400,
          letterSpacing: '0.04em',
          maxWidth: NODE_W,
          wordBreak: 'break-all',
        }}
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
// Legend — horizontal pill row
// ---------------------------------------------------------------------------

function EdgeLegend() {
  return (
    <div className="flex flex-wrap items-center gap-2 px-6 pb-5 pt-1">
      <LegendChip color={DANGER} label="Ruta más frecuente" />
      <LegendChip color={WARNING} label="Top 25%" />
      <LegendChip color={COBALT} label="Ruta normal" faded />
    </div>
  );
}

function LegendChip({
  color,
  label,
  faded = false,
}: {
  color: string;
  label: string;
  faded?: boolean;
}) {
  return (
    <div
      className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full"
      style={{
        background: '#f4f4f4',
        border: '1px solid #e2e2e7',
        fontSize: 12,
        color: '#505a63',
        opacity: faded ? 0.85 : 1,
      }}
    >
      <span
        className="inline-block rounded-full flex-shrink-0"
        style={{ width: 8, height: 8, background: color }}
      />
      <span>{label}</span>
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
        <div
          className="flex items-center justify-center rounded-full"
          style={{
            width: 80,
            height: 80,
            background: '#f4f4f4',
            border: '1px solid #e2e2e7',
          }}
        >
          <Network style={{ color: '#8d969e' }} size={36} strokeWidth={1.5} />
        </div>
        <div className="flex flex-col gap-1.5">
          <p className="text-body-md-bold text-ink">Sin rutas frecuentes</p>
          <p className="text-body-sm" style={{ color: '#8d969e', maxWidth: 300 }}>
            No hay rutas de transferencia que superen el umbral mínimo.
          </p>
        </div>
      </div>
    );
  }

  const { nodes, edges } = buildGraph(routes);

  return (
    <div
      className="rounded-[20px] border border-hairline-light overflow-hidden flex flex-col"
      style={{
        minHeight: 440,
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
      {/* Header */}
      <div className="flex items-center gap-3 px-6 pt-5 pb-4 border-b border-hairline-light">
        <span
          className="inline-flex items-center gap-2 px-3 py-1 rounded-full text-white font-semibold"
          style={{
            fontSize: 12,
            background: 'linear-gradient(135deg, #4f55f1 0%, #494fdf 60%, #3a40c4 100%)',
          }}
        >
          <Network size={12} strokeWidth={2.5} />
          Red de transferencias
        </span>

        <span
          className="inline-flex items-center px-2 py-0.5 rounded-full text-ink font-medium"
          style={{
            fontSize: 12,
            background: '#f4f4f4',
            border: '1px solid #e2e2e7',
          }}
        >
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
          nodesDraggable={true}
          nodesConnectable={false}
          elementsSelectable={true}
          panOnDrag={true}
          panOnScroll={false}
          zoomOnScroll={true}
          zoomOnPinch={true}
          zoomOnDoubleClick={true}
          minZoom={0.3}
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

      {/* Legend — horizontal pill row outside graph area */}
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
