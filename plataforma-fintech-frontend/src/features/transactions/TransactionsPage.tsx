import { useUserTransactionsQuery, useWalletTransactionsQuery, useReverseTransactionMutation } from './hooks';
import { TransactionFilters } from './TransactionFilters';
import { TransactionRow } from './TransactionRow';
import { useSelectionStore } from '../../stores/use-selection-store';
import { useAppStore } from '../../stores/use-app-store';
import type { TransactionType as StoreTransactionType, TransactionStatus as StoreTransactionStatus } from '../../stores/use-app-store';

export function TransactionsPage() {
  const selectedUserId = useSelectionStore((s) => s.selectedUserId);

  // W1: read filters from Zustand store instead of local useState
  const transactionFilters = useAppStore((s) => s.transactionFilters);
  const setTransactionFilters = useAppStore((s) => s.setTransactionFilters);

  const { type: typeFilter, status: statusFilter, walletId: walletIdFilter } = transactionFilters;

  const queryFilters = {
    ...(typeFilter !== undefined ? { type: typeFilter } : {}),
    ...(statusFilter !== undefined ? { status: statusFilter } : {}),
  };

  // W1+W2: when walletId filter is set, use wallet-scoped query; otherwise user-scoped
  const userQuery = useUserTransactionsQuery(
    !walletIdFilter ? (selectedUserId ?? '') : '',
    !walletIdFilter && Object.keys(queryFilters).length > 0 ? queryFilters : undefined
  );

  const walletQuery = useWalletTransactionsQuery(
    walletIdFilter ? (selectedUserId ?? '') : '',
    walletIdFilter ?? ''
  );

  const { data: transactions, isLoading } = walletIdFilter ? walletQuery : userQuery;

  const reverseMutation = useReverseTransactionMutation();

  function handleTypeChange(value: StoreTransactionType | undefined) {
    // W1: merge into store; omit the key entirely when clearing (exactOptionalPropertyTypes-safe)
    const next = { ...transactionFilters };
    if (value !== undefined) {
      next.type = value;
    } else {
      delete next.type;
    }
    setTransactionFilters(next);
  }

  function handleStatusChange(value: StoreTransactionStatus | undefined) {
    const next = { ...transactionFilters };
    if (value !== undefined) {
      next.status = value;
    } else {
      delete next.status;
    }
    setTransactionFilters(next);
  }

  if (!selectedUserId) {
    return (
      <div className="text-surface-fg text-sm">
        Selecciona un usuario desde la sección de{' '}
        <a href="/users" className="text-accent underline">
          Usuarios
        </a>{' '}
        para ver su historial de transacciones.
      </div>
    );
  }

  return (
    <div className="flex flex-col gap-4">
      <h2 className="text-xl font-bold text-canvas-fg">Historial de transacciones</h2>
      <p className="text-sm text-surface-fg">Usuario: {selectedUserId}</p>

      <TransactionFilters
        type={typeFilter}
        status={statusFilter}
        onTypeChange={handleTypeChange}
        onStatusChange={handleStatusChange}
      />

      {isLoading && (
        <p className="text-surface-fg text-sm">Cargando transacciones...</p>
      )}

      {!isLoading && (!transactions || transactions.length === 0) && (
        <p className="text-surface-fg text-sm">No hay transacciones que mostrar.</p>
      )}

      {transactions && transactions.length > 0 && (
        <div className="overflow-x-auto rounded-lg border border-surface-fg/10">
          <table className="w-full">
            <thead className="bg-surface text-sm text-surface-fg">
              <tr>
                <th className="py-2 px-3 text-left">ID</th>
                <th className="py-2 px-3 text-left">Fecha</th>
                <th className="py-2 px-3 text-left">Tipo</th>
                <th className="py-2 px-3 text-left">Estado</th>
                <th className="py-2 px-3 text-right">Monto</th>
                <th className="py-2 px-3 text-left">Origen</th>
                <th className="py-2 px-3 text-left">Destino</th>
                <th className="py-2 px-3 text-center">Reversible</th>
                <th className="py-2 px-3">Acciones</th>
              </tr>
            </thead>
            <tbody>
              {transactions.map((tx) => (
                <TransactionRow
                  key={tx.id}
                  tx={tx}
                  onReverse={(txId) =>
                    reverseMutation.mutate({
                      transactionId: txId,
                      userId: selectedUserId,
                      // W2: pass walletId so byWallet invalidation fires when scoped to a wallet
                      ...(walletIdFilter ? { walletId: walletIdFilter } : {}),
                    })
                  }
                  isReverting={reverseMutation.isPending}
                />
              ))}
            </tbody>
          </table>
        </div>
      )}

      {reverseMutation.isError && (
        <p className="text-red-500 text-sm">
          Error al revertir:{' '}
          {(reverseMutation.error as { message?: string })?.message ?? 'Error desconocido'}
        </p>
      )}
    </div>
  );
}
