import { describe, it, expect } from 'vitest';
import { createUserSchema } from '../schemas';

describe('createUserSchema', () => {
  it('valid payload passes', () => {
    const result = createUserSchema.safeParse({
      id: 'USR001',
      name: 'Juan Pérez',
      email: 'juan@example.com',
    });
    expect(result.success).toBe(true);
  });

  it('invalid email fails', () => {
    const result = createUserSchema.safeParse({
      id: 'USR001',
      name: 'Juan',
      email: 'not-an-email',
    });
    expect(result.success).toBe(false);
  });

  it('name shorter than 2 chars fails', () => {
    const result = createUserSchema.safeParse({
      id: 'USR001',
      name: 'J',
      email: 'juan@example.com',
    });
    expect(result.success).toBe(false);
  });

  it('blank id fails', () => {
    const result = createUserSchema.safeParse({
      id: '',
      name: 'Juan',
      email: 'juan@example.com',
    });
    expect(result.success).toBe(false);
  });
});
