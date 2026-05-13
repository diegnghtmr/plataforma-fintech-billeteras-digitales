import { lazy, Suspense } from 'react';
import type React from 'react';
import { createBrowserRouter } from 'react-router-dom';
import { AppLayout } from '../shared/layout/AppLayout';
import { SkeletonPage } from '../shared/components/Skeleton';

const HomePage = lazy(() =>
  import('../features/home/HomePage').then((m) => ({ default: m.HomePage }))
);
const UsersPage = lazy(() =>
  import('../features/users/UsersPage').then((m) => ({ default: m.UsersPage }))
);
const WalletsPage = lazy(() =>
  import('../features/wallets/WalletsPage').then((m) => ({ default: m.WalletsPage }))
);
const OperationsPage = lazy(() =>
  import('../features/operations/OperationsPage').then((m) => ({ default: m.OperationsPage }))
);
const TransactionsPage = lazy(() =>
  import('../features/transactions/TransactionsPage').then((m) => ({ default: m.TransactionsPage }))
);
const PointsPage = lazy(() =>
  import('../features/points/PointsPage').then((m) => ({ default: m.PointsPage }))
);
const ScheduledOperationsPage = lazy(() =>
  import('../features/scheduled-operations/ScheduledOperationsPage').then((m) => ({
    default: m.ScheduledOperationsPage,
  }))
);
const NotificationsPage = lazy(() =>
  import('../features/notifications/NotificationsPage').then((m) => ({
    default: m.NotificationsPage,
  }))
);
const AnalyticsPage = lazy(() =>
  import('../features/analytics/AnalyticsPage').then((m) => ({ default: m.AnalyticsPage }))
);
const FraudPage = lazy(() =>
  import('../features/fraud/FraudPage').then((m) => ({ default: m.FraudPage }))
);

function withSkeleton(element: React.ReactElement) {
  return <Suspense fallback={<SkeletonPage />}>{element}</Suspense>;
}

export const router = createBrowserRouter(
  [
    {
      path: '/',
      element: <AppLayout />,
      children: [
        { index: true, element: withSkeleton(<HomePage />) },
        { path: 'users', element: withSkeleton(<UsersPage />) },
        { path: 'wallets', element: withSkeleton(<WalletsPage />) },
        { path: 'operations', element: withSkeleton(<OperationsPage />) },
        { path: 'transactions', element: withSkeleton(<TransactionsPage />) },
        { path: 'points', element: withSkeleton(<PointsPage />) },
        { path: 'scheduled', element: withSkeleton(<ScheduledOperationsPage />) },
        { path: 'notifications', element: withSkeleton(<NotificationsPage />) },
        { path: 'analytics', element: withSkeleton(<AnalyticsPage />) },
        { path: 'fraud', element: withSkeleton(<FraudPage />) },
      ],
    },
  ],
  {
    future: {
      v7_relativeSplatPath: true,
      v7_fetcherPersist: true,
      v7_normalizeFormMethod: true,
      v7_partialHydration: true,
      v7_skipActionErrorRevalidation: true,
    },
  }
);
