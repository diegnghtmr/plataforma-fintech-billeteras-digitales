import { useState } from 'react';
import { Check, Info, Pencil, Lock, LockOpen, X, Save } from 'lucide-react';
import { Card } from '../../shared/components/Card';
import { Button } from '../../shared/components/Button';
import { Input } from '../../shared/components/Input';
import { useSelectionStore } from '../../stores/use-selection-store';
import type { WalletResponse } from '../../api/wallets';

interface WalletListProps {
  wallets: WalletResponse[];
  onSelect?: (code: string) => void;
  onRename?: (code: string, name: string) => void;
  onToggleActive?: (wallet: WalletResponse) => void;
  isUpdating?: boolean;
}

export function WalletList({ wallets, onSelect, onRename, onToggleActive, isUpdating }: WalletListProps) {
  const selectedWalletId = useSelectionStore((s) => s.selectedWalletId);
  const [editingCode, setEditingCode] = useState<string | null>(null);
  const [draftName, setDraftName] = useState('');

  function startEdit(wallet: WalletResponse) {
    setEditingCode(wallet.code);
    setDraftName(wallet.name);
  }
  function cancelEdit() {
    setEditingCode(null);
    setDraftName('');
  }
  function commitEdit(code: string) {
    const trimmed = draftName.trim();
    if (!trimmed) return;
    onRename?.(code, trimmed);
    setEditingCode(null);
    setDraftName('');
  }

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
            en Operaciones (Recarga, Retiro y Transferencia interna). Podés{' '}
            <strong>renombrar</strong> o <strong>cerrar/reabrir</strong> cualquier billetera con
            los botones de cada tarjeta.
          </span>
        </p>
      )}
      {wallets.map((wallet) => {
        const isSelected = selectedWalletId === wallet.code;
        const isEditing = editingCode === wallet.code;
        const canClose = wallet.active && wallet.balance === 0;
        return (
          <Card
            key={wallet.code}
            variant="light"
            className={`flex flex-col gap-4 transition-shadow ${
              !wallet.active ? 'opacity-70' : ''
            } ${
              isSelected ? 'ring-2 ring-[#3a40c4] ring-offset-2 ring-offset-canvas-light' : ''
            }`}
          >
            <div className="flex items-center justify-between gap-3 flex-wrap">
              {isEditing ? (
                <div className="flex flex-col gap-1 flex-1 min-w-[18rem]">
                  <label
                    htmlFor={`wallet-name-${wallet.code}`}
                    className="text-xs font-medium text-stone"
                  >
                    Renombrar billetera {wallet.code}
                  </label>
                  <Input
                    id={`wallet-name-${wallet.code}`}
                    value={draftName}
                    onChange={(e) => setDraftName(e.target.value)}
                    onKeyDown={(e) => {
                      if (e.key === 'Enter') commitEdit(wallet.code);
                      if (e.key === 'Escape') cancelEdit();
                    }}
                    autoFocus
                    maxLength={80}
                    placeholder="Nuevo nombre"
                    aria-label="Nuevo nombre de billetera"
                  />
                </div>
              ) : (
                <div className="flex items-center gap-3 min-w-0 flex-1">
                  <span className="text-ink font-semibold truncate">{wallet.name}</span>
                  <span className="inline-flex items-center px-3 py-0.5 rounded-full text-xs font-semibold bg-surface-soft text-charcoal shrink-0">
                    {wallet.type}
                  </span>
                  {!wallet.active && (
                    <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-semibold bg-surface-soft text-stone shrink-0">
                      Cerrada
                    </span>
                  )}
                  {isSelected && (
                    <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-semibold bg-[#3a40c4] text-white shrink-0">
                      <Check size={10} strokeWidth={3} /> Seleccionada
                    </span>
                  )}
                </div>
              )}
              <div className="flex items-center gap-2 shrink-0">
                {isEditing ? (
                  <>
                    <Button
                      variant="pill-sm"
                      onClick={() => commitEdit(wallet.code)}
                      disabled={isUpdating || !draftName.trim()}
                      aria-label="Guardar nombre"
                    >
                      <Save size={14} strokeWidth={2} className="mr-1" /> Guardar
                    </Button>
                    <Button
                      variant="pill-sm"
                      onClick={cancelEdit}
                      disabled={isUpdating}
                      aria-label="Cancelar edición"
                    >
                      <X size={14} strokeWidth={2} />
                    </Button>
                  </>
                ) : (
                  <>
                    {onRename && (
                      <Button
                        variant="pill-sm"
                        onClick={() => startEdit(wallet)}
                        disabled={isUpdating}
                        aria-label={`Renombrar ${wallet.name}`}
                      >
                        <Pencil size={14} strokeWidth={2} className="mr-1" /> Renombrar
                      </Button>
                    )}
                    {onToggleActive && (
                      <Button
                        variant="pill-sm"
                        onClick={() => onToggleActive(wallet)}
                        disabled={isUpdating || (wallet.active && !canClose)}
                        title={
                          wallet.active && !canClose
                            ? 'No se puede cerrar: la billetera tiene saldo > 0'
                            : undefined
                        }
                        aria-label={wallet.active ? `Cerrar ${wallet.name}` : `Reabrir ${wallet.name}`}
                      >
                        {wallet.active ? (
                          <>
                            <Lock size={14} strokeWidth={2} className="mr-1" /> Cerrar
                          </>
                        ) : (
                          <>
                            <LockOpen size={14} strokeWidth={2} className="mr-1" /> Reabrir
                          </>
                        )}
                      </Button>
                    )}
                    {onSelect && (
                      <Button
                        variant="pill-sm"
                        onClick={() => onSelect(wallet.code)}
                        disabled={isSelected || !wallet.active}
                        aria-pressed={isSelected}
                      >
                        {isSelected ? 'Activa' : 'Seleccionar'}
                      </Button>
                    )}
                  </>
                )}
              </div>
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
