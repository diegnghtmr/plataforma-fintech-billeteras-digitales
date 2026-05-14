import type { NodeObject, LinkObject } from 'react-force-graph-2d';

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

export interface DrawNodeOpts {
  radius: number;
  opacity: number;
  isSelected: boolean;
  isHovered: boolean;
  showBadge: boolean;
}

export interface DrawLinkOpts {
  opacity: number;
  width: number;
  color: string;
  dashPattern: [number, number];
}

export interface DrawBackgroundOpts {
  width: number;
  height: number;
  dotSpacing: number;
  dotColor: string;
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

export function resolveNodeOpacity(
  nodeId: string,
  selectedNodeId: string | null,
  neighborIds: Set<string>,
): number {
  if (selectedNodeId === null) return 1.0;
  if (nodeId === selectedNodeId) return 1.0;
  if (neighborIds.has(nodeId)) return 1.0;
  return 0.25;
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
  return 0.18;
}
