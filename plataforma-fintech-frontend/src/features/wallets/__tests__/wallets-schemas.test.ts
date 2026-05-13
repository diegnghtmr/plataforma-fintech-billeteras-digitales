import { describe, it, expect } from 'vitest';
import { createWalletSchema } from '../schemas';

describe('createWalletSchema', () => {
  it('valid payload passes', () => {
    const result = createWalletSchema.safeParse({
      code: 'WALLET-001',
      name: 'Ahorros',
      type: 'SAVINGS',
    });
    expect(result.success).toBe(true);
  });

  it('blank code fails', () => {
    const result = createWalletSchema.safeParse({
      code: '',
      name: 'Ahorros',
      type: 'SAVINGS',
    });
    expect(result.success).toBe(false);
  });

  it('blank name fails', () => {
    const result = createWalletSchema.safeParse({
      code: 'W001',
      name: '',
      type: 'SAVINGS',
    });
    expect(result.success).toBe(false);
  });

  it('blank type fails', () => {
    const result = createWalletSchema.safeParse({
      code: 'W001',
      name: 'Ahorros',
      type: '',
    });
    expect(result.success).toBe(false);
  });
});
