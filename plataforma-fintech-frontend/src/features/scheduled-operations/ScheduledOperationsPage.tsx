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

  return (
    <div className="flex flex-col gap-6">
      <div className="flex items-center justify-between">
        <h2 className="text-canvas-fg text-xl font-bold">Operaciones Programadas</h2>
        <div className="flex flex-col gap-1 items-end">
          <button
            onClick={() => runMutation.mutate()}
            disabled={runMutation.isPending}
            className="bg-accent text-accent-fg rounded px-4 py-2 text-sm font-medium hover:opacity-90 disabled:opacity-50"
          >
            {runMutation.isPending ? 'Ejecutando...' : 'Ejecutar vencidas'}
          </button>
          {runMutation.isSuccess && runMutation.data && (
            <p className="text-surface-fg/70 text-xs">
              Ejecutadas: {runMutation.data.executed} | Fallidas: {runMutation.data.failed}
            </p>
          )}
          {runMutation.isError && (
            <p className="text-danger text-xs">Error al ejecutar</p>
          )}
        </div>
      </div>

      {/* Create form */}
      <div className="bg-surface rounded-lg p-4 max-w-lg">
        <h3 className="text-surface-fg font-semibold mb-3">Nueva Operación Programada</h3>
        <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-3">
          <div className="flex flex-col gap-1">
            <label htmlFor="type" className="text-surface-fg text-sm">
              Tipo
            </label>
            <select
              id="type"
              aria-label="Tipo"
              {...register('type')}
              className="bg-canvas text-canvas-fg border border-surface-fg/20 rounded px-3 py-2 text-sm"
            >
              {SCHEDULED_OPERATION_TYPES.map((t) => (
                <option key={t} value={t}>
                  {t}
                </option>
              ))}
            </select>
            {errors.type && (
              <span className="text-danger text-xs">{errors.type.message}</span>
            )}
          </div>

          <div className="flex flex-col gap-1">
            <label htmlFor="sourceUserId" className="text-surface-fg text-sm">
              Usuario origen
            </label>
            <input
              id="sourceUserId"
              {...register('sourceUserId')}
              className="bg-canvas text-canvas-fg border border-surface-fg/20 rounded px-3 py-2 text-sm"
            />
            {errors.sourceUserId && (
              <span className="text-danger text-xs">{errors.sourceUserId.message}</span>
            )}
          </div>

          <div className="flex flex-col gap-1">
            <label htmlFor="sourceWalletId" className="text-surface-fg text-sm">
              Billetera origen
            </label>
            <input
              id="sourceWalletId"
              {...register('sourceWalletId')}
              className="bg-canvas text-canvas-fg border border-surface-fg/20 rounded px-3 py-2 text-sm"
            />
            {errors.sourceWalletId && (
              <span className="text-danger text-xs">{errors.sourceWalletId.message}</span>
            )}
          </div>

          <div className="flex flex-col gap-1">
            <label htmlFor="amount" className="text-surface-fg text-sm">
              Monto
            </label>
            <input
              id="amount"
              type="number"
              step="0.01"
              {...register('amount', { valueAsNumber: true })}
              className="bg-canvas text-canvas-fg border border-surface-fg/20 rounded px-3 py-2 text-sm"
            />
            {errors.amount && (
              <span className="text-danger text-xs">{errors.amount.message}</span>
            )}
          </div>

          <div className="flex flex-col gap-1">
            <label htmlFor="scheduledAt" className="text-surface-fg text-sm">
              Fecha programada
            </label>
            <input
              id="scheduledAt"
              type="datetime-local"
              {...register('scheduledAt')}
              className="bg-canvas text-canvas-fg border border-surface-fg/20 rounded px-3 py-2 text-sm"
            />
            {errors.scheduledAt && (
              <span className="text-danger text-xs">{errors.scheduledAt.message}</span>
            )}
          </div>

          <button
            type="submit"
            disabled={createMutation.isPending}
            className="bg-accent text-accent-fg rounded px-4 py-2 text-sm font-medium hover:opacity-90 disabled:opacity-50"
          >
            {createMutation.isPending ? 'Creando...' : 'Crear operación'}
          </button>
        </form>
      </div>

      {/* Operations table */}
      <div className="overflow-x-auto">
        {isLoading ? (
          <p className="text-surface-fg/70 text-sm">Cargando...</p>
        ) : operations.length === 0 ? (
          <p className="text-surface-fg/70 text-sm">No hay operaciones programadas.</p>
        ) : (
          <table className="w-full text-sm text-surface-fg">
            <thead>
              <tr className="border-b border-surface-fg/10">
                <th className="text-left py-2 pr-4">ID</th>
                <th className="text-left py-2 pr-4">Tipo</th>
                <th className="text-left py-2 pr-4">Estado</th>
                <th className="text-left py-2 pr-4">Monto</th>
                <th className="text-left py-2 pr-4">Programada</th>
                <th className="text-left py-2">Acción</th>
              </tr>
            </thead>
            <tbody>
              {operations.map((op: ScheduledOperationResponse) => (
                <tr key={op.id} className="border-b border-surface-fg/5">
                  <td className="py-2 pr-4 font-mono text-xs">{op.id}</td>
                  <td className="py-2 pr-4">{op.type}</td>
                  <td className="py-2 pr-4">{op.status}</td>
                  <td className="py-2 pr-4">{op.amount.toFixed(2)}</td>
                  <td className="py-2 pr-4 text-xs">
                    {new Date(op.scheduledAt).toLocaleString()}
                  </td>
                  <td className="py-2">
                    <button
                      onClick={() => cancelMutation.mutate(op.id)}
                      disabled={op.status !== 'PENDING' || cancelMutation.isPending}
                      className="px-3 py-1 rounded text-xs bg-danger text-danger-fg hover:opacity-90 disabled:opacity-40 disabled:cursor-not-allowed"
                    >
                      Cancelar
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}
