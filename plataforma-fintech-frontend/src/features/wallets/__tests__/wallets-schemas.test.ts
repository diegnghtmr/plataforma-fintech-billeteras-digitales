import { describe, it, expect } from 'vitest';
import { createWalletSchema } from '../schemas';

describe('createWalletSchema', () => {
  it('valid payload passes', () => {
    const result = createWalletSchema.safeParse({
      name: 'Ahorros',
      type: 'SAVINGS',
    });
    expect(result.success).toBe(true);
  });

  it('blank name fails', () => {
    const result = createWalletSchema.safeParse({
      name: '',
      type: 'SAVINGS',
    });
    expect(result.success).toBe(false);
  });

  it('blank type fails', () => {
    const result = createWalletSchema.safeParse({
      name: 'Ahorros',
      type: '',
    });
    expect(result.success).toBe(false);
  });

  it('missing name fails', () => {
    const result = createWalletSchema.safeParse({
      type: 'SAVINGS',
    });
    expect(result.success).toBe(false);
  });

  it('missing type fails', () => {
    const result = createWalletSchema.safeParse({
      name: 'Ahorros',
    });
    expect(result.success).toBe(false);
  });
});
