import { Check, Info } from 'lucide-react';
import { Card } from '../../shared/components/Card';
import { Button } from '../../shared/components/Button';
import { useSelectionStore } from '../../stores/use-selection-store';
import type { WalletResponse } from '../../api/wallets';

interface WalletListProps {
  wallets: WalletResponse[];
  onSelect?: (code: string) => void;
}

export function WalletList({ wallets, onSelect }: WalletListProps) {
  const selectedWalletId = useSelectionStore((s) => s.selectedWalletId);

  if (wallets.length === 0) {
    return <p className="text-stone text-sm">No hay billeteras aún.</p>;
  }

  return (
    <div className="flex flex-col gap-4">
      {onSelect && (
        <p className="flex items-start gap-2 rounded-[12px] bg-surface-soft px-4 py-3 text-xs text-charcoal">
          <Info size={14} className="mt-0.5 shrink-0 text-stone" strokeWidth={2} />
          <span>
            Al seleccionar una billetera, se usa como <strong>origen predeterminado</strong>{' '}
            en Operaciones (Recarga, Retiro y Transferencia interna).
          </span>
        </p>
      )}
      {wallets.map((wallet) => {
        const isSelected = selectedWalletId === wallet.code;
        return (
          <Card
            key={wallet.code}
            variant="light"
            className={`flex flex-col gap-4 transition-shadow ${
              isSelected ? 'ring-2 ring-[#3a40c4] ring-offset-2 ring-offset-canvas-light' : ''
            }`}
          >
            <div className="flex items-center justify-between gap-3">
              <div className="flex items-center gap-3 min-w-0">
                <span className="text-ink font-semibold truncate">{wallet.name}</span>
                <span className="inline-flex items-center px-3 py-0.5 rounded-full text-xs font-semibold bg-surface-soft text-charcoal shrink-0">
                  {wallet.type}
                </span>
                {isSelected && (
                  <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-semibold bg-[#3a40c4] text-white shrink-0">
                    <Check size={10} strokeWidth={3} /> Seleccionada
                  </span>
                )}
              </div>
              {onSelect && (
                <Button
                  variant="pill-sm"
                  onClick={() => onSelect(wallet.code)}
                  disabled={isSelected}
                  aria-pressed={isSelected}
                >
                  {isSelected ? 'Activa' : 'Seleccionar'}
                </Button>
              )}
            </div>
            <dl className="grid grid-cols-2 gap-x-6 gap-y-2 text-sm">
              <dt className="text-stone font-medium">Código</dt>
              <dd className="text-ink font-medium">{wallet.code}</dd>
              <dt className="text-stone font-medium">Balance</dt>
              <dd className="text-ink font-semibold">{wallet.balance.toFixed(2)}</dd>
              <dt className="text-stone font-medium">Activa</dt>
              <dd className="text-ink">{wallet.active ? 'Sí' : 'No'}</dd>
              <dt className="text-stone font-medium">Transacciones</dt>
              <dd className="text-ink">{wallet.transactionCount}</dd>
            </dl>
          </Card>
        );
      })}
    </div>
  );
}
