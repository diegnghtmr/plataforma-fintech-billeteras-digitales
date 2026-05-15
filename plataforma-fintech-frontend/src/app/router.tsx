import { createBrowserRouter } from 'react-router-dom';
import { AppLayout } from '../shared/layout/AppLayout';

export const router = createBrowserRouter(
  [
    {
      path: '/',
      element: <AppLayout />,
      children: [
        {
          index: true,
          lazy: () =>
            import('../features/home/HomePage').then((m) => ({ Component: m.HomePage })),
        },
        {
          path: 'users',
          lazy: () =>
            import('../features/users/UsersPage').then((m) => ({ Component: m.UsersPage })),
        },
        {
          path: 'wallets',
          lazy: () =>
            import('../features/wallets/WalletsPage').then((m) => ({ Component: m.WalletsPage })),
        },
        {
          path: 'operations',
          lazy: () =>
            import('../features/operations/OperationsPage').then((m) => ({
              Component: m.OperationsPage,
            })),
        },
        {
          path: 'transactions',
          lazy: () =>
            import('../features/transactions/TransactionsPage').then((m) => ({
              Component: m.TransactionsPage,
            })),
        },
        {
          path: 'transactions/:transactionId/flow',
          lazy: () =>
            import('../features/transactions/TransferFlowPage').then((m) => ({
              Component: m.TransferFlowPage,
            })),
        },
        {
          path: 'points',
          lazy: () =>
            import('../features/points/PointsPage').then((m) => ({ Component: m.PointsPage })),
        },
        {
          path: 'scheduled',
          lazy: () =>
            import('../features/scheduled-operations/ScheduledOperationsPage').then((m) => ({
              Component: m.ScheduledOperationsPage,
            })),
        },
        {
          path: 'notifications',
          lazy: () =>
            import('../features/notifications/NotificationsPage').then((m) => ({
              Component: m.NotificationsPage,
            })),
        },
        {
          path: 'analytics',
          lazy: () =>
            import('../features/analytics/AnalyticsPage').then((m) => ({
              Component: m.AnalyticsPage,
            })),
        },
        {
          path: 'fraud',
          lazy: () =>
            import('../features/fraud/FraudPage').then((m) => ({ Component: m.FraudPage })),
        },
        {
          path: 'ai',
          lazy: () =>
            import('../features/ai-chat/AiChatPage').then((m) => ({
              Component: m.AiChatPage,
            })),
        },
      ],
    },
  ],
  {
    future: {
      v7_relativeSplatPath: true,
      v7_fetcherPersist: true,
      v7_normalizeFormMethod: true,
      v7_skipActionErrorRevalidation: true,
    },
  }
);
