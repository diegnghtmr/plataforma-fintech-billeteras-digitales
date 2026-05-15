import { GitBranch, Undo2 } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import type { TransactionResponse } from '../../api/transactions';
import { labelOperationType, labelOperationStatus } from '../../shared/i18n/enum-labels';

interface TransactionRowProps {
  tx: TransactionResponse;
  onReverse: (txId: string) => void;
  isReverting: boolean;
}

export function TransactionRow({ tx, onReverse, isReverting }: TransactionRowProps) {
  const canRevert = tx.reversible && tx.status === 'SUCCESSFUL';
  const navigate = useNavigate();

  function openFlow() {
    navigate(`/transactions/${tx.id}/flow`);
  }

  function handleKeyDown(e: React.KeyboardEvent<HTMLTableRowElement>) {
    if (e.target !== e.currentTarget) return;
    if (e.key === 'Enter' || e.key === ' ') {
      e.preventDefault();
      openFlow();
    }
  }

  return (
    <tr
      role="link"
      tabIndex={0}
      onClick={openFlow}
      onKeyDown={handleKeyDown}
      aria-label={`Ver flujo paso a paso de ${tx.id}`}
      className="border-b border-hairline-light text-sm text-ink cursor-pointer hover:bg-surface-soft focus-visible:outline-none focus-visible:bg-surface-soft focus-visible:ring-2 focus-visible:ring-brand focus-visible:ring-inset transition-colors"
    >
      <td className="py-2.5 px-3 font-mono text-stone">{tx.id}</td>
      <td className="py-2.5 px-3 text-stone">{new Date(tx.timestamp).toLocaleString()}</td>
      <td className="py-2.5 px-3">
        <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold bg-surface-soft text-charcoal">
          {labelOperationType(tx.type)}
        </span>
      </td>
      <td className="py-2.5 px-3 text-mute">{labelOperationStatus(tx.status)}</td>
      <td className="py-2.5 px-3 text-right font-semibold text-ink">{tx.amount.toFixed(2)}</td>
      <td className="py-2.5 px-3 text-stone font-mono text-xs">{tx.sourceWalletId ?? '—'}</td>
      <td className="py-2.5 px-3 text-stone font-mono text-xs">{tx.targetWalletId ?? '—'}</td>
      <td className="py-2.5 px-3 text-center text-mute">{tx.reversible ? 'Sí' : 'No'}</td>
      <td className="py-2.5 px-3">
        <div className="flex items-center justify-end gap-2">
          <button
            type="button"
            onClick={(e) => {
              e.stopPropagation();
              openFlow();
            }}
            aria-label={`Ver flujo paso a paso de ${tx.id}`}
            className="inline-flex items-center gap-1.5 justify-center rounded-full text-xs font-semibold tracking-wide px-3 py-1.5 bg-canvas-light border border-hairline-strong text-ink hover:opacity-70 transition-opacity focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand"
          >
            <GitBranch size={12} strokeWidth={2} />
            Flujo
          </button>
          <button
            type="button"
            onClick={(e) => {
              e.stopPropagation();
              onReverse(tx.id);
            }}
            disabled={!canRevert || isReverting}
            className={`inline-flex items-center gap-1.5 justify-center rounded-full text-xs font-semibold tracking-wide px-3 py-1.5 transition-opacity focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand ${
              canRevert
                ? 'bg-canvas-light border border-hairline-strong text-ink hover:opacity-70'
                : 'bg-surface-soft text-faint cursor-not-allowed'
            }`}
          >
            <Undo2 size={12} strokeWidth={2} />
            Revertir
          </button>
        </div>
      </td>
    </tr>
  );
}
