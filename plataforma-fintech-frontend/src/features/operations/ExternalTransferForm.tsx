import { useForm, useWatch, Controller } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { DollarSign, User, Wallet } from 'lucide-react';
import { externalTransferSchema, type ExternalTransferFormData } from './schemas';
import { Button } from '../../shared/components/Button';
import { Input } from '../../shared/components/Input';
import { Field } from '../../shared/components/Field';
import { SearchSelect } from '../../shared/components/SearchSelect';
import { useUsersListQuery } from '../users/hooks';
import { useUserWalletsQuery } from '../wallets/hooks';
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
  const { data: users = [], isLoading: usersLoading } = useUsersListQuery();

  const {
    register,
    handleSubmit,
    control,
    formState: { errors },
  } = useForm<ExternalTransferFormData>({
    resolver: zodResolver(externalTransferSchema),
    defaultValues: {
      sourceUserId: defaultSourceUserId,
      sourceWalletId: defaultSourceWalletId,
    },
  });

  const watchedSourceUserId = useWatch({ control, name: 'sourceUserId' });
  const watchedTargetUserId = useWatch({ control, name: 'targetUserId' });

  const { data: sourceWallets = [], isLoading: sourceWalletsLoading } = useUserWalletsQuery(
    watchedSourceUserId || undefined
  );
  const { data: targetWallets = [], isLoading: targetWalletsLoading } = useUserWalletsQuery(
    watchedTargetUserId || undefined
  );

  const userOptions = users.map((u) => ({
    value: u.id,
    label: u.name,
    description: `${u.id} · ${u.email}`,
  }));

  const sourceWalletOptions = sourceWallets.map((w) => ({
    value: w.code,
    label: w.name,
    description: `${w.code} · ${w.type}`,
  }));

  const targetWalletOptions = targetWallets.map((w) => ({
    value: w.code,
    label: w.name,
    description: `${w.code} · ${w.type}`,
  }));

  const errorMessage = error
    ? (ERROR_MESSAGES[error.code] ?? error.message)
    : null;

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4">
      <Field label="Usuario origen" error={errors.sourceUserId?.message}>
        <Controller
          control={control}
          name="sourceUserId"
          render={({ field }) => (
            <SearchSelect
              aria-label="Usuario origen"
              options={userOptions}
              value={field.value}
              onChange={field.onChange}
              placeholder="Selecciona un usuario"
              leftIcon={User}
              isLoading={usersLoading}
            />
          )}
        />
      </Field>
      <Field label="Billetera origen" error={errors.sourceWalletId?.message}>
        <Controller
          control={control}
          name="sourceWalletId"
          render={({ field }) => (
            <SearchSelect
              aria-label="Billetera origen"
              options={sourceWalletOptions}
              value={field.value}
              onChange={field.onChange}
              placeholder="Selecciona una billetera"
              emptyMessage={watchedSourceUserId ? 'Sin billeteras' : 'Selecciona un usuario primero'}
              leftIcon={Wallet}
              isLoading={sourceWalletsLoading}
              disabled={!watchedSourceUserId}
            />
          )}
        />
      </Field>
      <Field label="Usuario destino" error={errors.targetUserId?.message}>
        <Controller
          control={control}
          name="targetUserId"
          render={({ field }) => (
            <SearchSelect
              aria-label="Usuario destino"
              options={userOptions}
              value={field.value}
              onChange={field.onChange}
              placeholder="Selecciona un usuario"
              leftIcon={User}
              isLoading={usersLoading}
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
              placeholder="Selecciona una billetera"
              emptyMessage={watchedTargetUserId ? 'Sin billeteras' : 'Selecciona un usuario destino primero'}
              leftIcon={Wallet}
              isLoading={targetWalletsLoading}
              disabled={!watchedTargetUserId}
            />
          )}
        />
      </Field>
      <Field label="Monto" error={errors.amount?.message}>
        <Input
          type="number"
          step="0.01"
          leftIcon={DollarSign}
          {...register('amount', { valueAsNumber: true })}
          placeholder="0.00"
        />
      </Field>
      <Field label="Descripción (opcional)" error={errors.description?.message}>
        <Input {...register('description')} placeholder="Envío de fondos" />
      </Field>
      {errorMessage && (
        <p className="text-accent-danger text-sm" role="alert">{errorMessage}</p>
      )}
      <Button variant="dark" type="submit" disabled={isPending} className="self-start">
        {isPending ? 'Procesando...' : 'Transferir a otro usuario'}
      </Button>
    </form>
  );
}
