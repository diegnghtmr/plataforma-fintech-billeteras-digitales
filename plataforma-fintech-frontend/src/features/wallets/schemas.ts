import { z } from 'zod';

export const createWalletSchema = z.object({
  code: z.string().min(1, 'El código es requerido'),
  name: z.string().min(1, 'El nombre es requerido'),
  type: z.string().min(1, 'El tipo es requerido'),
});

export type CreateWalletFormData = z.infer<typeof createWalletSchema>;
