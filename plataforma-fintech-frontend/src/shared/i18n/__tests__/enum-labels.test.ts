import { describe, it, expect } from 'vitest';
import {
  NOTIFICATION_TYPE_LABEL,
  FRAUD_TYPE_LABEL,
  labelNotificationType,
  labelFraudType,
} from '../enum-labels';

describe('NOTIFICATION_TYPE_LABEL', () => {
  it('has label for SCHEDULED_REMINDER', () => {
    expect(NOTIFICATION_TYPE_LABEL.SCHEDULED_REMINDER).toBe('Recordatorio de operación');
  });

  it('has label for OPERATION_REJECTED', () => {
    expect(NOTIFICATION_TYPE_LABEL.OPERATION_REJECTED).toBe('Operación rechazada');
  });

  it('has label for BENEFIT_REDEEMED', () => {
    expect(NOTIFICATION_TYPE_LABEL.BENEFIT_REDEEMED).toBe('Beneficio canjeado');
  });

  it('still has all original labels', () => {
    expect(NOTIFICATION_TYPE_LABEL.LOW_BALANCE).toBe('Saldo bajo');
    expect(NOTIFICATION_TYPE_LABEL.TRANSACTION).toBe('Transacción');
    expect(NOTIFICATION_TYPE_LABEL.FRAUD_ALERT).toBe('Alerta de fraude');
    expect(NOTIFICATION_TYPE_LABEL.POINTS_LEVEL).toBe('Nivel de puntos');
    expect(NOTIFICATION_TYPE_LABEL.SYSTEM).toBe('Sistema');
  });

  it('labelNotificationType returns label for SCHEDULED_REMINDER', () => {
    expect(labelNotificationType('SCHEDULED_REMINDER')).toBe('Recordatorio de operación');
  });

  it('labelNotificationType falls back to value for unknown type', () => {
    expect(labelNotificationType('UNKNOWN_TYPE')).toBe('UNKNOWN_TYPE');
  });
});

describe('FRAUD_TYPE_LABEL', () => {
  it('has label for REPEATED_DESTINATION', () => {
    expect(FRAUD_TYPE_LABEL.REPEATED_DESTINATION).toBe('Destino repetido');
  });

  it('has label for WALLET_FRAGMENTATION', () => {
    expect(FRAUD_TYPE_LABEL.WALLET_FRAGMENTATION).toBe('Fragmentación de monto');
  });

  it('has label for FREQUENCY_BURST', () => {
    expect(FRAUD_TYPE_LABEL.FREQUENCY_BURST).toBe('Pico de frecuencia');
  });

  it('has label for OFF_HOURS', () => {
    expect(FRAUD_TYPE_LABEL.OFF_HOURS).toBe('Horario inusual');
  });

  it('still has all original fraud labels', () => {
    expect(FRAUD_TYPE_LABEL.LARGE_TRANSACTION).toBe('Transacción grande');
    expect(FRAUD_TYPE_LABEL.HIGH_VELOCITY).toBe('Alta velocidad');
    expect(FRAUD_TYPE_LABEL.CYCLE_DETECTED).toBe('Ciclo detectado');
  });

  it('labelFraudType returns label for REPEATED_DESTINATION', () => {
    expect(labelFraudType('REPEATED_DESTINATION')).toBe('Destino repetido');
  });

  it('labelFraudType falls back to value for unknown type', () => {
    expect(labelFraudType('UNKNOWN_FRAUD')).toBe('UNKNOWN_FRAUD');
  });
});
