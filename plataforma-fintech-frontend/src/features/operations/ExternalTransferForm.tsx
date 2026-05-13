import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { externalTransferSchema, type ExternalTransferFormData } from './schemas';
import { Button } from '../../shared/components/Button';
import { Input } from '../../shared/components/Input';
import { Field } from '../../shared/components/Field';
import type { ApiError } from '../../api/error';

interface ExternalTransferFormProps {
  defaultSourceUserId?: string;
  defaultSourceWalletId?: string;
  onSubmit: (data: ExternalTransferFormData) => void;
  isPending: boolean;
  error?: ApiError | null;
}

const ERROR_MESSAGES: Record<string, string> = {
  INSUFFICIENT_FUNDS: 'Saldo insuficiente para realizar la transferencia.',
  VALIDATION_ERROR: 'Los datos ingresados no son válidos.',
  WALLET_NOT_FOUND: 'Una de las billeteras no fue encontrada.',
  USER_NOT_FOUND: 'El usuario de destino no fue encontrado.',
};

export function ExternalTransferForm({
  defaultSourceUserId = '',
  defaultSourceWalletId = '',
  onSubmit,
  isPending,
  error,
}: ExternalTransferFormProps) {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<ExternalTransferFormData>({
    resolver: zodResolver(externalTransferSchema),
    defaultValues: {
      sourceUserId: defaultSourceUserId,
      sourceWalletId: defaultSourceWalletId,
    },
  });

  const errorMessage = error
    ? (ERROR_MESSAGES[error.code] ?? error.message)
    : null;

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4">
      <Field label="Usuario origen" error={errors.sourceUserId?.message}>
        <Input {...register('sourceUserId')} placeholder="USR001" />
      </Field>
      <Field label="Billetera origen" error={errors.sourceWalletId?.message}>
        <Input {...register('sourceWalletId')} placeholder="W001" />
      </Field>
      <Field label="Usuario destino" error={errors.targetUserId?.message}>
        <Input {...register('targetUserId')} placeholder="USR002" />
      </Field>
      <Field label="Billetera destino" error={errors.targetWalletId?.message}>
        <Input {...register('targetWalletId')} placeholder="W002" />
      </Field>
      <Field label="Monto" error={errors.amount?.message}>
        <Input
          type="number"
          step="0.01"
          {...register('amount', { valueAsNumber: true })}
          placeholder="0.00"
        />
      </Field>
      <Field label="Descripción (opcional)" error={errors.description?.message}>
        <Input {...register('description')} placeholder="Envío de fondos" />
      </Field>
      {errorMessage && (
        <p className="text-red-500 text-sm" role="alert">{errorMessage}</p>
      )}
      <Button type="submit" disabled={isPending}>
        {isPending ? 'Procesando...' : 'Transferir a otro usuario'}
      </Button>
    </form>
  );
}
