import { z } from 'zod';

export const moneyOperationSchema = z.object({
  amount: z.number().positive('El monto debe ser mayor a 0').finite(),
  description: z.string().optional(),
});

export type MoneyOperationFormData = z.infer<typeof moneyOperationSchema>;

export const internalTransferSchema = z
  .object({
    sourceWalletId: z.string().min(1, 'La billetera de origen es requerida'),
    targetWalletId: z.string().min(1, 'La billetera de destino es requerida'),
    amount: z.number().positive('El monto debe ser mayor a 0').finite(),
    description: z.string().optional(),
  })
  .refine((d) => d.sourceWalletId !== d.targetWalletId, {
    message: 'La billetera de destino debe ser distinta a la de origen.',
    path: ['targetWalletId'],
  });

export type InternalTransferFormData = z.infer<typeof internalTransferSchema>;

export const externalTransferSchema = z
  .object({
    sourceUserId: z.string().min(1, 'El usuario de origen es requerido'),
    sourceWalletId: z.string().min(1, 'La billetera de origen es requerida'),
    targetUserId: z.string().min(1, 'El usuario de destino es requerido'),
    targetWalletId: z.string().min(1, 'La billetera de destino es requerida'),
    amount: z.number().positive('El monto debe ser mayor a 0').finite(),
    description: z.string().optional(),
  })
  .refine((d) => d.sourceUserId !== d.targetUserId, {
    message: 'El usuario de destino debe ser distinto al de origen. Usa "Transferencia interna" si quieres mover entre billeteras propias.',
    path: ['targetUserId'],
  });

export type ExternalTransferFormData = z.infer<typeof externalTransferSchema>;
