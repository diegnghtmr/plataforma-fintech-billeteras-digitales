import { createBrowserRouter } from 'react-router-dom';
import { AppLayout } from '../shared/layout/AppLayout';

// Lazy placeholders — will be replaced with real pages in T04-F12/F14
function HomePage() {
  return <h1 className="text-canvas-fg text-2xl font-bold">Fintech Wallet</h1>;
}

// These will be replaced with real implementations
import { lazy, Suspense } from 'react';

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

export const router = createBrowserRouter([
  {
    path: '/',
    element: <AppLayout />,
    children: [
      { index: true, element: <HomePage /> },
      {
        path: 'users',
        element: (
          <Suspense fallback={<div className="text-canvas-fg">Cargando...</div>}>
            <UsersPage />
          </Suspense>
        ),
      },
      {
        path: 'wallets',
        element: (
          <Suspense fallback={<div className="text-canvas-fg">Cargando...</div>}>
            <WalletsPage />
          </Suspense>
        ),
      },
      {
        path: 'operations',
        element: (
          <Suspense fallback={<div className="text-canvas-fg">Cargando...</div>}>
            <OperationsPage />
          </Suspense>
        ),
      },
      {
        path: 'transactions',
        element: (
          <Suspense fallback={<div className="text-canvas-fg">Cargando...</div>}>
            <TransactionsPage />
          </Suspense>
        ),
      },
      {
        path: 'points',
        element: (
          <Suspense fallback={<div className="text-canvas-fg">Cargando...</div>}>
            <PointsPage />
          </Suspense>
        ),
      },
      {
        path: 'scheduled',
        element: (
          <Suspense fallback={<div className="text-canvas-fg">Cargando...</div>}>
            <ScheduledOperationsPage />
          </Suspense>
        ),
      },
      {
        path: 'notifications',
        element: (
          <Suspense fallback={<div className="text-canvas-fg">Cargando...</div>}>
            <NotificationsPage />
          </Suspense>
        ),
      },
      {
        path: 'analytics',
        element: (
          <Suspense fallback={<div className="text-canvas-fg">Cargando...</div>}>
            <AnalyticsPage />
          </Suspense>
        ),
      },
      {
        path: 'fraud',
        element: (
          <Suspense fallback={<div className="text-canvas-fg">Cargando...</div>}>
            <FraudPage />
          </Suspense>
        ),
      },
    ],
  },
], {
  future: {
    v7_relativeSplatPath: true,
    v7_fetcherPersist: true,
    v7_normalizeFormMethod: true,
    v7_partialHydration: true,
    v7_skipActionErrorRevalidation: true,
  },
});
