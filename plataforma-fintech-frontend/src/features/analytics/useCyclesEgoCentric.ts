import { useState, useMemo } from 'react';
import { deriveNeighbors, type CycleGraphLink } from './cyclesGraphUtils';

export function useCyclesEgoCentric(links: CycleGraphLink[]) {
  const [selectedNodeId, setSelectedNodeId] = useState<string | null>(null);

  const neighborIds = useMemo(
    () =>
      selectedNodeId === null
        ? new Set<string>()
        : deriveNeighbors(selectedNodeId, links),
    [selectedNodeId, links],
  );

  return { selectedNodeId, selectNode: setSelectedNodeId, neighborIds };
}
