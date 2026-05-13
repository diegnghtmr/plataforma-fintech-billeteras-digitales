import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { createWalletSchema, type CreateWalletFormData } from './schemas';
import { Button } from '../../shared/components/Button';
import { Input } from '../../shared/components/Input';
import { Field } from '../../shared/components/Field';

interface WalletFormProps {
  onSubmit: (data: CreateWalletFormData) => void;
  isPending: boolean;
}

export function WalletForm({ onSubmit, isPending }: WalletFormProps) {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<CreateWalletFormData>({
    resolver: zodResolver(createWalletSchema),
  });

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4">
      <Field label="Código" error={errors.code?.message}>
        <Input {...register('code')} placeholder="WALLET-001" />
      </Field>
      <Field label="Nombre" error={errors.name?.message}>
        <Input {...register('name')} placeholder="Ahorros" />
      </Field>
      <Field label="Tipo" error={errors.type?.message}>
        <Input {...register('type')} placeholder="SAVINGS" />
      </Field>
      <Button type="submit" disabled={isPending}>
        {isPending ? 'Creando...' : 'Crear billetera'}
      </Button>
    </form>
  );
}
