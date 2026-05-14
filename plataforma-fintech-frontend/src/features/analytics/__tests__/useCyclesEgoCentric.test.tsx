import { describe, it, expect } from 'vitest';
import { renderHook, act } from '@testing-library/react';
import { useCyclesEgoCentric } from '../useCyclesEgoCentric';
import type { CycleGraphLink } from '../cyclesGraphUtils';

const linksABC: CycleGraphLink[] = [
  { source: 'A', target: 'B' },
  { source: 'B', target: 'C' },
  { source: 'C', target: 'A' },
];

describe('useCyclesEgoCentric', () => {
  it('starts with null selectedNodeId and empty neighborIds', () => {
    const { result } = renderHook(() => useCyclesEgoCentric(linksABC));
    expect(result.current.selectedNodeId).toBeNull();
    expect(result.current.neighborIds.size).toBe(0);
  });

  it('updates selectedNodeId and derives neighborIds on selectNode', () => {
    const { result } = renderHook(() => useCyclesEgoCentric(linksABC));

    act(() => {
      result.current.selectNode('B');
    });

    expect(result.current.selectedNodeId).toBe('B');
    // B is in A→B (neighbor: A) and B→C (neighbor: C)
    expect(result.current.neighborIds).toEqual(new Set(['A', 'C']));
  });

  it('clears selection when selectNode(null) is called', () => {
    const { result } = renderHook(() => useCyclesEgoCentric(linksABC));

    act(() => {
      result.current.selectNode('B');
    });
    act(() => {
      result.current.selectNode(null);
    });

    expect(result.current.selectedNodeId).toBeNull();
    expect(result.current.neighborIds.size).toBe(0);
  });

  it('recomputes neighborIds when links change', () => {
    let links = linksABC;
    const { result, rerender } = renderHook(() => useCyclesEgoCentric(links));

    act(() => {
      result.current.selectNode('A');
    });

    // A's neighbors in ABC: B (outgoing) and C (incoming)
    expect(result.current.neighborIds).toEqual(new Set(['B', 'C']));

    // Change to a different set of links (only A→B)
    links = [{ source: 'A', target: 'B' }];
    rerender();

    // Now A only has B as neighbor
    expect(result.current.neighborIds).toEqual(new Set(['B']));
  });
});
