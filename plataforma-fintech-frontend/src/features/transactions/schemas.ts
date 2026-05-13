import { z } from 'zod';

const transactionTypeEnum = z.enum([
  'RECHARGE',
  'WITHDRAWAL',
  'INTERNAL_TRANSFER',
  'EXTERNAL_TRANSFER_SENT',
  'EXTERNAL_TRANSFER_RECEIVED',
]);

const transactionStatusEnum = z.enum(['SUCCESSFUL', 'REVERSED']);

export const transactionFiltersSchema = z.object({
  type: transactionTypeEnum.optional(),
  status: transactionStatusEnum.optional(),
  walletId: z.string().optional(),
});

export type TransactionFiltersFormData = z.infer<typeof transactionFiltersSchema>;
