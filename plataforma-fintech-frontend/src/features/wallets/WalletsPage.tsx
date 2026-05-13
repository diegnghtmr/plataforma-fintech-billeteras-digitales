import { Link } from 'react-router-dom';
import { useUserWalletsQuery, useCreateWalletMutation } from './hooks';
import { useSelectionStore } from '../../stores/use-selection-store';
import { WalletForm } from './WalletForm';
import { WalletList } from './WalletList';
import type { CreateWalletFormData } from './schemas';

export function WalletsPage() {
  const selectedUserId = useSelectionStore((s) => s.selectedUserId);
  const setSelectedWalletId = useSelectionStore((s) => s.setSelectedWalletId);

  const { data: wallets = [] } = useUserWalletsQuery(selectedUserId ?? undefined);
  const mutation = useCreateWalletMutation();

  if (!selectedUserId) {
    return (
      <div className="flex flex-col gap-4 text-canvas-fg">
        <h2 className="text-xl font-bold">Billeteras</h2>
        <p className="text-surface-fg/70">
          Primero selecciona un usuario en la página de{' '}
          <Link to="/users" className="text-accent underline">
            Usuarios
          </Link>
          .
        </p>
      </div>
    );
  }

  function handleSubmit(data: CreateWalletFormData) {
    mutation.mutate({ userId: selectedUserId!, ...data });
  }

  return (
    <div className="flex flex-col gap-6 max-w-lg">
      <h2 className="text-canvas-fg text-xl font-bold">
        Billeteras de <span className="text-accent">{selectedUserId}</span>
      </h2>

      <WalletList wallets={wallets} onSelect={(code) => setSelectedWalletId(code)} />

      <div className="border-t border-surface-fg/20 pt-4">
        <h3 className="text-canvas-fg font-semibold mb-3">Crear billetera</h3>
        <WalletForm onSubmit={handleSubmit} isPending={mutation.isPending} />
      </div>

      {mutation.isError && mutation.error && (
        <p className="text-danger text-sm">
          {(mutation.error as { message?: string })?.message ?? 'Error desconocido'}
        </p>
      )}
    </div>
  );
}
