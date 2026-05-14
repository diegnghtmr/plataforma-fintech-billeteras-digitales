import { useRef, useEffect, useState, useMemo } from 'react';
import ForceGraph2D from 'react-force-graph-2d';
import type { ForceGraphMethods, NodeObject, LinkObject } from 'react-force-graph-2d';
import { Loader2 } from 'lucide-react';
import {
  buildGraphData,
  resolveNodeOpacity,
  resolveLinkOpacity,
  type CycleGraphNode,
  type CycleGraphLink,
} from './cyclesGraphUtils';
import { drawNode, drawLink, drawDotBackground } from './cyclesGraphCanvas';
import { useCyclesEgoCentric } from './useCyclesEgoCentric';

// ---------------------------------------------------------------------------
// CyclePanelProps
// ---------------------------------------------------------------------------

interface CyclePanelProps {
  cycle: string[];
  cycleIndex: number;
}

// ---------------------------------------------------------------------------
// CyclePanel component
// ---------------------------------------------------------------------------

export function CyclePanel({ cycle, cycleIndex }: CyclePanelProps) {
  const fgRef = useRef<ForceGraphMethods | undefined>(undefined);
  const [hoveredNodeId, setHoveredNodeId] = useState<string | null>(null);
  const [hovered, setHovered] = useState(false);

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
    <div
      className="rounded-[20px] overflow-hidden flex flex-col"
      style={{
        minHeight: 400,
        background: 'linear-gradient(135deg, #ffffff 0%, #f4f4f4 100%)',
        border: '1px solid #e2e2e7',
        transition: 'box-shadow 200ms ease',
        boxShadow: hovered ? '0 8px 24px -12px rgba(73,79,223,0.18)' : 'none',
      }}
      onMouseEnter={() => setHovered(true)}
      onMouseLeave={() => setHovered(false)}
    >
      {/* Card header */}
      <div
        className="flex items-center gap-3 px-6 pt-5 pb-4"
        style={{ borderBottom: '1px solid #e2e2e7' }}
      >
        <span
          className="inline-flex items-center px-3 py-1 rounded-full text-white font-semibold"
          style={{
            fontSize: 12,
            background: 'linear-gradient(135deg, #4f55f1 0%, #494fdf 60%, #3a40c4 100%)',
          }}
        >
          Ciclo #{cycleIndex + 1}
        </span>

        <Loader2
          size={14}
          style={{
            color: '#494fdf',
            animation: 'spin 8s linear infinite',
          }}
        />

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

      {/* Canvas graph area */}
      <div
        role="img"
        aria-label={`Grafo del ciclo #${cycleIndex + 1}`}
        className="flex-1"
      >
        <ForceGraph2D
          ref={fgRef}
          graphData={
            graphData as {
              nodes: NodeObject[];
              links: LinkObject[];
            }
          }
          width={480}
          height={320}
          backgroundColor="transparent"
          cooldownTime={2000}
          linkCurvature={0.15}
          nodeRelSize={6}
          nodeCanvasObject={(node, ctx) => {
            const typedNode = node as CycleGraphNode;
            const nodeId = String(node.id);
            const isSelected = nodeId === selectedNodeId;
            const isNeighbor = neighborIds.has(nodeId);
            drawNode(ctx, typedNode, {
              radius: 14,
              opacity: resolveNodeOpacity(nodeId, selectedNodeId, neighborIds),
              isSelected,
              isHovered: nodeId === hoveredNodeId,
              showBadge: true,
            });
            // Suppress default rendering
            void isNeighbor;
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
                width: 2.5,
                color: '#494fdf',
                dashPattern: [6, 4],
              },
            );
          }}
          linkCanvasObjectMode={() => 'replace'}
          onRenderFramePre={(ctx, globalScale) => {
            void globalScale;
            drawDotBackground(ctx, 480, 320, {
              width: 480,
              height: 320,
              dotSpacing: 20,
              dotColor: 'rgba(73,79,223,0.06)',
            });
          }}
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
    </div>
  );
}
