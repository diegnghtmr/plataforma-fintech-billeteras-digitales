import { describe, expect, it } from 'vitest';
import { buildFlowSteps } from '../transferFlowSteps';
import type { TransactionResponse } from '../../../api/transactions';

function makeTx(overrides: Partial<TransactionResponse> = {}): TransactionResponse {
  return {
    id: 'TX-1',
    timestamp: '2026-01-01T00:00:00Z',
    type: 'RECHARGE',
    amount: 100,
    sourceWalletId: 'W001',
    targetWalletId: null,
    sourceUserId: 'USR001',
    targetUserId: null,
    status: 'SUCCESSFUL',
    pointsGenerated: 5,
    description: null,
    reversible: true,
    riskLevel: null,
    ...overrides,
  } as TransactionResponse;
}

describe('buildFlowSteps', () => {
  it('emits 7 steps for RECHARGE ending in notification', () => {
    const steps = buildFlowSteps(makeTx({ type: 'RECHARGE' }));
    expect(steps).toHaveLength(7);
    expect(steps[0]?.activeNodes).toContain('sourceUser');
    expect(steps.at(-1)?.activeNodes).toContain('notification');
  });

  it('emits 9 steps for INTERNAL_TRANSFER including target wallet', () => {
    const steps = buildFlowSteps(
      makeTx({ type: 'INTERNAL_TRANSFER', targetWalletId: 'W002' })
    );
    expect(steps).toHaveLength(9);
    expect(steps.flatMap((s) => s.activeNodes)).toContain('targetWallet');
  });

  it('includes the GrafoTransferencias step only for external transfers', () => {
    const internal = buildFlowSteps(makeTx({ type: 'INTERNAL_TRANSFER', targetWalletId: 'W002' }));
    const external = buildFlowSteps(
      makeTx({
        type: 'EXTERNAL_TRANSFER_SENT',
        targetUserId: 'USR002',
        targetWalletId: 'W002',
      })
    );

    expect(internal.some((s) => s.activeNodes.includes('graph'))).toBe(false);
    expect(external.some((s) => s.activeNodes.includes('graph'))).toBe(true);
  });

  it('marks fraud step with the riskLevel from the transaction when present', () => {
    const steps = buildFlowSteps(makeTx({ type: 'WITHDRAWAL', riskLevel: 'HIGH' }));
    const fraudStep = steps.find((s) => s.id === 'fraud');
    expect(fraudStep?.detail).toContain('HIGH');
  });
});
