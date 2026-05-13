import type { TransactionResponse } from '../../api/transactions';

interface TransactionRowProps {
  tx: TransactionResponse;
  onReverse: (txId: string) => void;
  isReverting: boolean;
}

export function TransactionRow({ tx, onReverse, isReverting }: TransactionRowProps) {
  const canRevert = tx.reversible && tx.status === 'SUCCESSFUL';

  return (
    <tr className="border-b border-surface-fg/10 text-sm text-canvas-fg">
      <td className="py-2 px-3 font-mono">{tx.id}</td>
      <td className="py-2 px-3">{new Date(tx.timestamp).toLocaleString()}</td>
      <td className="py-2 px-3">{tx.type}</td>
      <td className="py-2 px-3">{tx.status}</td>
      <td className="py-2 px-3 text-right">{tx.amount.toFixed(2)}</td>
      <td className="py-2 px-3">{tx.sourceWalletId ?? '—'}</td>
      <td className="py-2 px-3">{tx.targetWalletId ?? '—'}</td>
      <td className="py-2 px-3 text-center">{tx.reversible ? 'Sí' : 'No'}</td>
      <td className="py-2 px-3">
        <button
          onClick={() => onReverse(tx.id)}
          disabled={!canRevert || isReverting}
          className={`px-3 py-1 rounded text-sm font-medium transition-colors
            ${canRevert
              ? 'bg-accent text-accent-fg hover:opacity-80'
              : 'bg-surface-fg/10 text-surface-fg/40 cursor-not-allowed'
            }`}
        >
          Revertir
        </button>
      </td>
    </tr>
  );
}
