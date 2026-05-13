import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Hash, User, Mail } from 'lucide-react';
import { createUserSchema, type CreateUserFormData } from './schemas';
import { Button } from '../../shared/components/Button';
import { Input } from '../../shared/components/Input';
import { Field } from '../../shared/components/Field';

interface UserFormProps {
  onSubmit: (data: CreateUserFormData) => void;
  isPending: boolean;
}

export function UserForm({ onSubmit, isPending }: UserFormProps) {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<CreateUserFormData>({
    resolver: zodResolver(createUserSchema),
  });

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4">
      <Field label="ID" error={errors.id?.message}>
        <Input aria-label="ID" leftIcon={Hash} {...register('id')} placeholder="USR001" />
      </Field>
      <Field label="Nombre" error={errors.name?.message}>
        <Input aria-label="Nombre" leftIcon={User} {...register('name')} placeholder="Juan Pérez" />
      </Field>
      <Field label="Email" error={errors.email?.message}>
        <Input aria-label="Email" leftIcon={Mail} type="email" {...register('email')} placeholder="juan@example.com" />
      </Field>
      <Button type="submit" variant="dark" disabled={isPending}>
        {isPending ? 'Creando...' : 'Crear usuario'}
      </Button>
    </form>
  );
}
