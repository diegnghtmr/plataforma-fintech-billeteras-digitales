import { Card } from '../../shared/components/Card';
import { Button } from '../../shared/components/Button';
import type { WalletResponse } from '../../api/wallets';

interface WalletListProps {
  wallets: WalletResponse[];
  onSelect?: (code: string) => void;
}

export function WalletList({ wallets, onSelect }: WalletListProps) {
  if (wallets.length === 0) {
    return <p className="text-surface-fg/60 text-sm">No hay billeteras aún.</p>;
  }

  return (
    <div className="flex flex-col gap-3">
      {wallets.map((wallet) => (
        <Card key={wallet.code} className="flex flex-col gap-2">
          <div className="flex items-center justify-between">
            <span className="text-surface-fg font-semibold">{wallet.name}</span>
            {onSelect && (
              <Button variant="ghost" onClick={() => onSelect(wallet.code)}>
                Seleccionar
              </Button>
            )}
          </div>
          <dl className="grid grid-cols-2 gap-1 text-sm text-surface-fg/80">
            <dt className="font-medium">Código</dt>
            <dd>{wallet.code}</dd>
            <dt className="font-medium">Tipo</dt>
            <dd>{wallet.type}</dd>
            <dt className="font-medium">Balance</dt>
            <dd className="text-accent">{wallet.balance.toFixed(2)}</dd>
            <dt className="font-medium">Activa</dt>
            <dd>{wallet.active ? 'Sí' : 'No'}</dd>
            <dt className="font-medium">Transacciones</dt>
            <dd>{wallet.transactionCount}</dd>
          </dl>
        </Card>
      ))}
    </div>
  );
}
