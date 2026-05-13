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
import { Card } from '../../shared/components/Card';
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
      <div className="max-w-[1200px] mx-auto px-6 sm:px-8 lg:px-12 py-[88px]">
        <h1
          className="text-4xl sm:text-5xl lg:text-[48px] font-medium leading-none tracking-tight text-ink mb-4"
          style={{ fontFamily: "'Inter Tight', 'Inter', system-ui, sans-serif" }}
        >
          Operaciones
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
    <div className="max-w-[1200px] mx-auto px-6 sm:px-8 lg:px-12 py-[88px]">
      {/* Hero */}
      <div className="mb-12">
        <h1
          className="text-4xl sm:text-5xl lg:text-[48px] font-medium leading-none tracking-tight text-ink mb-3"
          style={{ fontFamily: "'Inter Tight', 'Inter', system-ui, sans-serif" }}
        >
          Operaciones
        </h1>
        <p className="text-base text-charcoal">
          Usuario: <span className="text-ink font-semibold">{selectedUserId}</span>
        </p>
      </div>

      <div className="max-w-lg flex flex-col gap-6">
        {/* Tab navigation */}
        <div className="flex gap-2 flex-wrap">
          {(Object.keys(TAB_LABELS) as Tab[]).map((tab) => (
            <button
              key={tab}
              onClick={() => setActiveTab(tab)}
              className={`inline-flex items-center justify-center rounded-full text-sm font-semibold tracking-wide px-4 py-2 h-9 transition-colors ${
                activeTab === tab
                  ? 'bg-canvas-dark text-on-dark'
                  : 'bg-surface-soft text-ink hover:opacity-90'
              }`}
            >
              {TAB_LABELS[tab]}
            </button>
          ))}
        </div>

        {/* Tab content */}
        <Card variant="light">
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
        </Card>
      </div>
    </div>
  );
}
