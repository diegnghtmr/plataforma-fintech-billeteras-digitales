import { describe, it, expect } from 'vitest';
import { transactionFiltersSchema } from '../schemas';

describe('transactionFiltersSchema', () => {
  it('accepts empty object (no filters)', () => {
    const result = transactionFiltersSchema.safeParse({});
    expect(result.success).toBe(true);
  });

  it('accepts valid type RECHARGE', () => {
    const result = transactionFiltersSchema.safeParse({ type: 'RECHARGE' });
    expect(result.success).toBe(true);
  });

  it('accepts valid type WITHDRAWAL', () => {
    const result = transactionFiltersSchema.safeParse({ type: 'WITHDRAWAL' });
    expect(result.success).toBe(true);
  });

  it('accepts valid type INTERNAL_TRANSFER', () => {
    const result = transactionFiltersSchema.safeParse({ type: 'INTERNAL_TRANSFER' });
    expect(result.success).toBe(true);
  });

  it('accepts valid type EXTERNAL_TRANSFER_SENT', () => {
    const result = transactionFiltersSchema.safeParse({ type: 'EXTERNAL_TRANSFER_SENT' });
    expect(result.success).toBe(true);
  });

  it('accepts valid type EXTERNAL_TRANSFER_RECEIVED', () => {
    const result = transactionFiltersSchema.safeParse({ type: 'EXTERNAL_TRANSFER_RECEIVED' });
    expect(result.success).toBe(true);
  });

  it('rejects invalid type value', () => {
    const result = transactionFiltersSchema.safeParse({ type: 'INVALID_TYPE' });
    expect(result.success).toBe(false);
  });

  it('accepts valid status SUCCESSFUL', () => {
    const result = transactionFiltersSchema.safeParse({ status: 'SUCCESSFUL' });
    expect(result.success).toBe(true);
  });

  it('accepts valid status REVERSED', () => {
    const result = transactionFiltersSchema.safeParse({ status: 'REVERSED' });
    expect(result.success).toBe(true);
  });

  it('rejects invalid status value', () => {
    const result = transactionFiltersSchema.safeParse({ status: 'PENDING' });
    expect(result.success).toBe(false);
  });

  it('accepts walletId string', () => {
    const result = transactionFiltersSchema.safeParse({ walletId: 'W001' });
    expect(result.success).toBe(true);
  });

  it('accepts all fields together', () => {
    const result = transactionFiltersSchema.safeParse({
      type: 'RECHARGE',
      status: 'SUCCESSFUL',
      walletId: 'W001',
    });
    expect(result.success).toBe(true);
  });
});
