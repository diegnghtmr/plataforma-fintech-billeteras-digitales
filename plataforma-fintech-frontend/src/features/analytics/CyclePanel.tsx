import { useRef, useEffect, useState, useMemo } from 'react';
import ForceGraph2D from 'react-force-graph-2d';
import type { ForceGraphMethods, NodeObject, LinkObject } from 'react-force-graph-2d';
import {
  buildGraphData,
  resolveNodeOpacity,
  resolveLinkOpacity,
  resolveNodeState,
  GRAPH_TOKENS,
  type CycleGraphNode,
  type CycleGraphLink,
} from './cyclesGraphUtils';
import { drawNode, drawLink } from './cyclesGraphCanvas';
import { useCyclesEgoCentric } from './useCyclesEgoCentric';

// ---------------------------------------------------------------------------
// CyclePanelProps
// ---------------------------------------------------------------------------

interface CyclePanelProps {
  cycle: string[];
  cycleIndex: number;
}

// ---------------------------------------------------------------------------
// CyclePanel — distill.pub-inspired panel
//
// • Flat white card with hairline border (no gradient, no drop shadow).
// • Calm header pill — cobalt is a stamp, not a band.
// • Force-directed canvas with explicit zoom cap so 3-node cycles don't get
//   visually inflated. After cooldown we centre and clamp zoom to 1.0; the
//   user can still zoom manually within sensible bounds.
// • Italic serif caption below the canvas — pure distill convention.
// ---------------------------------------------------------------------------

export function CyclePanel({ cycle, cycleIndex }: CyclePanelProps) {
  const fgRef = useRef<ForceGraphMethods | undefined>(undefined);
  const containerRef = useRef<HTMLDivElement | null>(null);
  const [hoveredNodeId, setHoveredNodeId] = useState<string | null>(null);
  const [hovered, setHovered] = useState(false);
  const [canvasSize, setCanvasSize] = useState({ width: 480, height: 420 });

  useEffect(() => {
    if (!containerRef.current) return;
    const ro = new ResizeObserver((entries) => {
      const entry = entries[0];
      if (!entry) return;
      const { width } = entry.contentRect;
      setCanvasSize({ width: Math.max(320, Math.floor(width)), height: 420 });
    });
    ro.observe(containerRef.current);
    return () => ro.disconnect();
  }, []);

  const graphData = useMemo(() => buildGraphData([cycle]), [cycle]);

  const { selectedNodeId, selectNode, neighborIds } = useCyclesEgoCentric(
    graphData.links as CycleGraphLink[],
  );

  const userCount = cycle.length;

  // d3-force configuration after mount
  useEffect(() => {
    if (!fgRef.current) return;
    const fg = fgRef.current;

    // Configure forces via the library's d3Force API.
    // Cast via unknown because ForceFn is a narrow type that lacks the d3 method signatures.
    const chargeForce = fg.d3Force('charge') as unknown as { strength: (v: number) => void } | undefined;
    chargeForce?.strength(-260);

    const linkForce = fg.d3Force('link') as unknown as
      | { distance: (v: number) => { strength: (v: number) => void } }
      | undefined;
    linkForce?.distance(80).strength(0.6);
  }, []);

  // Clamp initial zoom — react-force-graph's auto-fit produces a 2x scale on
  // tiny graphs (e.g. 3 nodes), which crushes the figure visually. Wait for
  // the layout to settle, then centre at (0,0) and lock to a sober 1.0 zoom.
  // The user can still zoom in/out manually.
  useEffect(() => {
    const fg = fgRef.current;
    if (!fg) return;
    const settle = window.setTimeout(() => {
      fg.centerAt(0, 0, 0);
      fg.zoom(1, 0);
    }, 1200);
    return () => window.clearTimeout(settle);
  }, [cycle]);

  // Reduced motion: pause animation if media query matches
  useEffect(() => {
    const mq = window.matchMedia('(prefers-reduced-motion: reduce)');
    if (mq.matches) {
      const timer = setTimeout(() => {
        fgRef.current?.pauseAnimation();
      }, 300);
      return () => clearTimeout(timer);
    }
    const handler = () => {
      if (mq.matches) fgRef.current?.pauseAnimation();
    };
    mq.addEventListener('change', handler);
    return () => mq.removeEventListener('change', handler);
  }, []);

  return (
    <figure
      className="rounded-[16px] overflow-hidden flex flex-col m-0 w-full"
      style={{
        minHeight: 500,
        background: GRAPH_TOKENS.canvas.background,
        border: '1px solid #e2e2e7',
        transition: 'box-shadow 200ms ease',
        boxShadow: hovered ? '0 4px 12px -8px rgba(0,0,0,0.06)' : 'none',
      }}
      onMouseEnter={() => setHovered(true)}
      onMouseLeave={() => setHovered(false)}
    >
      {/* Card header — calmer: pill + chip without saturated gradients */}
      <div
        className="flex items-center gap-3 px-6 pt-4 pb-3"
        style={{ borderBottom: '1px solid #e2e2e7' }}
      >
        <span
          className="inline-flex items-center px-2.5 py-0.5 rounded-full font-semibold"
          style={{
            fontSize: 11,
            background: '#494fdf',
            color: '#ffffff',
            letterSpacing: '0.02em',
          }}
        >
          Ciclo #{cycleIndex + 1}
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
          {userCount} usuario{userCount !== 1 ? 's' : ''}
        </span>
      </div>

      {/* Canvas graph area */}
      <div
        ref={containerRef}
        role="img"
        aria-label={`Grafo del ciclo #${cycleIndex + 1}`}
        className="flex-1 flex items-center justify-center"
        style={{ minHeight: 420, background: GRAPH_TOKENS.canvas.background }}
      >
        <ForceGraph2D
          ref={fgRef}
          graphData={
            graphData as {
              nodes: NodeObject[];
              links: LinkObject[];
            }
          }
          width={canvasSize.width}
          height={canvasSize.height}
          backgroundColor="transparent"
          cooldownTime={2000}
          cooldownTicks={120}
          linkCurvature={0}
          minZoom={0.4}
          maxZoom={3}
          nodeRelSize={4}
          enableZoomInteraction={true}
          nodeCanvasObject={(node, ctx) => {
            const typedNode = node as CycleGraphNode;
            const nodeId = String(node.id);
            const state = resolveNodeState(nodeId, selectedNodeId, neighborIds);
            // Hovered (but not selected) gets a transient highlight — same
            // pale-cobalt tint we use for neighbours. Cheap signal of focus
            // without breaking the monochrome canvas.
            const effectiveState =
              state === 'default' && nodeId === hoveredNodeId ? 'neighbor' : state;
            drawNode(ctx, typedNode, {
              state: effectiveState,
              opacity: resolveNodeOpacity(nodeId, selectedNodeId, neighborIds),
            });
          }}
          nodeCanvasObjectMode={() => 'replace'}
          linkCanvasObject={(link, ctx) => {
            const typedLink = link as CycleGraphLink;
            drawLink(
              ctx,
              link.source as { x: number; y: number } | string | NodeObject,
              link.target as { x: number; y: number } | string | NodeObject,
              {
                opacity: resolveLinkOpacity(
                  typedLink.source,
                  typedLink.target,
                  selectedNodeId,
                  neighborIds,
                ),
                width: GRAPH_TOKENS.edge.widthDefault,
                color: GRAPH_TOKENS.edge.strokeDefault,
              },
            );
          }}
          linkCanvasObjectMode={() => 'replace'}
          onNodeClick={(node) => {
            const nodeId = String(node.id);
            selectNode(nodeId === selectedNodeId ? null : nodeId);
          }}
          onNodeHover={(node) => {
            setHoveredNodeId(node ? String(node.id) : null);
          }}
          onBackgroundClick={() => selectNode(null)}
        />
      </div>

      {/* Screen-reader text fallback */}
      <details className="sr-only">
        <summary>Ciclo en texto</summary>
        <p>{[...cycle, cycle[0]].join(' → ')}</p>
      </details>
    </figure>
  );
}
