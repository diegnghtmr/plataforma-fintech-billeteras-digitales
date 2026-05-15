import { Link } from 'react-router-dom';
import { GitBranch } from 'lucide-react';
import type { TransactionResponse } from '../../api/transactions';
import { labelOperationType, labelOperationStatus } from '../../shared/i18n/enum-labels';

interface TransactionDetailProps {
  tx: TransactionResponse;
}

export function TransactionDetail({ tx }: TransactionDetailProps) {
  return (
    <div className="p-4 bg-surface rounded-lg border border-surface-fg/10 text-sm">
      <div className="flex items-center justify-between mb-3">
        <h3 className="font-semibold text-canvas-fg">Detalle de transacción</h3>
        <Link
          to={`/transactions/${tx.id}/flow`}
          className="inline-flex items-center gap-1.5 text-xs font-medium text-[#3a40c4] hover:underline"
        >
          <GitBranch size={14} /> Ver flujo paso a paso
        </Link>
      </div>
      <dl className="grid grid-cols-2 gap-2">
        <dt className="text-surface-fg">ID</dt>
        <dd className="font-mono text-canvas-fg">{tx.id}</dd>
        <dt className="text-surface-fg">Tipo</dt>
        <dd className="text-canvas-fg">{labelOperationType(tx.type)}</dd>
        <dt className="text-surface-fg">Estado</dt>
        <dd className="text-canvas-fg">{labelOperationStatus(tx.status)}</dd>
        <dt className="text-surface-fg">Monto</dt>
        <dd className="text-canvas-fg">{tx.amount.toFixed(2)}</dd>
        <dt className="text-surface-fg">Billetera origen</dt>
        <dd className="text-canvas-fg">{tx.sourceWalletId ?? '—'}</dd>
        <dt className="text-surface-fg">Billetera destino</dt>
        <dd className="text-canvas-fg">{tx.targetWalletId ?? '—'}</dd>
        <dt className="text-surface-fg">Usuario origen</dt>
        <dd className="text-canvas-fg">{tx.sourceUserId}</dd>
        <dt className="text-surface-fg">Usuario destino</dt>
        <dd className="text-canvas-fg">{tx.targetUserId ?? '—'}</dd>
        <dt className="text-surface-fg">Puntos generados</dt>
        <dd className="text-canvas-fg">{tx.pointsGenerated}</dd>
        <dt className="text-surface-fg">Reversible</dt>
        <dd className="text-canvas-fg">{tx.reversible ? 'Sí' : 'No'}</dd>
        {tx.description && (
          <>
            <dt className="text-surface-fg">Descripción</dt>
            <dd className="text-canvas-fg">{tx.description}</dd>
          </>
        )}
        <dt className="text-surface-fg">Fecha</dt>
        <dd className="text-canvas-fg">{new Date(tx.timestamp).toLocaleString()}</dd>
      </dl>
    </div>
  );
}
