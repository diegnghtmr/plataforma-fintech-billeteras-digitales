import { describe, it, expect } from 'vitest';
import { createScheduledOperationSchema } from '../schemas';

/**
 * T08-F04 (RED) — Scheduled operation create form schema tests.
 */
describe('createScheduledOperationSchema', () => {
  const validBase = {
    type: 'RECHARGE' as const,
    sourceUserId: 'USR001',
    sourceWalletId: 'W001',
    amount: 100,
    scheduledAt: '2027-01-01T00:00',
  };

  it('valid RECHARGE passes', () => {
    const result = createScheduledOperationSchema.safeParse(validBase);
    expect(result.success).toBe(true);
  });

  it('amount = 0.01 passes', () => {
    const result = createScheduledOperationSchema.safeParse({ ...validBase, amount: 0.01 });
    expect(result.success).toBe(true);
  });

  it('amount = 0 fails', () => {
    const result = createScheduledOperationSchema.safeParse({ ...validBase, amount: 0 });
    expect(result.success).toBe(false);
  });

  it('missing sourceUserId fails', () => {
    const { sourceUserId: _, ...rest } = validBase;
    const result = createScheduledOperationSchema.safeParse(rest);
    expect(result.success).toBe(false);
  });

  it('missing type fails', () => {
    const { type: _, ...rest } = validBase;
    const result = createScheduledOperationSchema.safeParse(rest);
    expect(result.success).toBe(false);
  });

  it('INTERNAL_TRANSFER without targetUserId passes schema (use case validates)', () => {
    const result = createScheduledOperationSchema.safeParse({
      ...validBase,
      type: 'INTERNAL_TRANSFER',
    });
    // Schema does not enforce target fields — use case does
    expect(result.success).toBe(true);
  });

  it('EXTERNAL_TRANSFER with all fields passes', () => {
    const result = createScheduledOperationSchema.safeParse({
      ...validBase,
      type: 'EXTERNAL_TRANSFER',
      targetUserId: 'USR002',
      targetWalletId: 'W002',
    });
    expect(result.success).toBe(true);
  });
});
