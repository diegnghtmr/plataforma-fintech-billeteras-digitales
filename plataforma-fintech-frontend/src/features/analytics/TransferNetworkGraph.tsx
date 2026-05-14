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
import { GRAPH_TOKENS } from './cyclesGraphUtils';

// ---------------------------------------------------------------------------
// TransferNetworkGraph — distill.pub-inspired monochrome network
//
// Resting nodes are pale grey discs with a thin dark stroke. The user id
// label sits below as plain text on the canvas — no pill, no colour-coded
// identity per user. The only accent is brand cobalt on the single most
// frequent route. Top 25% routes step up to a darker greyscale; everything
// else stays at a quiet 1px charcoal line.
// ---------------------------------------------------------------------------

const COBALT_TOP = GRAPH_TOKENS.edge.strokeTop; // top route — the one accent
const TOP25_STROKE = GRAPH_TOKENS.edge.strokeTop25; // darker greyscale
const DEFAULT_STROKE = GRAPH_TOKENS.edge.strokeDefault; // 1px charcoal

// Container hugs the 38px disc so xyflow Handles attach at the disc edge —
// otherwise edges enter/leave with long invisible stubs that read as "the
// line passes through the node". Label overflows visually without growing
// the layout bbox.
const NODE_W = 40;
const NODE_H = 40;

// ---------------------------------------------------------------------------
// Initials helper
// ---------------------------------------------------------------------------

function extractInitials(userId: string): string {
  const letters = userId.replace(/[^a-zA-Z]/g, '');
  const digits = userId.replace(/[^0-9]/g, '');
  const firstLetter = letters.charAt(0).toUpperCase();
  const trailingDigits = digits.slice(-2);
  return trailingDigits ? `${firstLetter}${trailingDigits}` : firstLetter;
}

// ---------------------------------------------------------------------------
// Edge style resolver — monochrome scale + ONE cobalt accent
// ---------------------------------------------------------------------------

interface EdgeStyleSpec {
  stroke: string;
  width: number;
  isTop: boolean;
  isHighVolume: boolean;
}

function resolveEdgeStyle(
  transferCount: number,
  p75: number,
  maxCount: number,
): EdgeStyleSpec {
  if (transferCount === maxCount) {
    return { stroke: COBALT_TOP, width: GRAPH_TOKENS.edge.widthTop, isTop: true, isHighVolume: false };
  }
  if (transferCount >= p75) {
    return { stroke: TOP25_STROKE, width: GRAPH_TOKENS.edge.widthTop25, isTop: false, isHighVolume: true };
  }
  return { stroke: DEFAULT_STROKE, width: GRAPH_TOKENS.edge.widthDefault, isTop: false, isHighVolume: false };
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

  // Determine edge style thresholds
  const counts = routes.map((r) => r.transferCount);
  const sorted = [...counts].sort((a, b) => a - b);
  const p75Index = Math.floor(sorted.length * 0.75);
  const p75 = sorted[p75Index] ?? 0;
  const maxCount = Math.max(...counts);

  // Build dagre graph
  const g = new dagre.graphlib.Graph();
  g.setGraph({ rankdir: 'LR', nodesep: 90, ranksep: 160, marginx: 48, marginy: 48 });
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
    const spec = resolveEdgeStyle(r.transferCount, p75, maxCount);
    // Pick handles so a back-edge (target to the LEFT of source in the LR
    // layout) exits from the source's LEFT handle, not its RIGHT — otherwise
    // xyflow renders an unwanted stub before turning back.
    const srcPos = g.node(r.sourceUserId);
    const tgtPos = g.node(r.targetUserId);
    const isBackEdge = tgtPos.x < srcPos.x;
    const sourceHandle = isBackEdge ? 'src-l' : 'src-r';
    const targetHandle = isBackEdge ? 'tgt-r' : 'tgt-l';
    return {
      id: `${r.sourceUserId}->${r.targetUserId}`,
      source: r.sourceUserId,
      target: r.targetUserId,
      sourceHandle,
      targetHandle,
      type: 'straight',
      // No animated shimmer — the cobalt + thicker stroke is already the
      // single accent; pulsing dashes would break the academic restraint.
      animated: false,
      label: `${r.transferCount}`,
      labelStyle: {
        fontSize: 10,
        fill: spec.isTop ? COBALT_TOP : '#3a3d40',
        fontWeight: 600,
        fontFamily: 'Inter, system-ui, sans-serif',
      },
      labelBgStyle: { fill: '#ffffff', fillOpacity: 0.92, rx: 3 },
      style: {
        stroke: spec.stroke,
        strokeWidth: spec.width,
        strokeOpacity: spec.isTop || spec.isHighVolume ? 1 : 0.85,
      },
      markerEnd: {
        type: MarkerType.ArrowClosed,
        color: spec.stroke,
        width: 11,
        height: 11,
      },
    };
  });

  return { nodes, edges };
}

// ---------------------------------------------------------------------------
// Custom network node — pale grey disc, plain text id below
// ---------------------------------------------------------------------------

interface NetworkNodeData extends Record<string, unknown> {
  userId: string;
}

function NetworkNodeRenderer({ data }: { data: NetworkNodeData }) {
  const initials = extractInitials(data.userId);

  return (
    <div className="relative flex items-center justify-center" style={{ width: NODE_W, height: NODE_H }}>
      {/* Four named handles — buildGraph picks which side to attach to per
          edge based on the dagre-computed geometry, so back-edges (target to
          the left of source) exit from the LEFT instead of stubbing right. */}
      <Handle id="tgt-l" type="target" position={Position.Left} style={{ opacity: 0 }} />
      <Handle id="src-r" type="source" position={Position.Right} style={{ opacity: 0 }} />
      <Handle id="tgt-r" type="target" position={Position.Right} style={{ opacity: 0 }} />
      <Handle id="src-l" type="source" position={Position.Left} style={{ opacity: 0 }} />

      {/* Disc — pale grey fill, 1px dark stroke, charcoal initials inside */}
      <div
        className="flex items-center justify-center rounded-full font-semibold select-none"
        style={{
          width: 38,
          height: 38,
          fontSize: 11,
          background: GRAPH_TOKENS.node.fillDefault,
          color: '#3a3d40',
          border: `1px solid ${GRAPH_TOKENS.node.stroke}`,
          fontFamily: 'Inter, system-ui, sans-serif',
          letterSpacing: '0.02em',
          transition: 'transform 180ms ease, background 180ms ease, border-color 180ms ease',
        }}
        onMouseEnter={(e) => {
          const el = e.currentTarget as HTMLDivElement;
          el.style.transform = 'scale(1.04)';
          el.style.borderColor = '#2c2c2c';
        }}
        onMouseLeave={(e) => {
          const el = e.currentTarget as HTMLDivElement;
          el.style.transform = 'scale(1)';
          el.style.borderColor = GRAPH_TOKENS.node.stroke;
        }}
      >
        {initials}
      </div>

      {/* Plain text id — overflows visually outside the layout bbox so the
          handles stay tight to the disc. Absolutely positioned to keep the
          flex container width = disc width = NODE_W. */}
      <span
        className="select-none absolute top-full left-1/2 -translate-x-1/2 mt-1"
        style={{
          fontFamily: 'Inter, system-ui, sans-serif',
          fontSize: 10,
          fontWeight: 500,
          color: '#3a3d40',
          letterSpacing: '0.02em',
          whiteSpace: 'nowrap',
          textAlign: 'center',
          pointerEvents: 'none',
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
    <div className="flex flex-wrap items-center gap-2 px-6 pb-4 pt-1">
      <LegendChip color={COBALT_TOP} label="Ruta más frecuente" />
      <LegendChip color={TOP25_STROKE} label="Top 25%" />
      <LegendChip color={DEFAULT_STROKE} label="Ruta normal" faded />
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
      className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full"
      style={{
        background: '#f4f4f4',
        border: '1px solid #e2e2e7',
        fontSize: 11,
        color: '#3a3d40',
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
    <figure
      className="rounded-[16px] border border-hairline-light overflow-hidden flex flex-col m-0"
      style={{
        minHeight: 440,
        background: '#ffffff',
        transition: 'box-shadow 200ms ease',
      }}
      onMouseEnter={(e) => {
        (e.currentTarget as HTMLDivElement).style.boxShadow =
          '0 4px 12px -8px rgba(0,0,0,0.06)';
      }}
      onMouseLeave={(e) => {
        (e.currentTarget as HTMLDivElement).style.boxShadow = 'none';
      }}
    >
      {/* Header — calmer cobalt pill, neutral chip */}
      <div className="flex items-center gap-3 px-6 pt-4 pb-3 border-b border-hairline-light">
        <span
          className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full font-semibold"
          style={{
            fontSize: 11,
            background: '#494fdf',
            color: '#ffffff',
            letterSpacing: '0.02em',
          }}
        >
          <Network size={11} strokeWidth={2.5} />
          Red de transferencias
        </span>

        <span
          className="inline-flex items-center px-2 py-0.5 rounded-full font-medium"
          style={{
            fontSize: 11,
            background: '#f4f4f4',
            border: '1px solid #e2e2e7',
            color: '#3a3d40',
          }}
        >
          {nodes.length} usuarios · {edges.length} rutas
        </span>
      </div>

      {/* Graph area — explicit height required by ReactFlow */}
      <div style={{ height: 400, width: '100%', background: '#ffffff' }}>
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
            gap={24}
            size={1}
            color="rgba(0,0,0,0.04)"
          />
          <Controls
            position="bottom-right"
            showInteractive={false}
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
    </figure>
  );
}
