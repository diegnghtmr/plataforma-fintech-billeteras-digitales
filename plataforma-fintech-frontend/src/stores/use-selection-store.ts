import { create } from 'zustand';

interface SelectionState {
  selectedUserId: string | null;
  selectedWalletId: string | null;
  setSelectedUserId: (id: string) => void;
  setSelectedWalletId: (id: string) => void;
  clearSelection: () => void;
}

/**
 * Session-only selection store.
 * NO persistence middleware — state is intentionally volatile.
 */
export const useSelectionStore = create<SelectionState>((set) => ({
  selectedUserId: null,
  selectedWalletId: null,
  setSelectedUserId: (id) => set({ selectedUserId: id }),
  setSelectedWalletId: (id) => set({ selectedWalletId: id }),
  clearSelection: () => set({ selectedUserId: null, selectedWalletId: null }),
}));
