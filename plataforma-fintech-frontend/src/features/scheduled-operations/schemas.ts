import { z } from 'zod';

export const SCHEDULED_OPERATION_TYPES = [
  'RECHARGE',
  'WITHDRAWAL',
  'INTERNAL_TRANSFER',
  'EXTERNAL_TRANSFER',
] as const;

export const createScheduledOperationSchema = z.object({
  type: z.enum(SCHEDULED_OPERATION_TYPES),
  sourceUserId: z.string().min(1, 'El usuario de origen es requerido'),
  sourceWalletId: z.string().min(1, 'La billetera de origen es requerida'),
  targetUserId: z.string().optional(),
  targetWalletId: z.string().optional(),
  amount: z.number().positive('El monto debe ser mayor a 0').finite(),
  scheduledAt: z.string().min(1, 'La fecha programada es requerida'),
  description: z.string().optional(),
});

export type CreateScheduledOperationFormData = z.infer<typeof createScheduledOperationSchema>;
