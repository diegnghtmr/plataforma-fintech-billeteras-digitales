import { useUserTransactionsQuery, useWalletTransactionsQuery, useReverseTransactionMutation } from './hooks';
import { TransactionFilters } from './TransactionFilters';
import { TransactionRow } from './TransactionRow';
import { useSelectionStore } from '../../stores/use-selection-store';
import { useAppStore } from '../../stores/use-app-store';
import type { TransactionType as StoreTransactionType, TransactionStatus as StoreTransactionStatus } from '../../stores/use-app-store';

export function TransactionsPage() {
  const selectedUserId = useSelectionStore((s) => s.selectedUserId);

  const transactionFilters = useAppStore((s) => s.transactionFilters);
  const setTransactionFilters = useAppStore((s) => s.setTransactionFilters);

  const { type: typeFilter, status: statusFilter, walletId: walletIdFilter } = transactionFilters;

  const queryFilters = {
    ...(typeFilter !== undefined ? { type: typeFilter } : {}),
    ...(statusFilter !== undefined ? { status: statusFilter } : {}),
  };

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
      <div className="max-w-[1200px] mx-auto px-6 sm:px-8 lg:px-12 py-[88px]">
        <p className="text-charcoal text-sm">
          Selecciona un usuario desde la sección de{' '}
          <a href="/users" className="text-accent-blue-link underline">
            Usuarios
          </a>{' '}
          para ver su historial de transacciones.
        </p>
      </div>
    );
  }

  return (
    <div className="max-w-[1200px] mx-auto px-6 sm:px-8 lg:px-12 py-[88px]">
      {/* Hero */}
      <div className="mb-12">
        <h1
          className="text-4xl sm:text-5xl lg:text-[48px] font-medium leading-none tracking-tight text-ink mb-3"
          style={{ fontFamily: "'Inter Tight', 'Inter', system-ui, sans-serif" }}
        >
          Historial y reversión
        </h1>
        <p className="text-base text-charcoal">
          Usuario: <span className="text-ink font-semibold">{selectedUserId}</span>
        </p>
      </div>

      <TransactionFilters
        type={typeFilter}
        status={statusFilter}
        onTypeChange={handleTypeChange}
        onStatusChange={handleStatusChange}
      />

      {isLoading && (
        <p className="text-stone text-sm">Cargando transacciones...</p>
      )}

      {!isLoading && (!transactions || transactions.length === 0) && (
        <p className="text-stone text-sm">No hay transacciones que mostrar.</p>
      )}

      {transactions && transactions.length > 0 && (
        <div className="overflow-x-auto rounded-[20px] border border-hairline-light">
          <table className="w-full">
            <thead className="bg-surface-soft text-sm text-stone">
              <tr>
                <th className="py-3 px-3 text-left font-semibold tracking-wide">ID</th>
                <th className="py-3 px-3 text-left font-semibold tracking-wide">Fecha</th>
                <th className="py-3 px-3 text-left font-semibold tracking-wide">Tipo</th>
                <th className="py-3 px-3 text-left font-semibold tracking-wide">Estado</th>
                <th className="py-3 px-3 text-right font-semibold tracking-wide">Monto</th>
                <th className="py-3 px-3 text-left font-semibold tracking-wide">Origen</th>
                <th className="py-3 px-3 text-left font-semibold tracking-wide">Destino</th>
                <th className="py-3 px-3 text-center font-semibold tracking-wide">Reversible</th>
                <th className="py-3 px-3 font-semibold tracking-wide">Acciones</th>
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
        <p className="text-accent-danger text-sm">
          Error al revertir:{' '}
          {(reverseMutation.error as { message?: string })?.message ?? 'Error desconocido'}
        </p>
      )}
    </div>
  );
}
