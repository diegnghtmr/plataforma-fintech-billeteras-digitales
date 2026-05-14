import { describe, it, expect } from 'vitest';
import {
  buildGraphData,
  deriveNeighbors,
  resolveNodeOpacity,
  resolveLinkOpacity,
  resolveNodeState,
  resolveNodeStyle,
  GRAPH_TOKENS,
  type CycleGraphLink,
} from '../cyclesGraphUtils';

// ---------------------------------------------------------------------------
// resolveNodeState — maps ego-centric selection to a visual state
// ---------------------------------------------------------------------------

describe('resolveNodeState', () => {
  const neighbors = new Set(['A', 'C']);

  it('returns "default" when there is no selection', () => {
    expect(resolveNodeState('B', null, new Set())).toBe('default');
  });

  it('returns "selected" for the selected node', () => {
    expect(resolveNodeState('B', 'B', neighbors)).toBe('selected');
  });

  it('returns "neighbor" for a neighbor of the selected node', () => {
    expect(resolveNodeState('A', 'B', neighbors)).toBe('neighbor');
    expect(resolveNodeState('C', 'B', neighbors)).toBe('neighbor');
  });

  it('returns "dimmed" for an unrelated node when selection is active', () => {
    expect(resolveNodeState('X', 'B', neighbors)).toBe('dimmed');
  });
});

// ---------------------------------------------------------------------------
// resolveNodeStyle — distill aesthetic tokens
// ---------------------------------------------------------------------------

describe('resolveNodeStyle', () => {
  it('returns the pale grey resting palette for "default"', () => {
    const s = resolveNodeStyle('default');
    expect(s.fill).toBe(GRAPH_TOKENS.node.fillDefault);
    expect(s.stroke).toBe(GRAPH_TOKENS.node.stroke);
    expect(s.radius).toBe(GRAPH_TOKENS.node.radius);
  });

  it('returns the cobalt accent palette for "selected"', () => {
    const s = resolveNodeStyle('selected');
    expect(s.fill).toBe(GRAPH_TOKENS.node.fillSelected);
    expect(s.stroke).toBe(GRAPH_TOKENS.node.strokeSelected);
    expect(s.radius).toBe(GRAPH_TOKENS.node.radiusSelected);
  });

  it('returns a pale cobalt tint for "neighbor"', () => {
    const s = resolveNodeStyle('neighbor');
    expect(s.fill).toBe(GRAPH_TOKENS.node.fillNeighbor);
    expect(s.stroke).toBe(GRAPH_TOKENS.node.stroke);
  });

  it('returns a very pale grey for "dimmed"', () => {
    const s = resolveNodeStyle('dimmed');
    expect(s.fill).toBe(GRAPH_TOKENS.node.fillDimmed);
  });
});

// ---------------------------------------------------------------------------
// buildGraphData
// ---------------------------------------------------------------------------

describe('buildGraphData', () => {
  it('returns empty nodes and links for empty input', () => {
    const result = buildGraphData([]);
    expect(result).toEqual({ nodes: [], links: [] });
  });

  it('builds correct nodes and closed ring links for a single cycle', () => {
    const result = buildGraphData([['A', 'B', 'C']]);
    expect(result.nodes).toHaveLength(3);
    expect(result.links).toHaveLength(3);

    // Nodes have correct metadata
    const nodeA = result.nodes.find((n) => n.id === 'A');
    const nodeB = result.nodes.find((n) => n.id === 'B');
    const nodeC = result.nodes.find((n) => n.id === 'C');
    expect(nodeA).toBeDefined();
    expect(nodeB).toBeDefined();
    expect(nodeC).toBeDefined();
    expect(nodeA!.cycleIndex).toBe(0);
    expect(nodeA!.positionInCycle).toBe(1);
    expect(nodeB!.positionInCycle).toBe(2);
    expect(nodeC!.positionInCycle).toBe(3);

    // Closed ring: A→B, B→C, C→A
    const linkAB = result.links.find((l) => l.source === 'A' && l.target === 'B');
    const linkBC = result.links.find((l) => l.source === 'B' && l.target === 'C');
    const linkCA = result.links.find((l) => l.source === 'C' && l.target === 'A');
    expect(linkAB).toBeDefined();
    expect(linkBC).toBeDefined();
    expect(linkCA).toBeDefined();
  });

  it('builds correct nodes for multiple cycles with correct cycleIndex', () => {
    const result = buildGraphData([
      ['A', 'B', 'C', 'D'],
      ['X', 'Y', 'Z', 'W', 'V'],
    ]);
    expect(result.nodes).toHaveLength(9);
    expect(result.links).toHaveLength(9);

    const nodesInCycle0 = result.nodes.filter((n) => n.cycleIndex === 0);
    const nodesInCycle1 = result.nodes.filter((n) => n.cycleIndex === 1);
    expect(nodesInCycle0).toHaveLength(4);
    expect(nodesInCycle1).toHaveLength(5);
  });
});

describe('deriveNeighbors', () => {
  // Cycle: A→B, B→C, C→A
  const linksABC: CycleGraphLink[] = [
    { source: 'A', target: 'B' },
    { source: 'B', target: 'C' },
    { source: 'C', target: 'A' },
  ];

  it('returns both incoming and outgoing neighbors for B', () => {
    const neighbors = deriveNeighbors('B', linksABC);
    expect(neighbors).toEqual(new Set(['A', 'C']));
  });

  it('returns both incoming and outgoing neighbors for A', () => {
    const neighbors = deriveNeighbors('A', linksABC);
    // A→B (outgoing: B), C→A (incoming: C)
    expect(neighbors).toEqual(new Set(['C', 'B']));
  });

  it('returns empty set for a node not in any links', () => {
    const neighbors = deriveNeighbors('Z', linksABC);
    expect(neighbors.size).toBe(0);
  });
});

describe('resolveNodeOpacity', () => {
  const neighbors = new Set(['A', 'C']);

  it('returns 1.0 when there is no selection', () => {
    expect(resolveNodeOpacity('B', null, new Set())).toBe(1.0);
    expect(resolveNodeOpacity('X', null, new Set())).toBe(1.0);
  });

  it('returns 1.0 for the selected node', () => {
    expect(resolveNodeOpacity('B', 'B', neighbors)).toBe(1.0);
  });

  it('returns 1.0 for a neighbor of the selected node', () => {
    expect(resolveNodeOpacity('A', 'B', neighbors)).toBe(1.0);
    expect(resolveNodeOpacity('C', 'B', neighbors)).toBe(1.0);
  });

  it('returns 0.2 for an unrelated node (distill-restraint fade)', () => {
    expect(resolveNodeOpacity('X', 'B', new Set(['A']))).toBe(0.2);
  });
});

describe('resolveLinkOpacity', () => {
  const neighbors = new Set(['A', 'C']);

  it('returns 1.0 when there is no selection', () => {
    expect(resolveLinkOpacity('A', 'B', null, new Set())).toBe(1.0);
  });

  it('returns 1.0 when source is selected', () => {
    expect(resolveLinkOpacity('B', 'C', 'B', neighbors)).toBe(1.0);
  });

  it('returns 1.0 when target is selected', () => {
    expect(resolveLinkOpacity('A', 'B', 'B', neighbors)).toBe(1.0);
  });

  it('returns 1.0 when source is a neighbor', () => {
    expect(resolveLinkOpacity('A', 'X', 'B', neighbors)).toBe(1.0);
  });

  it('returns 1.0 when target is a neighbor', () => {
    expect(resolveLinkOpacity('X', 'C', 'B', neighbors)).toBe(1.0);
  });

  it('returns the dimmed token (0.2) when both endpoints are unrelated', () => {
    expect(resolveLinkOpacity('X', 'Y', 'B', new Set(['A']))).toBe(
      GRAPH_TOKENS.edge.opacityDimmed,
    );
    expect(resolveLinkOpacity('X', 'Y', 'B', new Set(['A']))).toBe(0.2);
  });
});
