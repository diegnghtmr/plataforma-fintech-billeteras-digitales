import { Undo2 } from 'lucide-react';
import type { TransactionResponse } from '../../api/transactions';

interface TransactionRowProps {
  tx: TransactionResponse;
  onReverse: (txId: string) => void;
  isReverting: boolean;
}

export function TransactionRow({ tx, onReverse, isReverting }: TransactionRowProps) {
  const canRevert = tx.reversible && tx.status === 'SUCCESSFUL';

  return (
    <tr className="border-b border-hairline-light text-sm text-ink">
      <td className="py-2.5 px-3 font-mono text-stone">{tx.id}</td>
      <td className="py-2.5 px-3 text-stone">{new Date(tx.timestamp).toLocaleString()}</td>
      <td className="py-2.5 px-3">
        <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold bg-surface-soft text-charcoal">
          {tx.type}
        </span>
      </td>
      <td className="py-2.5 px-3 text-mute">{tx.status}</td>
      <td className="py-2.5 px-3 text-right font-semibold text-ink">{tx.amount.toFixed(2)}</td>
      <td className="py-2.5 px-3 text-stone font-mono text-xs">{tx.sourceWalletId ?? '—'}</td>
      <td className="py-2.5 px-3 text-stone font-mono text-xs">{tx.targetWalletId ?? '—'}</td>
      <td className="py-2.5 px-3 text-center text-mute">{tx.reversible ? 'Sí' : 'No'}</td>
      <td className="py-2.5 px-3">
        <button
          onClick={() => onReverse(tx.id)}
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
      </td>
    </tr>
  );
}
