import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useSelectionStore } from '../../stores/use-selection-store';
import {
  useRechargeMutation,
  useWithdrawMutation,
  useInternalTransferMutation,
  useExternalTransferMutation,
} from './hooks';
import { RechargeForm } from './RechargeForm';
import { WithdrawForm } from './WithdrawForm';
import { InternalTransferForm } from './InternalTransferForm';
import { ExternalTransferForm } from './ExternalTransferForm';
import type { ApiError } from '../../api/error';
import type { MoneyOperationFormData } from './schemas';
import type { InternalTransferFormData } from './schemas';
import type { ExternalTransferFormData } from './schemas';

function asApiError(e: unknown): ApiError | null {
  return e !== null && typeof e === 'object' && 'code' in e ? (e as ApiError) : null;
}

type Tab = 'recharge' | 'withdraw' | 'internal' | 'external';

const TAB_LABELS: Record<Tab, string> = {
  recharge: 'Recarga',
  withdraw: 'Retiro',
  internal: 'Transferencia interna',
  external: 'Transferencia externa',
};

export function OperationsPage() {
  const selectedUserId = useSelectionStore((s) => s.selectedUserId);
  const selectedWalletId = useSelectionStore((s) => s.selectedWalletId);
  const [activeTab, setActiveTab] = useState<Tab>('recharge');

  const rechargeMutation = useRechargeMutation();
  const withdrawMutation = useWithdrawMutation();
  const internalMutation = useInternalTransferMutation();
  const externalMutation = useExternalTransferMutation();

  if (!selectedUserId) {
    return (
      <div className="flex flex-col gap-4 text-canvas-fg">
        <h2 className="text-xl font-bold">Operaciones</h2>
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

  function handleRecharge(data: MoneyOperationFormData & { userId: string; walletId: string }) {
    const { description, ...rest } = data;
    rechargeMutation.mutate({ ...rest, ...(description !== undefined ? { description } : {}) });
  }

  function handleWithdraw(data: MoneyOperationFormData & { userId: string; walletId: string }) {
    const { description, ...rest } = data;
    withdrawMutation.mutate({ ...rest, ...(description !== undefined ? { description } : {}) });
  }

  function handleInternal(data: InternalTransferFormData & { userId: string }) {
    const { description, ...rest } = data;
    internalMutation.mutate({ ...rest, ...(description !== undefined ? { description } : {}) });
  }

  function handleExternal(data: ExternalTransferFormData) {
    const { description, ...rest } = data;
    externalMutation.mutate({ ...rest, ...(description !== undefined ? { description } : {}) });
  }

  return (
    <div className="flex flex-col gap-6 max-w-lg">
      <h2 className="text-canvas-fg text-xl font-bold">
        Operaciones — <span className="text-accent">{selectedUserId}</span>
      </h2>

      {/* Tab navigation */}
      <div className="flex gap-2 border-b border-surface-fg/20 pb-2">
        {(Object.keys(TAB_LABELS) as Tab[]).map((tab) => (
          <button
            key={tab}
            onClick={() => setActiveTab(tab)}
            className={`px-3 py-1 rounded-t text-sm font-medium transition-colors ${
              activeTab === tab
                ? 'bg-accent text-accent-fg'
                : 'text-surface-fg hover:bg-surface-fg/10'
            }`}
          >
            {TAB_LABELS[tab]}
          </button>
        ))}
      </div>

      {/* Tab content */}
      {activeTab === 'recharge' && (
        <RechargeForm
          userId={selectedUserId}
          walletId={selectedWalletId ?? ''}
          onSubmit={handleRecharge}
          isPending={rechargeMutation.isPending}
          error={rechargeMutation.isError ? asApiError(rechargeMutation.error) : null}
        />
      )}

      {activeTab === 'withdraw' && (
        <WithdrawForm
          userId={selectedUserId}
          walletId={selectedWalletId ?? ''}
          onSubmit={handleWithdraw}
          isPending={withdrawMutation.isPending}
          error={withdrawMutation.isError ? asApiError(withdrawMutation.error) : null}
        />
      )}

      {activeTab === 'internal' && (
        <InternalTransferForm
          userId={selectedUserId}
          defaultSourceWalletId={selectedWalletId ?? ''}
          onSubmit={handleInternal}
          isPending={internalMutation.isPending}
          error={internalMutation.isError ? asApiError(internalMutation.error) : null}
        />
      )}

      {activeTab === 'external' && (
        <ExternalTransferForm
          defaultSourceUserId={selectedUserId}
          defaultSourceWalletId={selectedWalletId ?? ''}
          onSubmit={handleExternal}
          isPending={externalMutation.isPending}
          error={externalMutation.isError ? asApiError(externalMutation.error) : null}
        />
      )}
    </div>
  );
}
