import { useEffect, useMemo, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import {
  ReactFlow,
  Background,
  BackgroundVariant,
  Controls,
  Handle,
  Position,
  MarkerType,
  type Edge,
  type Node,
  type NodeTypes,
} from '@xyflow/react';
import '@xyflow/react/dist/style.css';
import {
  ArrowLeft,
  CircleUser,
  Wallet,
  Network,
  Sparkles,
  ShieldAlert,
  Database,
  Bell,
  Play,
  Pause,
  RotateCcw,
  StepForward,
  StepBack,
} from 'lucide-react';
import { useTransactionQuery } from './hooks';
import { buildFlowSteps, type FlowStep, type NodeKey } from './transferFlowSteps';
import { labelOperationType, labelOperationStatus } from '../../shared/i18n/enum-labels';
import type { TransactionResponse } from '../../api/transactions';

const COBALT = '#494fdf';
const COBALT_DEEP = '#3a40c4';
const SLATE = '#4a4a4a';

interface FlowNodeData extends Record<string, unknown> {
  label: string;
  sublabel?: string;
  icon: 'user' | 'wallet' | 'graph' | 'points' | 'fraud' | 'persistence' | 'notification';
  active: boolean;
}

function FlowNode({ data }: { data: FlowNodeData }) {
  const Icon = {
    user: CircleUser,
    wallet: Wallet,
    graph: Network,
    points: Sparkles,
    fraud: ShieldAlert,
    persistence: Database,
    notification: Bell,
  }[data.icon];

  return (
    <div
      className={`relative flex flex-col items-center justify-center w-32 h-20 rounded-lg border-2 transition-all duration-300 ${
        data.active
          ? 'bg-[#dee0f5] border-[#3a40c4] shadow-lg scale-105'
          : 'bg-surface border-surface-fg/15'
      }`}
    >
      <Handle type="target" position={Position.Left} style={{ background: SLATE, width: 6, height: 6 }} />
      <Icon
        size={20}
        color={data.active ? COBALT_DEEP : SLATE}
        strokeWidth={data.active ? 2.5 : 1.75}
      />
      <div
        className={`mt-1 text-[11px] font-medium ${
          data.active ? 'text-[#3a40c4]' : 'text-canvas-fg'
        }`}
      >
        {data.label}
      </div>
      {data.sublabel && (
        <div className="text-[9px] font-mono text-surface-fg">{data.sublabel}</div>
      )}
      <Handle type="source" position={Position.Right} style={{ background: SLATE, width: 6, height: 6 }} />
    </div>
  );
}

const nodeTypes: NodeTypes = { flow: FlowNode };

// Manual layout — readable left-to-right pipeline with side branches for puntos/fraude/notif
const NODE_POSITIONS: Record<NodeKey, { x: number; y: number }> = {
  sourceUser: { x: 0, y: 160 },
  sourceWallet: { x: 180, y: 160 },
  graph: { x: 360, y: 60 },
  targetWallet: { x: 540, y: 160 },
  targetUser: { x: 720, y: 160 },
  points: { x: 360, y: 320 },
  fraud: { x: 540, y: 320 },
  persistence: { x: 360, y: 440 },
  notification: { x: 720, y: 440 },
};

function buildNodes(tx: TransactionResponse, activeNodes: Set<NodeKey>): Node<FlowNodeData>[] {
  const isExternal =
    tx.type === 'EXTERNAL_TRANSFER' ||
    tx.type === 'EXTERNAL_TRANSFER_SENT' ||
    tx.type === 'EXTERNAL_TRANSFER_RECEIVED';
  const isInternal = tx.type === 'INTERNAL_TRANSFER';
  const hasTarget = isExternal || isInternal;

  const all: Array<[NodeKey, FlowNodeData]> = [
    ['sourceUser', { label: 'Usuario origen', sublabel: tx.sourceUserId, icon: 'user', active: false }],
    ['sourceWallet', { label: 'Billetera origen', sublabel: tx.sourceWalletId ?? '—', icon: 'wallet', active: false }],
    ['points', { label: `Puntos +${tx.pointsGenerated ?? 0}`, icon: 'points', active: false }],
    ['fraud', { label: 'Fraude', sublabel: tx.riskLevel ?? 'sin alerta', icon: 'fraud', active: false }],
    ['persistence', { label: 'Persistencia', sublabel: tx.id, icon: 'persistence', active: false }],
    ['notification', { label: 'Notificación', icon: 'notification', active: false }],
  ];

  if (hasTarget) {
    all.push(['targetWallet', { label: 'Billetera destino', sublabel: tx.targetWalletId ?? '—', icon: 'wallet', active: false }]);
    all.push(['targetUser', { label: 'Usuario destino', sublabel: tx.targetUserId ?? '—', icon: 'user', active: false }]);
  }
  if (isExternal) {
    all.push(['graph', { label: 'GrafoTransferencias', sublabel: 'addEdge', icon: 'graph', active: false }]);
  }

  return all.map(([key, data]) => ({
    id: key,
    type: 'flow',
    data: { ...data, active: activeNodes.has(key) },
    position: NODE_POSITIONS[key],
    draggable: false,
  }));
}

function buildEdges(tx: TransactionResponse, activeEdge?: { from: NodeKey; to: NodeKey }): Edge[] {
  const isExternal =
    tx.type === 'EXTERNAL_TRANSFER' ||
    tx.type === 'EXTERNAL_TRANSFER_SENT' ||
    tx.type === 'EXTERNAL_TRANSFER_RECEIVED';
  const isInternal = tx.type === 'INTERNAL_TRANSFER';
  const hasTarget = isExternal || isInternal;

  const candidates: Array<{ from: NodeKey; to: NodeKey }> = [
    { from: 'sourceUser', to: 'sourceWallet' },
    { from: 'sourceWallet', to: 'points' },
    { from: 'sourceWallet', to: 'fraud' },
    { from: 'persistence', to: 'notification' },
  ];
  if (hasTarget) {
    candidates.push({ from: 'sourceWallet', to: 'targetWallet' });
    candidates.push({ from: 'targetWallet', to: 'targetUser' });
    candidates.push({ from: 'targetWallet', to: 'points' });
  }
  if (isExternal) {
    candidates.push({ from: 'sourceUser', to: 'targetUser' });
    candidates.push({ from: 'sourceWallet', to: 'targetUser' });
  }

  return candidates.map(({ from, to }) => {
    const isActive = activeEdge && activeEdge.from === from && activeEdge.to === to;
    return {
      id: `${from}->${to}`,
      source: from,
      target: to,
      animated: isActive,
      markerEnd: {
        type: MarkerType.ArrowClosed,
        color: isActive ? COBALT : '#b5b8bc',
      },
      style: {
        stroke: isActive ? COBALT : '#b5b8bc',
        strokeWidth: isActive ? 2 : 1,
      },
    };
  });
}

const PLAY_INTERVAL_MS = 1400;

function useStepPlayer(steps: FlowStep[]) {
  const [index, setIndex] = useState(0);
  const [playing, setPlaying] = useState(true);

  useEffect(() => {
    if (!playing) return;
    if (index >= steps.length - 1) {
      setPlaying(false);
      return;
    }
    const id = window.setTimeout(() => setIndex((i) => i + 1), PLAY_INTERVAL_MS);
    return () => window.clearTimeout(id);
  }, [playing, index, steps.length]);

  const goto = (next: number) => {
    setPlaying(false);
    setIndex(Math.max(0, Math.min(steps.length - 1, next)));
  };

  return {
    index,
    playing,
    play: () => setPlaying(true),
    pause: () => setPlaying(false),
    next: () => goto(index + 1),
    prev: () => goto(index - 1),
    replay: () => {
      setIndex(0);
      setPlaying(true);
    },
    jumpTo: (i: number) => goto(i),
  };
}

export function TransferFlowPage() {
  const { transactionId } = useParams<{ transactionId: string }>();
  const { data: tx, isLoading, error } = useTransactionQuery(transactionId);

  const steps = useMemo<FlowStep[]>(() => (tx ? buildFlowSteps(tx) : []), [tx]);
  const player = useStepPlayer(steps);

  const currentStep = steps[player.index];
  const activeNodes = useMemo(
    () => new Set<NodeKey>(currentStep?.activeNodes ?? []),
    [currentStep]
  );

  const nodes = useMemo(() => (tx ? buildNodes(tx, activeNodes) : []), [tx, activeNodes]);
  const edges = useMemo(
    () => (tx ? buildEdges(tx, currentStep?.activeEdge) : []),
    [tx, currentStep]
  );

  if (isLoading) {
    return <div className="p-6 text-canvas-fg">Cargando transacción…</div>;
  }
  if (error || !tx) {
    return (
      <div className="p-6">
        <Link to="/transactions" className="inline-flex items-center gap-2 text-canvas-fg hover:underline">
          <ArrowLeft size={16} /> Volver
        </Link>
        <div className="mt-4 text-red-600">No se pudo cargar la transacción.</div>
      </div>
    );
  }

  return (
    <div className="p-6 space-y-6">
      <header className="flex items-center justify-between gap-4">
        <div>
          <Link
            to="/transactions"
            className="inline-flex items-center gap-2 text-sm text-surface-fg hover:text-canvas-fg"
          >
            <ArrowLeft size={14} /> Transacciones
          </Link>
          <h1 className="mt-2 text-2xl font-semibold text-canvas-fg">
            Flujo paso a paso · {labelOperationType(tx.type)}
          </h1>
          <p className="text-sm text-surface-fg">
            <span className="font-mono">{tx.id}</span> · {labelOperationStatus(tx.status)} ·{' '}
            {new Date(tx.timestamp).toLocaleString()}
          </p>
        </div>
      </header>

      <section
        className="rounded-lg border border-surface-fg/10 bg-canvas"
        style={{ height: 580 }}
        aria-label="Diagrama de flujo de la transferencia"
      >
        <ReactFlow
          nodes={nodes}
          edges={edges}
          nodeTypes={nodeTypes}
          fitView
          fitViewOptions={{ padding: 0.2 }}
          nodesDraggable={false}
          nodesConnectable={false}
          elementsSelectable={false}
          proOptions={{ hideAttribution: true }}
        >
          <Background variant={BackgroundVariant.Dots} gap={20} size={1} color="#e0e0e0" />
          <Controls showInteractive={false} />
        </ReactFlow>
      </section>

      <section className="grid gap-4 lg:grid-cols-[1fr_320px]">
        <div className="rounded-lg border border-surface-fg/10 bg-surface p-4">
          <div className="flex items-center gap-2 mb-3 text-canvas-fg">
            <button
              type="button"
              onClick={player.prev}
              disabled={player.index === 0}
              className="p-2 rounded border border-surface-fg/15 bg-canvas hover:bg-surface-soft disabled:opacity-40 disabled:cursor-not-allowed text-canvas-fg"
              aria-label="Paso anterior"
            >
              <StepBack size={16} strokeWidth={2} />
            </button>
            {player.playing ? (
              <button
                type="button"
                onClick={player.pause}
                className="p-2 rounded border border-surface-fg/15 bg-canvas hover:bg-surface-soft text-canvas-fg"
                aria-label="Pausar"
              >
                <Pause size={16} strokeWidth={2} />
              </button>
            ) : (
              <button
                type="button"
                onClick={player.play}
                className="p-2 rounded border border-surface-fg/15 bg-canvas hover:bg-surface-soft text-canvas-fg"
                aria-label="Reproducir"
              >
                <Play size={16} strokeWidth={2} />
              </button>
            )}
            <button
              type="button"
              onClick={player.next}
              disabled={player.index === steps.length - 1}
              className="p-2 rounded border border-surface-fg/15 bg-canvas hover:bg-surface-soft disabled:opacity-40 disabled:cursor-not-allowed text-canvas-fg"
              aria-label="Paso siguiente"
            >
              <StepForward size={16} strokeWidth={2} />
            </button>
            <button
              type="button"
              onClick={player.replay}
              className="p-2 rounded border border-surface-fg/15 bg-canvas hover:bg-surface-soft text-canvas-fg"
              aria-label="Reiniciar"
            >
              <RotateCcw size={16} strokeWidth={2} />
            </button>
            <span className="ml-3 text-xs text-surface-fg">
              Paso {player.index + 1} de {steps.length}
            </span>
          </div>

          <ol className="space-y-2">
            {steps.map((s, i) => {
              const isActive = i === player.index;
              const isPast = i < player.index;
              return (
                <li key={s.id}>
                  <button
                    type="button"
                    onClick={() => player.jumpTo(i)}
                    className={`w-full text-left rounded-md px-3 py-2 transition-all border ${
                      isActive
                        ? 'bg-[#dee0f5] border-[#3a40c4] text-[#3a40c4]'
                        : isPast
                          ? 'bg-canvas border-surface-fg/10 text-canvas-fg/70'
                          : 'bg-canvas border-surface-fg/10 text-canvas-fg hover:bg-surface'
                    }`}
                  >
                    <div className="flex items-baseline gap-2">
                      <span
                        className={`inline-flex items-center justify-center w-5 h-5 rounded-full text-[10px] font-semibold ${
                          isActive
                            ? 'bg-[#3a40c4] text-white'
                            : isPast
                              ? 'bg-canvas-fg/30 text-white'
                              : 'bg-surface-fg/15 text-canvas-fg'
                        }`}
                      >
                        {i + 1}
                      </span>
                      <span className="text-sm font-medium">{s.title}</span>
                    </div>
                    {isActive && (
                      <p className="mt-1 ml-7 text-xs text-[#3a40c4]/85 font-mono">{s.detail}</p>
                    )}
                  </button>
                </li>
              );
            })}
          </ol>
        </div>

        <aside className="rounded-lg border border-surface-fg/10 bg-surface p-4 text-sm space-y-2">
          <h2 className="font-semibold text-canvas-fg">Resumen</h2>
          <dl className="grid grid-cols-2 gap-1.5 text-xs">
            <dt className="text-surface-fg">Monto</dt>
            <dd className="text-canvas-fg font-medium">{tx.amount.toFixed(2)}</dd>
            <dt className="text-surface-fg">Origen</dt>
            <dd className="font-mono text-canvas-fg">{tx.sourceUserId}</dd>
            <dt className="text-surface-fg">Destino</dt>
            <dd className="font-mono text-canvas-fg">{tx.targetUserId ?? '—'}</dd>
            <dt className="text-surface-fg">Puntos</dt>
            <dd className="text-canvas-fg">+{tx.pointsGenerated ?? 0}</dd>
            <dt className="text-surface-fg">Riesgo</dt>
            <dd className="text-canvas-fg">{tx.riskLevel ?? 'sin alerta'}</dd>
            <dt className="text-surface-fg">Reversible</dt>
            <dd className="text-canvas-fg">{tx.reversible ? 'Sí' : 'No'}</dd>
          </dl>
          {tx.description && (
            <p className="pt-2 mt-2 border-t border-surface-fg/10 text-xs text-canvas-fg/80">
              {tx.description}
            </p>
          )}
        </aside>
      </section>
    </div>
  );
}
