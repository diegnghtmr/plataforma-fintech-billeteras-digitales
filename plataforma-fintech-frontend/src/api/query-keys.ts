/**
 * Centralised query key registry for TanStack Query.
 * All keys are deterministic given the same inputs — factories return the same
 * array structure so cache invalidation is predictable.
 * Arrays use `as const` where possible for maximum type narrowing.
 */
export const queryKeys = {
  users: {
    all: ['users'] as const,
    detail: (userId: string) => ['users', userId] as const,
  },

  wallets: {
    byUser: (userId: string) => ['wallets', userId] as const,
    detail: (userId: string, walletId: string) =>
      ['wallets', userId, walletId] as const,
  },

  transactions: {
    byUser: (userId: string, filters?: Record<string, unknown>) =>
      ['transactions', 'user', userId, filters ?? {}] as const,
    byWallet: (userId: string, walletId: string) =>
      ['transactions', 'wallet', userId, walletId] as const,
    detail: (transactionId: string) =>
      ['transactions', 'detail', transactionId] as const,
  },

  points: {
    byUser: (userId: string) => ['points', userId] as const,
    ranking: (limit?: number) => ['points', 'ranking', limit ?? 10] as const,
  },

  notifications: {
    byUser: (userId: string, unreadOnly: boolean = false) =>
      ['notifications', 'byUser', userId, unreadOnly] as const,
  },

  scheduledOperations: {
    all: ['scheduledOperations'] as const,
  },

  analytics: {
    summary: ['analytics', 'summary'] as const,
    topUsers: ['analytics', 'topUsers'] as const,
    topWallets: ['analytics', 'topWallets'] as const,
    frequentRoutes: ['analytics', 'frequentRoutes'] as const,
    topTransactions: (limit: number) => ['analytics', 'topTransactions', limit] as const,
    cycles: ['analytics', 'cycles'] as const,
    topWalletCategories: (limit: number) => ['analytics', 'topWalletCategories', limit] as const,
    movementByType: ['analytics', 'movementByType'] as const,
    totalMoved: (from: string, to: string) => ['analytics', 'totalMoved', from, to] as const,
  },

  fraud: {
    events: (filters?: Record<string, unknown>) =>
      ['fraud', 'events', filters ?? {}] as const,
  },

  ai: {
    all: ['ai'] as const,
    chat: (conversationId: string, scope: string) =>
      ['ai', 'chat', conversationId, scope] as const,
    fraudExplain: (id: string) => ['ai', 'fraud-explain', id] as const,
    actionDraft: () => ['ai', 'action-draft'] as const,
  },
} as const;
