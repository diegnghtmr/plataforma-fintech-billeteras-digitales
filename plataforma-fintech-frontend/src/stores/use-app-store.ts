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
  setTransactionFilters: (next: TransactionFilters) => void;
  resetFilters: () => void;
}

export const useAppStore = create<AppState>((set) => ({
  transactionFilters: {},

  setTransactionFilters: (next) => set({ transactionFilters: next }),

  resetFilters: () => set({ transactionFilters: {} }),
}));
