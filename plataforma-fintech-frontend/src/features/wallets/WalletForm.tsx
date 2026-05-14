import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Tag } from 'lucide-react';
import { createWalletSchema, type CreateWalletFormData } from './schemas';
import { Button } from '../../shared/components/Button';
import { Input } from '../../shared/components/Input';
import { Field } from '../../shared/components/Field';
import { WALLET_TYPE_LABEL } from '../../shared/i18n/enum-labels';

interface WalletFormProps {
  onSubmit: (data: CreateWalletFormData) => void;
  isPending: boolean;
}

const WALLET_TYPES = Object.keys(WALLET_TYPE_LABEL) as Array<keyof typeof WALLET_TYPE_LABEL>;

export function WalletForm({ onSubmit, isPending }: WalletFormProps) {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<CreateWalletFormData>({
    resolver: zodResolver(createWalletSchema),
    defaultValues: { type: WALLET_TYPES[0] },
  });

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-5">
      <Field label="Nombre" error={errors.name?.message}>
        <Input leftIcon={Tag} {...register('name')} placeholder="Ahorros" />
      </Field>
      <Field label="Tipo" error={errors.type?.message}>
        <select
          {...register('type')}
          aria-label="Tipo de billetera"
          className="w-full bg-canvas-light text-ink border border-hairline-light rounded-[12px] h-14 px-4 text-body-md focus:outline-none focus:border-brand focus:ring-1 focus:ring-brand"
        >
          {WALLET_TYPES.map((t) => (
            <option key={t} value={t}>
              {WALLET_TYPE_LABEL[t]}
            </option>
          ))}
        </select>
      </Field>
      <Button variant="dark" type="submit" disabled={isPending} className="self-start">
        {isPending ? 'Creando...' : 'Crear billetera'}
      </Button>
    </form>
  );
}
