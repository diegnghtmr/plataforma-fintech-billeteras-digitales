import { Link } from 'react-router-dom';
import { useUserWalletsQuery, useCreateWalletMutation } from './hooks';
import { useSelectionStore } from '../../stores/use-selection-store';
import { WalletForm } from './WalletForm';
import { WalletList } from './WalletList';
import { Card } from '../../shared/components/Card';
import type { CreateWalletFormData } from './schemas';

export function WalletsPage() {
  const selectedUserId = useSelectionStore((s) => s.selectedUserId);
  const setSelectedWalletId = useSelectionStore((s) => s.setSelectedWalletId);

  const { data: wallets = [] } = useUserWalletsQuery(selectedUserId ?? undefined);
  const mutation = useCreateWalletMutation();

  if (!selectedUserId) {
    return (
      <div className="max-w-[1200px] mx-auto px-6 sm:px-8 lg:px-12 py-[88px]">
        <h1
          className="text-4xl sm:text-5xl lg:text-[48px] font-medium leading-none tracking-tight text-ink mb-4"
          style={{ fontFamily: "'Inter Tight', 'Inter', system-ui, sans-serif" }}
        >
          Billeteras
        </h1>
        <p className="text-base text-charcoal">
          Primero selecciona un usuario en la página de{' '}
          <Link to="/users" className="text-accent-blue-link underline">
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
    <div className="max-w-[1200px] mx-auto px-6 sm:px-8 lg:px-12 py-[88px]">
      {/* Hero */}
      <div className="mb-12">
        <h1
          className="text-4xl sm:text-5xl lg:text-[48px] font-medium leading-none tracking-tight text-ink mb-3"
          style={{ fontFamily: "'Inter Tight', 'Inter', system-ui, sans-serif" }}
        >
          Billeteras
        </h1>
        <p className="text-base text-charcoal">
          Usuario: <span className="text-ink font-semibold">{selectedUserId}</span>
        </p>
      </div>

      <div className="flex flex-col gap-8 max-w-2xl">
        <WalletList wallets={wallets} onSelect={(code) => setSelectedWalletId(code)} />

        <div className="border-t border-hairline-light pt-8">
          <Card variant="light">
            <h2
              className="text-xl font-medium text-ink mb-6"
              style={{ fontFamily: "'Inter Tight', 'Inter', system-ui, sans-serif" }}
            >
              Crear billetera
            </h2>
            <WalletForm onSubmit={handleSubmit} isPending={mutation.isPending} />
          </Card>
        </div>

        {mutation.isError && mutation.error && (
          <p className="text-accent-danger text-sm">
            {(mutation.error as { message?: string })?.message ?? 'Error desconocido'}
          </p>
        )}
      </div>
    </div>
  );
}
