import { describe, it, expect } from 'vitest';
import {
  moneyOperationSchema,
  internalTransferSchema,
  externalTransferSchema,
} from '../schemas';

describe('moneyOperationSchema', () => {
  it('valid amount passes', () => {
    const result = moneyOperationSchema.safeParse({ amount: 100 });
    expect(result.success).toBe(true);
  });

  it('minimum amount 0.01 passes', () => {
    const result = moneyOperationSchema.safeParse({ amount: 0.01 });
    expect(result.success).toBe(true);
  });

  it('amount below 0.01 fails', () => {
    const result = moneyOperationSchema.safeParse({ amount: 0 });
    expect(result.success).toBe(false);
  });

  it('negative amount fails', () => {
    const result = moneyOperationSchema.safeParse({ amount: -1 });
    expect(result.success).toBe(false);
  });

  it('description is optional', () => {
    const result = moneyOperationSchema.safeParse({ amount: 100 });
    expect(result.success).toBe(true);
    expect(result.data?.description).toBeUndefined();
  });

  it('description accepted when provided', () => {
    const result = moneyOperationSchema.safeParse({ amount: 100, description: 'recarga navidad' });
    expect(result.success).toBe(true);
    expect(result.data?.description).toBe('recarga navidad');
  });
});

describe('internalTransferSchema', () => {
  it('valid payload passes', () => {
    const result = internalTransferSchema.safeParse({
      sourceWalletId: 'W001',
      targetWalletId: 'W002',
      amount: 200,
    });
    expect(result.success).toBe(true);
  });

  it('empty sourceWalletId fails', () => {
    const result = internalTransferSchema.safeParse({
      sourceWalletId: '',
      targetWalletId: 'W002',
      amount: 200,
    });
    expect(result.success).toBe(false);
  });

  it('empty targetWalletId fails', () => {
    const result = internalTransferSchema.safeParse({
      sourceWalletId: 'W001',
      targetWalletId: '',
      amount: 200,
    });
    expect(result.success).toBe(false);
  });

  it('amount below 0.01 fails', () => {
    const result = internalTransferSchema.safeParse({
      sourceWalletId: 'W001',
      targetWalletId: 'W002',
      amount: 0,
    });
    expect(result.success).toBe(false);
  });

  it('description is optional', () => {
    const result = internalTransferSchema.safeParse({
      sourceWalletId: 'W001',
      targetWalletId: 'W002',
      amount: 100,
    });
    expect(result.success).toBe(true);
  });
});

describe('externalTransferSchema', () => {
  const validPayload = {
    sourceUserId: 'USR001',
    sourceWalletId: 'W001',
    targetUserId: 'USR002',
    targetWalletId: 'W002',
    amount: 500,
  };

  it('valid payload passes', () => {
    const result = externalTransferSchema.safeParse(validPayload);
    expect(result.success).toBe(true);
  });

  it('empty sourceUserId fails', () => {
    const result = externalTransferSchema.safeParse({ ...validPayload, sourceUserId: '' });
    expect(result.success).toBe(false);
  });

  it('empty sourceWalletId fails', () => {
    const result = externalTransferSchema.safeParse({ ...validPayload, sourceWalletId: '' });
    expect(result.success).toBe(false);
  });

  it('empty targetUserId fails', () => {
    const result = externalTransferSchema.safeParse({ ...validPayload, targetUserId: '' });
    expect(result.success).toBe(false);
  });

  it('empty targetWalletId fails', () => {
    const result = externalTransferSchema.safeParse({ ...validPayload, targetWalletId: '' });
    expect(result.success).toBe(false);
  });

  it('amount below 0.01 fails', () => {
    const result = externalTransferSchema.safeParse({ ...validPayload, amount: 0 });
    expect(result.success).toBe(false);
  });

  it('description is optional', () => {
    const result = externalTransferSchema.safeParse(validPayload);
    expect(result.success).toBe(true);
  });

  it('rejects when sourceUserId equals targetUserId', () => {
    const result = externalTransferSchema.safeParse({
      ...validPayload,
      targetUserId: 'USR001',
    });
    expect(result.success).toBe(false);
    if (!result.success) {
      const issue = result.error.issues.find((i) => i.path.includes('targetUserId'));
      expect(issue?.message).toContain('distinto al de origen');
    }
  });
});

describe('internalTransferSchema same-wallet rule', () => {
  it('rejects when sourceWalletId equals targetWalletId', () => {
    const result = internalTransferSchema.safeParse({
      sourceWalletId: 'W001',
      targetWalletId: 'W001',
      amount: 100,
    });
    expect(result.success).toBe(false);
    if (!result.success) {
      const issue = result.error.issues.find((i) => i.path.includes('targetWalletId'));
      expect(issue?.message).toContain('distinta a la de origen');
    }
  });
});
