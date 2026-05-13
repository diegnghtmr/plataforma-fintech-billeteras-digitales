import { useForm, type SubmitHandler } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
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
import { Card } from '../../shared/components/Card';
import { Button } from '../../shared/components/Button';
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
    createMutation.mutate(payload, { onSuccess: () => reset() });
  };

  const inputCls =
    'w-full bg-canvas-light text-ink border border-hairline-light rounded-[12px] px-4 h-14 text-base focus:outline-none focus:border-brand';
  const labelCls = 'text-charcoal text-sm font-semibold tracking-wide';
  const errorCls = 'text-accent-danger text-xs';

  return (
    <div className="max-w-[1200px] mx-auto px-6 sm:px-8 lg:px-12 py-[88px]">
      {/* Hero */}
      <div className="mb-12 flex items-start justify-between flex-wrap gap-6">
        <div>
          <h1
            className="text-4xl sm:text-5xl lg:text-[48px] font-medium leading-none tracking-tight text-ink mb-3"
            style={{ fontFamily: "'Inter Tight', 'Inter', system-ui, sans-serif" }}
          >
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

      <div className="flex flex-col gap-8">
        {/* Create form */}
        <Card variant="light" className="max-w-lg">
          <h2
            className="text-xl font-medium text-ink mb-6"
            style={{ fontFamily: "'Inter Tight', 'Inter', system-ui, sans-serif" }}
          >
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
                  <option key={t} value={t}>{t}</option>
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

        {/* Operations table */}
        <div className="overflow-x-auto">
          {isLoading ? (
            <p className="text-stone text-sm">Cargando...</p>
          ) : operations.length === 0 ? (
            <p className="text-stone text-sm">No hay operaciones programadas.</p>
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
                      <td className="py-3 px-4 text-charcoal">{op.type}</td>
                      <td className="py-3 px-4 text-charcoal">{op.status}</td>
                      <td className="py-3 px-4 font-semibold">{op.amount.toFixed(2)}</td>
                      <td className="py-3 px-4 text-xs text-stone">
                        {new Date(op.scheduledAt).toLocaleString()}
                      </td>
                      <td className="py-3 px-4">
                        <button
                          onClick={() => cancelMutation.mutate(op.id)}
                          disabled={op.status !== 'PENDING' || cancelMutation.isPending}
                          className="inline-flex items-center justify-center rounded-full text-xs font-semibold tracking-wide px-3 py-1.5 bg-canvas-light border border-hairline-strong text-ink hover:opacity-70 disabled:opacity-30 disabled:cursor-not-allowed"
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
      </div>
    </div>
  );
}
