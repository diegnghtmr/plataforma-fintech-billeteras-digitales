import { describe, it, expect, beforeEach } from 'vitest';
import { useAppStore } from '../use-app-store';

describe('useAppStore — transactionFilters slice', () => {
  beforeEach(() => {
    // Reset store state before each test
    useAppStore.setState({
      transactionFilters: {},
    });
  });

  it('initial state has empty filters', () => {
    const filters = useAppStore.getState().transactionFilters;
    expect(filters).toEqual({});
  });

  it('setTransactionFilters sets type', () => {
    useAppStore.getState().setTransactionFilters({ type: 'RECHARGE' });
    expect(useAppStore.getState().transactionFilters.type).toBe('RECHARGE');
  });

  it('setTransactionFilters sets status', () => {
    useAppStore.getState().setTransactionFilters({ status: 'SUCCESSFUL' });
    expect(useAppStore.getState().transactionFilters.status).toBe('SUCCESSFUL');
  });

  it('setTransactionFilters replaces previous state (omitted keys are cleared)', () => {
    useAppStore.getState().setTransactionFilters({ type: 'RECHARGE', status: 'SUCCESSFUL' });
    useAppStore.getState().setTransactionFilters({ status: 'SUCCESSFUL' });

    const filters = useAppStore.getState().transactionFilters;
    expect(filters.type).toBeUndefined();
    expect(filters.status).toBe('SUCCESSFUL');
  });

  it('setTransactionFilters can update walletId', () => {
    useAppStore.getState().setTransactionFilters({ walletId: 'W001' });
    expect(useAppStore.getState().transactionFilters.walletId).toBe('W001');
  });

  it('resetFilters returns to empty state', () => {
    useAppStore.getState().setTransactionFilters({ type: 'RECHARGE', status: 'SUCCESSFUL' });
    useAppStore.getState().resetFilters();

    const filters = useAppStore.getState().transactionFilters;
    expect(filters).toEqual({});
  });

  it('resetFilters does not affect non-filter state', () => {
    // Set some filter first
    useAppStore.getState().setTransactionFilters({ type: 'RECHARGE' });
    useAppStore.getState().resetFilters();

    // Filters back to empty
    expect(useAppStore.getState().transactionFilters).toEqual({});
  });
});
