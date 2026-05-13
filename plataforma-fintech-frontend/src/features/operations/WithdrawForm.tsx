import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { FileText } from 'lucide-react';
import { moneyOperationSchema, type MoneyOperationFormData } from './schemas';
import { Button } from '../../shared/components/Button';
import { AmountInput } from '../../shared/components/AmountInput';
import { Input } from '../../shared/components/Input';
import { Field } from '../../shared/components/Field';
import type { ApiError } from '../../api/error';

interface WithdrawFormProps {
  userId: string;
  walletId: string;
  onSubmit: (data: MoneyOperationFormData & { userId: string; walletId: string }) => void;
  isPending: boolean;
  error?: ApiError | null;
}

const ERROR_MESSAGES: Record<string, string> = {
  INSUFFICIENT_FUNDS: 'Saldo insuficiente para realizar el retiro.',
  VALIDATION_ERROR: 'Los datos ingresados no son válidos.',
  WALLET_NOT_FOUND: 'La billetera no fue encontrada.',
  USER_NOT_FOUND: 'El usuario no fue encontrado.',
};

export function WithdrawForm({ userId, walletId, onSubmit, isPending, error }: WithdrawFormProps) {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<MoneyOperationFormData>({
    resolver: zodResolver(moneyOperationSchema),
  });

  function handleFormSubmit(data: MoneyOperationFormData) {
    onSubmit({ ...data, userId, walletId });
  }

  const errorMessage = error
    ? (ERROR_MESSAGES[error.code] ?? error.message)
    : null;

  return (
    <form onSubmit={handleSubmit(handleFormSubmit)} className="flex flex-col gap-4">
      <Field label="Monto" error={errors.amount?.message}>
        <AmountInput
          {...register('amount', { valueAsNumber: true })}
          placeholder="0.00"
        />
      </Field>
      <Field label="Descripción (opcional)" error={errors.description?.message}>
        <Input leftIcon={FileText} {...register('description')} placeholder="Retiro de efectivo" />
      </Field>
      {errorMessage && (
        <p className="text-accent-danger text-sm" role="alert">{errorMessage}</p>
      )}
      <Button variant="dark" type="submit" disabled={isPending} className="self-start">
        {isPending ? 'Procesando...' : 'Retirar'}
      </Button>
    </form>
  );
}
