import { describe, it, expect } from 'vitest';
import { parseApiError } from '../error';

describe('parseApiError', () => {
  it('returns typed ApiError for valid payload with code and message', () => {
    const result = parseApiError({ code: 'USER_NOT_FOUND', message: 'Usuario no encontrado' });
    expect(result).not.toBeNull();
    expect(result?.code).toBe('USER_NOT_FOUND');
    expect(result?.message).toBe('Usuario no encontrado');
  });

  it('returns typed ApiError with details array when provided', () => {
    const result = parseApiError({
      code: 'WALLET_NOT_FOUND',
      message: 'Billetera no encontrada',
      details: ['d1'],
    });
    expect(result).not.toBeNull();
    expect(result?.details).toEqual(['d1']);
  });

  it('returns null for null input', () => {
    expect(parseApiError(null)).toBeNull();
  });

  it('returns null for undefined input', () => {
    expect(parseApiError(undefined)).toBeNull();
  });

  it('returns null for number input', () => {
    expect(parseApiError(42)).toBeNull();
  });

  it('returns null when code is missing', () => {
    expect(parseApiError({ message: 'no code' })).toBeNull();
  });

  it('returns null when message is missing', () => {
    expect(parseApiError({ code: 'X' })).toBeNull();
  });
});
