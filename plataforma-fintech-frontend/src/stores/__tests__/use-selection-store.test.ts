import { describe, it, expect, beforeEach } from 'vitest';
import { useSelectionStore } from '../use-selection-store';

describe('useSelectionStore', () => {
  beforeEach(() => {
    // Reset store state before each test
    useSelectionStore.setState({ selectedUserId: null, selectedWalletId: null });
  });

  it('setSelectedUserId updates userId', () => {
    const { setSelectedUserId } = useSelectionStore.getState();
    setSelectedUserId('USR001');
    expect(useSelectionStore.getState().selectedUserId).toBe('USR001');
  });

  it('setSelectedWalletId updates walletId', () => {
    const { setSelectedWalletId } = useSelectionStore.getState();
    setSelectedWalletId('W001');
    expect(useSelectionStore.getState().selectedWalletId).toBe('W001');
  });

  it('clearSelection resets both to null', () => {
    const store = useSelectionStore.getState();
    store.setSelectedUserId('USR001');
    store.setSelectedWalletId('W001');

    useSelectionStore.getState().clearSelection();

    expect(useSelectionStore.getState().selectedUserId).toBeNull();
    expect(useSelectionStore.getState().selectedWalletId).toBeNull();
  });

  it('setSelectedUserId does not affect walletId', () => {
    const store = useSelectionStore.getState();
    store.setSelectedWalletId('W001');
    store.setSelectedUserId('USR001');

    expect(useSelectionStore.getState().selectedWalletId).toBe('W001');
  });

  it('setSelectedWalletId does not affect userId', () => {
    const store = useSelectionStore.getState();
    store.setSelectedUserId('USR001');
    store.setSelectedWalletId('W001');

    expect(useSelectionStore.getState().selectedUserId).toBe('USR001');
  });
});
