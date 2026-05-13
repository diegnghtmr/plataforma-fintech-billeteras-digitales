import type { TransactionType, TransactionStatus } from '../../api/transactions';

interface TransactionFiltersProps {
  type?: TransactionType | undefined;
  status?: TransactionStatus | undefined;
  onTypeChange: (value: TransactionType | undefined) => void;
  onStatusChange: (value: TransactionStatus | undefined) => void;
}

const TRANSACTION_TYPES: TransactionType[] = [
  'RECHARGE',
  'WITHDRAWAL',
  'INTERNAL_TRANSFER',
  'EXTERNAL_TRANSFER_SENT',
  'EXTERNAL_TRANSFER_RECEIVED',
];

const TRANSACTION_STATUSES: TransactionStatus[] = ['SUCCESSFUL', 'REVERSED'];

export function TransactionFilters({
  type,
  status,
  onTypeChange,
  onStatusChange,
}: TransactionFiltersProps) {
  return (
    <div className="flex gap-4 mb-4">
      <label className="flex flex-col gap-1 text-sm text-surface-fg">
        <span id="type-filter-label">Tipo</span>
        <select
          aria-label="Tipo"
          value={type ?? ''}
          onChange={(e) =>
            onTypeChange(e.target.value ? (e.target.value as TransactionType) : undefined)
          }
          className="border border-surface-fg/20 rounded px-2 py-1 bg-canvas text-canvas-fg text-sm"
        >
          <option value="">Todos</option>
          {TRANSACTION_TYPES.map((t) => (
            <option key={t} value={t}>
              {t}
            </option>
          ))}
        </select>
      </label>

      <label className="flex flex-col gap-1 text-sm text-surface-fg">
        <span>Estado</span>
        <select
          aria-label="Estado"
          value={status ?? ''}
          onChange={(e) =>
            onStatusChange(e.target.value ? (e.target.value as TransactionStatus) : undefined)
          }
          className="border border-surface-fg/20 rounded px-2 py-1 bg-canvas text-canvas-fg text-sm"
        >
          <option value="">Todos</option>
          {TRANSACTION_STATUSES.map((s) => (
            <option key={s} value={s}>
              {s}
            </option>
          ))}
        </select>
      </label>
    </div>
  );
}
