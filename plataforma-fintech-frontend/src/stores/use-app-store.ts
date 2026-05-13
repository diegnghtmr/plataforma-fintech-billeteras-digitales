import { create } from 'zustand';
import type { TransactionType, TransactionStatus } from '../api/transactions';

export type { TransactionType, TransactionStatus };

export interface TransactionFilters {
  type?: TransactionType;
  status?: TransactionStatus;
  walletId?: string;
}

interface AppState {
  transactionFilters: TransactionFilters;
  setTransactionFilters: (partial: Partial<TransactionFilters>) => void;
  resetFilters: () => void;
}

/**
 * Application-level store for UI state (filters, selections).
 * Server state (data) MUST NOT be duplicated here — lives in TanStack Query.
 */
export const useAppStore = create<AppState>((set) => ({
  transactionFilters: {},

  setTransactionFilters: (partial) =>
    set((state) => ({
      transactionFilters: { ...state.transactionFilters, ...partial },
    })),

  resetFilters: () => set({ transactionFilters: {} }),
}));
