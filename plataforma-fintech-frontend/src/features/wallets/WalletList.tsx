import { Card } from '../../shared/components/Card';
import { Button } from '../../shared/components/Button';
import type { WalletResponse } from '../../api/wallets';

interface WalletListProps {
  wallets: WalletResponse[];
  onSelect?: (code: string) => void;
}

export function WalletList({ wallets, onSelect }: WalletListProps) {
  if (wallets.length === 0) {
    return <p className="text-stone text-sm">No hay billeteras aún.</p>;
  }

  return (
    <div className="flex flex-col gap-4">
      {wallets.map((wallet) => (
        <Card key={wallet.code} variant="light" className="flex flex-col gap-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              <span className="text-ink font-semibold">{wallet.name}</span>
              <span className="inline-flex items-center px-3 py-0.5 rounded-full text-xs font-semibold bg-surface-soft text-charcoal">
                {wallet.type}
              </span>
            </div>
            {onSelect && (
              <Button variant="pill-sm" onClick={() => onSelect(wallet.code)}>
                Seleccionar
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
      ))}
    </div>
  );
}
