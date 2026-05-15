import { useForm, useWatch, Controller } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Wallet } from 'lucide-react';
import { internalTransferSchema, type InternalTransferFormData } from './schemas';
import { Button } from '../../shared/components/Button';
import { AmountInput } from '../../shared/components/AmountInput';
import { Input } from '../../shared/components/Input';
import { Field } from '../../shared/components/Field';
import { SearchSelect } from '../../shared/components/SearchSelect';
import { useUserWalletsQuery } from '../wallets/hooks';
import type { ApiError } from '../../api/error';

interface InternalTransferFormProps {
  userId: string;
  defaultSourceWalletId?: string;
  onSubmit: (data: InternalTransferFormData & { userId: string }) => void;
  isPending: boolean;
  error?: ApiError | null;
}

const ERROR_MESSAGES: Record<string, string> = {
  INSUFFICIENT_FUNDS: 'Saldo insuficiente para realizar la transferencia.',
  WALLET_NOT_FOUND: 'Una de las billeteras no fue encontrada.',
  USER_NOT_FOUND: 'El usuario no fue encontrado.',
};

function resolveErrorMessage(error: ApiError | null | undefined): string | null {
  if (!error) return null;
  // For VALIDATION_ERROR the API already provides the precise reason
  // (e.g. "Source and target wallets must be different"); prefer it.
  if (error.code === 'VALIDATION_ERROR' && error.message) return error.message;
  return ERROR_MESSAGES[error.code] ?? error.message ?? 'Ocurrió un error.';
}

export function InternalTransferForm({
  userId,
  defaultSourceWalletId = '',
  onSubmit,
  isPending,
  error,
}: InternalTransferFormProps) {
  const { data: wallets = [], isLoading: walletsLoading } = useUserWalletsQuery(userId || undefined);

  const {
    register,
    handleSubmit,
    control,
    formState: { errors },
  } = useForm<InternalTransferFormData>({
    resolver: zodResolver(internalTransferSchema),
    defaultValues: { sourceWalletId: defaultSourceWalletId },
  });

  function handleFormSubmit(data: InternalTransferFormData) {
    onSubmit({ ...data, userId });
  }

  const watchedSourceWalletId = useWatch({ control, name: 'sourceWalletId' });

  const walletOptions = wallets.map((w) => ({
    value: w.code,
    label: w.name,
    description: `${w.code} · ${w.type}`,
  }));

  const targetWalletOptions = walletOptions.filter(
    (o) => o.value !== watchedSourceWalletId
  );

  const noWallets = !walletsLoading && wallets.length === 0;
  const onlyOneWallet = !walletsLoading && wallets.length === 1;

  const errorMessage = resolveErrorMessage(error);

  return (
    <form onSubmit={handleSubmit(handleFormSubmit)} className="flex flex-col gap-4">
      {noWallets && (
        <p className="text-stone text-sm" role="status">
          Este usuario no tiene billeteras. Creá al menos dos en la sección de Billeteras
          para poder transferir.
        </p>
      )}
      {onlyOneWallet && (
        <p className="text-stone text-sm" role="status">
          Este usuario tiene una sola billetera. Una transferencia interna requiere
          dos billeteras distintas.
        </p>
      )}
      <Field label="Billetera origen" error={errors.sourceWalletId?.message}>
        <Controller
          control={control}
          name="sourceWalletId"
          render={({ field }) => (
            <SearchSelect
              aria-label="Billetera origen"
              options={walletOptions}
              value={field.value}
              onChange={field.onChange}
              placeholder="Selecciona billetera origen"
              leftIcon={Wallet}
              isLoading={walletsLoading}
            />
          )}
        />
      </Field>
      <Field label="Billetera destino" error={errors.targetWalletId?.message}>
        <Controller
          control={control}
          name="targetWalletId"
          render={({ field }) => (
            <SearchSelect
              aria-label="Billetera destino"
              options={targetWalletOptions}
              value={field.value}
              onChange={field.onChange}
              placeholder="Selecciona billetera destino"
              leftIcon={Wallet}
              isLoading={walletsLoading}
            />
          )}
        />
      </Field>
      <Field label="Monto" error={errors.amount?.message}>
        <AmountInput
          {...register('amount', { valueAsNumber: true })}
          placeholder="0.00"
        />
      </Field>
      <Field label="Descripción (opcional)" error={errors.description?.message}>
        <Input {...register('description')} placeholder="Movimiento interno" />
      </Field>
      {errorMessage && (
        <p className="text-accent-danger text-sm" role="alert">{errorMessage}</p>
      )}
      <Button variant="dark" type="submit" disabled={isPending} className="self-start">
        {isPending ? 'Procesando...' : 'Transferir'}
      </Button>
    </form>
  );
}
