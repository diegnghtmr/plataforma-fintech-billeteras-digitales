import type { NodeObject, LinkObject } from 'react-force-graph-2d';

// ---------------------------------------------------------------------------
// Visual aesthetic — distill.pub-inspired sober tokens
// ---------------------------------------------------------------------------
//
// Distill renders graph figures in a near-monochrome canvas: pale grey resting
// nodes with a thin dark stroke, ONE accent (cobalt for us) for the focused
// node, and ash-grey 1px edges. No per-id colour. No drop shadow. No pill
// background behind labels. We mirror that here so analytics figures read as
// academic illustrations, not candy charts.
//
// Tokens are centralised here so future charts (BarChart, PieChart) can
// extend the same restrained palette without re-deriving values.
// ---------------------------------------------------------------------------

export const GRAPH_TOKENS = {
  node: {
    fillDefault: '#e8e8e8',
    fillSelected: '#494fdf', // brand cobalt — the one accent
    fillNeighbor: '#dee0f5', // pale cobalt tint — close family of selected
    fillDimmed: '#f0f0f0',
    stroke: '#4a4a4a',
    strokeSelected: '#3a40c4', // cobalt-deep
    radius: 12,
    radiusSelected: 13,
    label: {
      color: '#3a3d40', // charcoal
      colorSelected: '#3a40c4',
      font: '500 10px Inter, system-ui, sans-serif',
    },
  },
  edge: {
    strokeDefault: '#b5b8bc',
    strokeTop25: '#4a4a4a',
    strokeTop: '#494fdf',
    widthDefault: 1,
    widthTop25: 1.5,
    widthTop: 2,
    arrowSize: 5,
    opacityDimmed: 0.2,
  },
  canvas: {
    background: '#ffffff',
  },
} as const;

// ---------------------------------------------------------------------------
// Types
// ---------------------------------------------------------------------------

export interface CycleGraphNode extends NodeObject {
  id: string;
  cycleIndex: number;
  positionInCycle: number;
}

export interface CycleGraphLinkInput extends LinkObject {
  source: string;
  target: string;
}

export type CycleGraphLink = CycleGraphLinkInput;

export type NodeVisualState = 'default' | 'selected' | 'neighbor' | 'dimmed';

export interface NodeVisualStyle {
  fill: string;
  stroke: string;
  strokeWidth: number;
  labelColor: string;
  radius: number;
}

export interface DrawNodeOpts {
  state: NodeVisualState;
  opacity: number;
}

export interface DrawLinkOpts {
  opacity: number;
  width: number;
  color: string;
}

export interface DrawBackgroundOpts {
  width: number;
  height: number;
  dotSpacing: number;
  dotColor: string;
}

// ---------------------------------------------------------------------------
// resolveNodeStyle — single source of truth for node visuals
// ---------------------------------------------------------------------------

export function resolveNodeStyle(state: NodeVisualState): NodeVisualStyle {
  const t = GRAPH_TOKENS.node;
  switch (state) {
    case 'selected':
      return {
        fill: t.fillSelected,
        stroke: t.strokeSelected,
        strokeWidth: 1.5,
        labelColor: t.label.colorSelected,
        radius: t.radiusSelected,
      };
    case 'neighbor':
      return {
        fill: t.fillNeighbor,
        stroke: t.stroke,
        strokeWidth: 1,
        labelColor: t.label.color,
        radius: t.radius,
      };
    case 'dimmed':
      return {
        fill: t.fillDimmed,
        stroke: t.stroke,
        strokeWidth: 1,
        labelColor: t.label.color,
        radius: t.radius,
      };
    case 'default':
    default:
      return {
        fill: t.fillDefault,
        stroke: t.stroke,
        strokeWidth: 1,
        labelColor: t.label.color,
        radius: t.radius,
      };
  }
}

// ---------------------------------------------------------------------------
// resolveNodeState — maps ego-centric selection into a visual state
// ---------------------------------------------------------------------------

export function resolveNodeState(
  nodeId: string,
  selectedNodeId: string | null,
  neighborIds: Set<string>,
): NodeVisualState {
  if (selectedNodeId === null) return 'default';
  if (nodeId === selectedNodeId) return 'selected';
  if (neighborIds.has(nodeId)) return 'neighbor';
  return 'dimmed';
}

// ---------------------------------------------------------------------------
// buildGraphData
// ---------------------------------------------------------------------------

export function buildGraphData(cycles: string[][]): {
  nodes: CycleGraphNode[];
  links: CycleGraphLink[];
} {
  const nodes: CycleGraphNode[] = [];
  const links: CycleGraphLink[] = [];

  for (let cycleIndex = 0; cycleIndex < cycles.length; cycleIndex++) {
    const cycle = cycles[cycleIndex]!;
    for (let i = 0; i < cycle.length; i++) {
      nodes.push({
        id: cycle[i]!,
        cycleIndex,
        positionInCycle: i + 1,
      });
      const nextId = cycle[(i + 1) % cycle.length]!;
      links.push({ source: cycle[i]!, target: nextId });
    }
  }

  return { nodes, links };
}

// ---------------------------------------------------------------------------
// deriveNeighbors
// ---------------------------------------------------------------------------

export function deriveNeighbors(
  nodeId: string,
  links: CycleGraphLink[],
): Set<string> {
  const neighbors = new Set<string>();
  for (const link of links) {
    const src = resolveIdFromString(link.source);
    const tgt = resolveIdFromString(link.target);
    if (src === nodeId) neighbors.add(tgt);
    if (tgt === nodeId) neighbors.add(src);
  }
  return neighbors;
}

function resolveIdFromString(x: string | NodeObject | number | undefined): string {
  if (typeof x === 'string') return x;
  if (typeof x === 'number') return String(x);
  if (x != null && typeof x === 'object' && 'id' in x) return String(x.id);
  return '';
}

// ---------------------------------------------------------------------------
// resolveNodeOpacity
// ---------------------------------------------------------------------------
//
// Distill restraint: dimmed nodes drop to 0.2 (slightly stronger fade than the
// previous 0.25 — closer to "almost gone, structurally present" which is the
// editorial intent of distill's ego-centric callouts).
// ---------------------------------------------------------------------------

export function resolveNodeOpacity(
  nodeId: string,
  selectedNodeId: string | null,
  neighborIds: Set<string>,
): number {
  if (selectedNodeId === null) return 1.0;
  if (nodeId === selectedNodeId) return 1.0;
  if (neighborIds.has(nodeId)) return 1.0;
  return 0.2;
}

// ---------------------------------------------------------------------------
// resolveLinkOpacity
// ---------------------------------------------------------------------------

export function resolveLinkOpacity(
  source: string | NodeObject,
  target: string | NodeObject,
  selectedNodeId: string | null,
  neighborIds: Set<string>,
): number {
  if (selectedNodeId === null) return 1.0;
  const src = resolveIdFromString(source as string | NodeObject | number | undefined);
  const tgt = resolveIdFromString(target as string | NodeObject | number | undefined);
  if (
    src === selectedNodeId ||
    tgt === selectedNodeId ||
    neighborIds.has(src) ||
    neighborIds.has(tgt)
  ) {
    return 1.0;
  }
  return GRAPH_TOKENS.edge.opacityDimmed;
}
