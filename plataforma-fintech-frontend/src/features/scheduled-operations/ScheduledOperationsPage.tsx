import { useState } from 'react';
import { useForm, type SubmitHandler } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { CalendarClock } from 'lucide-react';
import { useSelectionStore } from '../../stores/use-selection-store';
import {
  useScheduledOperationsQuery,
  useCreateScheduledOperationMutation,
  useCancelScheduledOperationMutation,
  useRunScheduledOpsMutation,
} from './hooks';
import {
  createScheduledOperationSchema,
  SCHEDULED_OPERATION_TYPES,
  type CreateScheduledOperationFormData,
} from './schemas';
import { labelOperationType, labelOperationStatus } from '../../shared/i18n/enum-labels';
import { Card } from '../../shared/components/Card';
import { Button } from '../../shared/components/Button';
import { Modal } from '../../shared/components/Modal';
import { EmptyState } from '../../shared/components/EmptyState';
import { Skeleton } from '../../shared/components/Skeleton';
import { pushToast } from '../../shared/components/Toast';
import type {
  ScheduledOperationResponse,
  CreateScheduledOperationRequest,
} from '../../api/scheduled-operations';

function stripUndefined<T extends Record<string, unknown>>(obj: T): Partial<T> {
  return Object.fromEntries(
    Object.entries(obj).filter(([, v]) => v !== undefined)
  ) as Partial<T>;
}

export function ScheduledOperationsPage() {
  const selectedUserId = useSelectionStore((s) => s.selectedUserId);
  const selectedWalletId = useSelectionStore((s) => s.selectedWalletId);
  const [cancelTarget, setCancelTarget] = useState<string | null>(null);

  const { data: operations = [], isLoading } = useScheduledOperationsQuery();
  const createMutation = useCreateScheduledOperationMutation();
  const cancelMutation = useCancelScheduledOperationMutation();
  const runMutation = useRunScheduledOpsMutation();

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<CreateScheduledOperationFormData>({
    resolver: zodResolver(createScheduledOperationSchema),
    defaultValues: {
      type: 'RECHARGE',
      sourceUserId: selectedUserId ?? '',
      sourceWalletId: selectedWalletId ?? '',
      amount: 0,
      scheduledAt: '',
    },
  });

  const onSubmit: SubmitHandler<CreateScheduledOperationFormData> = (data) => {
    const payload = stripUndefined({
      ...data,
      scheduledAt: new Date(data.scheduledAt).toISOString(),
    }) as CreateScheduledOperationRequest;
    createMutation.mutate(payload, {
      onSuccess: () => {
        reset();
        pushToast({ variant: 'success', message: 'Operación programada creada.' });
      },
      onError: () => {
        pushToast({ variant: 'error', message: 'No se pudo crear la operación.' });
      },
    });
  };

  function handleCancelConfirm() {
    if (!cancelTarget) return;
    cancelMutation.mutate(cancelTarget, {
      onSuccess: () => {
        setCancelTarget(null);
        pushToast({ variant: 'success', message: 'Operación cancelada.' });
      },
      onError: () => {
        pushToast({ variant: 'error', message: 'No se pudo cancelar la operación.' });
      },
    });
  }

  const inputCls =
    'w-full bg-canvas-light text-ink border border-hairline-light rounded-[12px] px-4 h-14 text-base focus:outline-none focus:border-brand';
  const labelCls = 'text-charcoal text-sm font-semibold tracking-wide';
  const errorCls = 'text-accent-danger text-xs';

  return (
    <div className="max-w-[1200px] mx-auto px-6 sm:px-8 lg:px-12 py-[88px]">
      {/* Hero */}
      <div className="mb-12 flex items-start justify-between flex-wrap gap-6">
        <div>
          <h1 className="text-display-lg text-ink mb-3">
            Operaciones programadas
          </h1>
          <p className="text-base text-charcoal">
            Programa, supervisa y ejecuta operaciones diferidas.
          </p>
        </div>

        <div className="flex flex-col gap-1 items-end">
          <Button
            variant="outline-light"
            onClick={() => runMutation.mutate()}
            disabled={runMutation.isPending}
            className="h-11 text-sm px-5"
          >
            {runMutation.isPending ? 'Ejecutando...' : 'Ejecutar vencidas'}
          </Button>
          {runMutation.isSuccess && runMutation.data && (
            <p className="text-stone text-xs">
              Ejecutadas: {runMutation.data.executed} | Fallidas: {runMutation.data.failed}
            </p>
          )}
          {runMutation.isError && (
            <p className="text-accent-danger text-xs">Error al ejecutar</p>
          )}
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-[minmax(0,360px)_minmax(0,1fr)] gap-8 items-start">
        {/* Create form */}
        <aside className="lg:sticky lg:top-24">
          <Card variant="light">
            <h2 className="text-heading-sm text-ink mb-6">
              Nueva operación programada
            </h2>
            <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-5">
              <div className="flex flex-col gap-1.5">
                <label htmlFor="type" className={labelCls}>Tipo</label>
                <select
                  id="type"
                  aria-label="Tipo"
                  {...register('type')}
                  className={inputCls}
                >
                  {SCHEDULED_OPERATION_TYPES.map((t) => (
                    <option key={t} value={t}>{labelOperationType(t)}</option>
                  ))}
                </select>
                {errors.type && <span className={errorCls}>{errors.type.message}</span>}
              </div>

              <div className="flex flex-col gap-1.5">
                <label htmlFor="sourceUserId" className={labelCls}>Usuario origen</label>
                <input id="sourceUserId" {...register('sourceUserId')} className={inputCls} />
                {errors.sourceUserId && <span className={errorCls}>{errors.sourceUserId.message}</span>}
              </div>

              <div className="flex flex-col gap-1.5">
                <label htmlFor="sourceWalletId" className={labelCls}>Billetera origen</label>
                <input id="sourceWalletId" {...register('sourceWalletId')} className={inputCls} />
                {errors.sourceWalletId && <span className={errorCls}>{errors.sourceWalletId.message}</span>}
              </div>

              <div className="flex flex-col gap-1.5">
                <label htmlFor="amount" className={labelCls}>Monto</label>
                <input
                  id="amount"
                  type="number"
                  step="0.01"
                  {...register('amount', { valueAsNumber: true })}
                  className={inputCls}
                />
                {errors.amount && <span className={errorCls}>{errors.amount.message}</span>}
              </div>

              <div className="flex flex-col gap-1.5">
                <label htmlFor="scheduledAt" className={labelCls}>Fecha programada</label>
                <input
                  id="scheduledAt"
                  type="datetime-local"
                  {...register('scheduledAt')}
                  className={inputCls}
                />
                {errors.scheduledAt && <span className={errorCls}>{errors.scheduledAt.message}</span>}
              </div>

              <Button variant="dark" type="submit" disabled={createMutation.isPending} className="self-start">
                {createMutation.isPending ? 'Creando...' : 'Crear operación'}
              </Button>
            </form>
          </Card>
        </aside>

        {/* Operations table */}
        <section className="min-w-0">
        <Modal
          open={cancelTarget !== null}
          onClose={() => setCancelTarget(null)}
          title="Cancelar operación"
          description="¿Estás seguro que querés cancelar esta operación programada?"
          confirmLabel="Sí, cancelar"
          tone="danger"
          onConfirm={handleCancelConfirm}
          isPending={cancelMutation.isPending}
        />

        <div className="overflow-x-auto">
          {isLoading ? (
            <div className="flex flex-col gap-3">
              {Array.from({ length: 4 }).map((_, i) => (
                <Skeleton key={i} className="h-12 w-full rounded-[12px]" />
              ))}
            </div>
          ) : operations.length === 0 ? (
            <EmptyState
              icon={CalendarClock}
              title="Sin operaciones programadas"
              description="No hay operaciones pendientes ni ejecutadas aún."
            />
          ) : (
            <div className="rounded-[20px] border border-hairline-light overflow-hidden">
              <table className="w-full text-sm text-ink">
                <thead className="bg-surface-soft">
                  <tr className="border-b border-hairline-light">
                    <th className="text-left py-3 px-4 text-stone font-semibold tracking-wide">ID</th>
                    <th className="text-left py-3 px-4 text-stone font-semibold tracking-wide">Tipo</th>
                    <th className="text-left py-3 px-4 text-stone font-semibold tracking-wide">Estado</th>
                    <th className="text-left py-3 px-4 text-stone font-semibold tracking-wide">Monto</th>
                    <th className="text-left py-3 px-4 text-stone font-semibold tracking-wide">Programada</th>
                    <th className="text-left py-3 px-4 text-stone font-semibold tracking-wide">Acción</th>
                  </tr>
                </thead>
                <tbody>
                  {operations.map((op: ScheduledOperationResponse) => (
                    <tr key={op.id} className="border-b border-hairline-light">
                      <td className="py-3 px-4 font-mono text-xs text-stone">{op.id}</td>
                      <td className="py-3 px-4 text-charcoal">{labelOperationType(op.type)}</td>
                      <td className="py-3 px-4 text-charcoal">{labelOperationStatus(op.status)}</td>
                      <td className="py-3 px-4 font-semibold">{op.amount.toFixed(2)}</td>
                      <td className="py-3 px-4 text-xs text-stone">
                        {new Date(op.scheduledAt).toLocaleString()}
                      </td>
                      <td className="py-3 px-4">
                        <button
                          onClick={() => setCancelTarget(op.id)}
                          disabled={op.status !== 'PENDING' || cancelMutation.isPending}
                          className="inline-flex items-center justify-center rounded-full text-button-sm px-3 py-1.5 bg-canvas-light border border-hairline-strong text-ink hover:opacity-70 disabled:opacity-30 disabled:cursor-not-allowed focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand"
                        >
                          Cancelar
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
        </section>
      </div>
    </div>
  );
}
