import { z } from 'zod';

export const createUserSchema = z.object({
  id: z.string().min(1, 'El ID es requerido'),
  name: z.string().min(2, 'El nombre debe tener al menos 2 caracteres'),
  email: z.string().email('Email inválido'),
});

export type CreateUserFormData = z.infer<typeof createUserSchema>;
