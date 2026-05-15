import { useUserWalletsQuery, useCreateWalletMutation } from './hooks';
import { InlineLink } from '../../shared/components/InlineLink';
import { useSelectionStore } from '../../stores/use-selection-store';
import { WalletForm } from './WalletForm';
import { WalletList } from './WalletList';
import { Card } from '../../shared/components/Card';
import { pushToast } from '../../shared/components/Toast';
import type { CreateWalletFormData } from './schemas';

export function WalletsPage() {
  const selectedUserId = useSelectionStore((s) => s.selectedUserId);
  const setSelectedWalletId = useSelectionStore((s) => s.setSelectedWalletId);

  const { data: wallets = [] } = useUserWalletsQuery(selectedUserId ?? undefined);
  const mutation = useCreateWalletMutation();

  if (!selectedUserId) {
    return (
      <div className="max-w-[1200px] mx-auto px-6 sm:px-8 lg:px-12 py-[88px]">
        <h1 className="text-display-lg text-ink mb-4">
          Billeteras
        </h1>
        <p className="text-body-md text-charcoal">
          Primero seleccioná un usuario en la página de{' '}
          <InlineLink to="/users">Usuarios</InlineLink>
          .
        </p>
      </div>
    );
  }

  function handleSubmit(data: CreateWalletFormData) {
    mutation.mutate(
      { userId: selectedUserId!, ...data },
      {
        onSuccess: (result) => pushToast({ variant: 'success', message: `Billetera ${result.code} creada correctamente.` }),
        onError: () => pushToast({ variant: 'error', message: 'No se pudo crear la billetera.' }),
      }
    );
  }

  return (
    <div className="max-w-[1200px] mx-auto px-6 sm:px-8 lg:px-12 py-[88px]">
      {/* Hero */}
      <div className="mb-12">
        <h1 className="text-display-lg text-ink mb-3">
          Billeteras
        </h1>
        <p className="text-body-md text-charcoal">
          Usuario: <span className="text-ink font-semibold">{selectedUserId}</span>
        </p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-[minmax(0,360px)_minmax(0,1fr)] gap-8 items-start">
        <aside className="lg:sticky lg:top-24">
          <Card variant="light">
            <h2 className="text-heading-sm text-ink mb-6">
              Crear billetera
            </h2>
            <WalletForm onSubmit={handleSubmit} isPending={mutation.isPending} />
          </Card>
        </aside>

        <section className="min-w-0">
          <WalletList
            wallets={wallets}
            onSelect={(code) => {
              setSelectedWalletId(code);
              const wallet = wallets.find((w) => w.code === code);
              pushToast({
                variant: 'success',
                message: `${wallet?.name ?? code} quedó seleccionada como origen predeterminado en Operaciones.`,
              });
            }}
          />
        </section>
      </div>
    </div>
  );
}
