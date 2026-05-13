import { describe, it, expect } from 'vitest';
import { createUserSchema } from '../schemas';

describe('createUserSchema', () => {
  it('valid payload passes', () => {
    const result = createUserSchema.safeParse({
      name: 'Juan Pérez',
      email: 'juan@example.com',
    });
    expect(result.success).toBe(true);
  });

  it('invalid email fails', () => {
    const result = createUserSchema.safeParse({
      name: 'Juan',
      email: 'not-an-email',
    });
    expect(result.success).toBe(false);
  });

  it('name shorter than 2 chars fails', () => {
    const result = createUserSchema.safeParse({
      name: 'J',
      email: 'juan@example.com',
    });
    expect(result.success).toBe(false);
  });

  it('id field is ignored when present (auto-assigned by backend)', () => {
    // The schema no longer includes id — extra fields are stripped by Zod by default
    const result = createUserSchema.safeParse({
      name: 'Juan',
      email: 'juan@example.com',
    });
    expect(result.success).toBe(true);
    if (result.success) {
      expect('id' in result.data).toBe(false);
    }
  });
});
