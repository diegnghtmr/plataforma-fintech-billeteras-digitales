import { Workflow } from 'lucide-react';
import { CyclePanel } from './CyclePanel';

// ---------------------------------------------------------------------------
// CyclesGraph — public component
// ---------------------------------------------------------------------------

interface CyclesGraphProps {
  cycles: string[][];
}

export function CyclesGraph({ cycles }: CyclesGraphProps) {
  if (cycles.length === 0) {
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
          <Workflow
            style={{ color: '#8d969e' }}
            size={36}
            strokeWidth={1.5}
          />
        </div>
        <div className="flex flex-col gap-1.5">
          <p className="text-body-md-bold text-ink">Sin ciclos detectados</p>
          <p className="text-body-sm" style={{ color: '#8d969e', maxWidth: 300 }}>
            El grafo de transferencias no presenta ciclos en este momento.
          </p>
        </div>
      </div>
    );
  }

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
      {cycles.map((cycle, i) => (
        <CyclePanel key={i} cycle={cycle} cycleIndex={i} />
      ))}
    </div>
  );
}
