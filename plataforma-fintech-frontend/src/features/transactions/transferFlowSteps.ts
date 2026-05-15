import type { TransactionResponse } from '../../api/transactions';

export type NodeKey =
  | 'sourceUser'
  | 'sourceWallet'
  | 'graph'
  | 'targetWallet'
  | 'targetUser'
  | 'points'
  | 'fraud'
  | 'persistence'
  | 'notification';

export interface FlowStep {
  id: string;
  title: string;
  detail: string;
  activeNodes: NodeKey[];
  activeEdge?: { from: NodeKey; to: NodeKey };
}

function fmt(amount: number): string {
  return new Intl.NumberFormat('es-AR', {
    style: 'currency',
    currency: 'USD',
    maximumFractionDigits: 2,
  }).format(amount);
}

/**
 * Builds the step-by-step sequence for any transaction type.
 * Pure function — no side effects, fully testable.
 */
export function buildFlowSteps(tx: TransactionResponse): FlowStep[] {
  const amount = fmt(tx.amount);
  const points = tx.pointsGenerated ?? 0;
  const risk = tx.riskLevel ?? null;
  const fraudDetail = risk
    ? `FraudDetector marcó esta transacción con riesgo ${risk}.`
    : 'FraudDetector evaluó la transacción: sin alertas.';

  switch (tx.type) {
    case 'RECHARGE':
      return [
        step('validate-user', 'Validar usuario', `UserRepository.findById(${tx.sourceUserId}) confirma el titular.`, ['sourceUser']),
        step('validate-wallet', 'Validar billetera', `WalletRepository.findByOwnerIdAndCode(${tx.sourceUserId}, ${tx.sourceWalletId}) ok.`, ['sourceWallet'], { from: 'sourceUser', to: 'sourceWallet' }),
        step('credit', `Acreditar billetera +${amount}`, `RechargeWalletUseCase incrementa el saldo y transactionCount.`, ['sourceWallet']),
        step('points', `Calcular puntos +${points}`, `PuntosCalculator.compute(RECHARGE, ${amount}) → ${points} pts.`, ['points'], { from: 'sourceWallet', to: 'points' }),
        step('fraud', 'Detección de fraude', fraudDetail, ['fraud'], { from: 'sourceWallet', to: 'fraud' }),
        step('persist', 'Persistir transacción', `TransactionRepository.save(${tx.id}) y push en la pila reversible.`, ['persistence']),
        step('notify', 'Notificación', 'NotificationEmitter dispara level-up o saldo bajo si corresponde.', ['notification'], { from: 'persistence', to: 'notification' }),
      ];

    case 'WITHDRAWAL':
      return [
        step('validate-user', 'Validar usuario', `UserRepository.findById(${tx.sourceUserId}) confirma el titular.`, ['sourceUser']),
        step('validate-wallet', 'Validar billetera', `WalletRepository.findByOwnerIdAndCode(${tx.sourceUserId}, ${tx.sourceWalletId}) ok.`, ['sourceWallet'], { from: 'sourceUser', to: 'sourceWallet' }),
        step('debit', `Debitar billetera −${amount}`, `WithdrawWalletUseCase descuenta el monto del saldo.`, ['sourceWallet']),
        step('points', `Calcular puntos +${points}`, `PuntosCalculator.compute(WITHDRAWAL, ${amount}) → ${points} pts.`, ['points'], { from: 'sourceWallet', to: 'points' }),
        step('fraud', 'Detección de fraude', fraudDetail, ['fraud'], { from: 'sourceWallet', to: 'fraud' }),
        step('persist', 'Persistir transacción', `TransactionRepository.save(${tx.id}) y push en la pila reversible.`, ['persistence']),
        step('notify', 'Notificación', 'Si el saldo cae bajo 100, NotificationEmitter envía aviso al titular.', ['notification'], { from: 'persistence', to: 'notification' }),
      ];

    case 'INTERNAL_TRANSFER':
      return [
        step('validate-user', 'Validar usuario', `UserRepository.findById(${tx.sourceUserId}).`, ['sourceUser']),
        step('validate-source', 'Validar billetera origen', `WalletRepository confirma ${tx.sourceWalletId}.`, ['sourceWallet'], { from: 'sourceUser', to: 'sourceWallet' }),
        step('debit', `Debitar origen −${amount}`, `InternalTransferUseCase descuenta del saldo origen.`, ['sourceWallet']),
        step('validate-target', 'Validar billetera destino', `WalletRepository confirma ${tx.targetWalletId}.`, ['targetWallet'], { from: 'sourceWallet', to: 'targetWallet' }),
        step('credit', `Acreditar destino +${amount}`, 'El mismo usuario recibe el monto en su otra billetera.', ['targetWallet']),
        step('points', `Calcular puntos +${points}`, `PuntosCalculator.compute(INTERNAL_TRANSFER, ${amount}).`, ['points'], { from: 'targetWallet', to: 'points' }),
        step('fraud', 'Detección de fraude', fraudDetail, ['fraud'], { from: 'targetWallet', to: 'fraud' }),
        step('persist', 'Persistir transacción', `TransactionRepository.save(${tx.id}).`, ['persistence']),
        step('notify', 'Notificación', 'Si hay level-up o saldo bajo, NotificationEmitter avisa al titular.', ['notification'], { from: 'persistence', to: 'notification' }),
      ];

    case 'EXTERNAL_TRANSFER_SENT':
    case 'EXTERNAL_TRANSFER_RECEIVED':
    case 'EXTERNAL_TRANSFER': {
      const isReceived = tx.type === 'EXTERNAL_TRANSFER_RECEIVED';
      const note = isReceived
        ? ' (vista del receptor: el caso de uso se ejecutó desde el emisor)'
        : '';
      return [
        step('validate-src-user', `Validar usuario origen${note}`, `UserRepository.findById(${tx.sourceUserId}).`, ['sourceUser']),
        step('validate-src-wallet', 'Validar billetera origen', `WalletRepository confirma ${tx.sourceWalletId}.`, ['sourceWallet'], { from: 'sourceUser', to: 'sourceWallet' }),
        step('debit', `Debitar origen −${amount}`, 'ExternalTransferUseCase descuenta el monto y el costo de envío.', ['sourceWallet']),
        step('validate-tgt-user', 'Validar usuario destino', `UserRepository.findById(${tx.targetUserId ?? '—'}).`, ['targetUser'], { from: 'sourceWallet', to: 'targetUser' }),
        step('validate-tgt-wallet', 'Validar billetera destino', `WalletRepository confirma ${tx.targetWalletId ?? '—'}.`, ['targetWallet'], { from: 'targetUser', to: 'targetWallet' }),
        step('credit', `Acreditar destino +${amount}`, 'El receptor recibe el monto neto.', ['targetWallet']),
        step('graph', 'Registrar arista en GrafoTransferencias', `graph.addEdge(${tx.sourceUserId} → ${tx.targetUserId ?? '—'}, ${amount}).`, ['graph'], { from: 'sourceUser', to: 'targetUser' }),
        step('points', `Calcular puntos +${points}`, 'PuntosCalculator otorga puntos al emisor (y al receptor según política).', ['points'], { from: 'targetWallet', to: 'points' }),
        step('fraud', 'Detección de fraude', fraudDetail, ['fraud'], { from: 'sourceWallet', to: 'fraud' }),
        step('persist', 'Persistir transacciones (sent + received)', `TransactionRepository.save(outgoing) y save(incoming). Push reversible si es SENT.`, ['persistence']),
        step('notify', 'Notificación', 'NotificationEmitter dispara fraud-alert/level-up/low-balance según corresponda.', ['notification'], { from: 'persistence', to: 'notification' }),
      ];
    }

    default:
      return [
        step('persist', 'Persistir transacción', `TransactionRepository.save(${tx.id}).`, ['persistence']),
      ];
  }
}

function step(
  id: string,
  title: string,
  detail: string,
  activeNodes: NodeKey[],
  activeEdge?: { from: NodeKey; to: NodeKey }
): FlowStep {
  return { id, title, detail, activeNodes, activeEdge };
}
