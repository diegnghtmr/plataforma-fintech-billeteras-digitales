export const OPERATION_TYPE_LABEL = {
  RECHARGE: 'Recarga',
  WITHDRAWAL: 'Retiro',
  INTERNAL_TRANSFER: 'Transferencia interna',
  EXTERNAL_TRANSFER: 'Transferencia externa',
  EXTERNAL_TRANSFER_SENT: 'Transferencia externa enviada',
  EXTERNAL_TRANSFER_RECEIVED: 'Transferencia externa recibida',
} as const;

export const OPERATION_STATUS_LABEL = {
  PENDING: 'Pendiente',
  EXECUTED: 'Ejecutada',
  CANCELLED: 'Cancelada',
  FAILED: 'Fallida',
  SUCCESSFUL: 'Exitosa',
  REVERSED: 'Revertida',
  REJECTED: 'Rechazada',
} as const;

export const NOTIFICATION_TYPE_LABEL = {
  LOW_BALANCE: 'Saldo bajo',
  TRANSACTION: 'Transacción',
  FRAUD_ALERT: 'Alerta de fraude',
  POINTS_LEVEL: 'Nivel de puntos',
  SYSTEM: 'Sistema',
  SCHEDULED_REMINDER: 'Recordatorio de operación',
  OPERATION_REJECTED: 'Operación rechazada',
  BENEFIT_REDEEMED: 'Beneficio canjeado',
} as const;

export const NOTIFICATION_SEVERITY_LABEL = {
  INFO: 'Información',
  WARNING: 'Advertencia',
  CRITICAL: 'Crítica',
} as const;

export const FRAUD_SEVERITY_LABEL = {
  LOW: 'Bajo',
  MEDIUM: 'Medio',
  HIGH: 'Alto',
  CRITICAL: 'Crítico',
} as const;

export const FRAUD_TYPE_LABEL = {
  LARGE_TRANSACTION: 'Transacción grande',
  HIGH_VELOCITY: 'Alta velocidad',
  CYCLE_DETECTED: 'Ciclo detectado',
  REPEATED_DESTINATION: 'Destino repetido',
  WALLET_FRAGMENTATION: 'Fragmentación de monto',
  FREQUENCY_BURST: 'Pico de frecuencia',
  OFF_HOURS: 'Horario inusual',
} as const;

export const LOYALTY_LEVEL_LABEL = {
  BRONZE: 'Bronce',
  SILVER: 'Plata',
  GOLD: 'Oro',
  PLATINUM: 'Platino',
} as const;

export function labelOperationType(value: string): string {
  return (OPERATION_TYPE_LABEL as Record<string, string>)[value] ?? value;
}

export function labelOperationStatus(value: string): string {
  return (OPERATION_STATUS_LABEL as Record<string, string>)[value] ?? value;
}

export function labelNotificationType(value: string): string {
  return (NOTIFICATION_TYPE_LABEL as Record<string, string>)[value] ?? value;
}

export function labelNotificationSeverity(value: string): string {
  return (NOTIFICATION_SEVERITY_LABEL as Record<string, string>)[value] ?? value;
}

export function labelFraudSeverity(value: string): string {
  return (FRAUD_SEVERITY_LABEL as Record<string, string>)[value] ?? value;
}

export function labelFraudType(value: string): string {
  return (FRAUD_TYPE_LABEL as Record<string, string>)[value] ?? value;
}

export function labelLoyaltyLevel(value: string): string {
  return (LOYALTY_LEVEL_LABEL as Record<string, string>)[value] ?? value;
}

export const WALLET_TYPE_LABEL = {
  DAILY_EXPENSES: 'Gastos diarios',
  SAVINGS: 'Ahorros',
  INVESTMENT: 'Inversiones',
  SHOPPING: 'Compras',
  SUBSCRIPTIONS: 'Suscripciones',
  TRANSPORT: 'Transporte',
} as const;

export function labelWalletType(value: string): string {
  return (WALLET_TYPE_LABEL as Record<string, string>)[value] ?? value;
}
