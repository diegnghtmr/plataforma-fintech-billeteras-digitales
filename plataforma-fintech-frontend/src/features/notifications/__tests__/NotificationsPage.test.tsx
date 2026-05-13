import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import type { ReactNode } from 'react';
import { NotificationsPage } from '../NotificationsPage';

vi.mock('../hooks', () => ({
  useUserNotificationsQuery: vi.fn(),
  useMarkNotificationReadMutation: vi.fn(),
}));

vi.mock('../../../stores/use-selection-store', () => ({
  useSelectionStore: vi.fn(),
}));

import {
  useUserNotificationsQuery,
  useMarkNotificationReadMutation,
} from '../hooks';
import { useSelectionStore } from '../../../stores/use-selection-store';

const MOCK_READ_NOTIF = {
  id: 'NTF-000001',
  userId: 'USR001',
  type: 'SYSTEM',
  severity: 'INFO',
  title: 'Read notification',
  message: 'This is read',
  read: true,
  createdAt: '2026-01-01T00:00:00Z',
};

const MOCK_UNREAD_1 = {
  id: 'NTF-000002',
  userId: 'USR001',
  type: 'TRANSACTION',
  severity: 'WARNING',
  title: 'Unread 1',
  message: 'Unread message 1',
  read: false,
  createdAt: '2026-01-02T00:00:00Z',
};

const MOCK_UNREAD_2 = {
  id: 'NTF-000003',
  userId: 'USR001',
  type: 'FRAUD_ALERT',
  severity: 'CRITICAL',
  title: 'Unread 2',
  message: 'Unread message 2',
  read: false,
  createdAt: '2026-01-03T00:00:00Z',
};

function makeWrapper() {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return ({ children }: { children: ReactNode }) => (
    <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
  );
}

describe('NotificationsPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(useMarkNotificationReadMutation).mockReturnValue({
      mutate: vi.fn(),
      isPending: false,
    } as any);
  });

  it('shows empty state when no user is selected', () => {
    vi.mocked(useSelectionStore).mockReturnValue(null);
    vi.mocked(useUserNotificationsQuery).mockReturnValue({
      data: undefined,
      isLoading: false,
    } as any);

    render(<NotificationsPage />, { wrapper: makeWrapper() });

    expect(screen.getByText(/selecciona un usuario/i)).toBeInTheDocument();
  });

  it('renders all notifications when user is selected', () => {
    vi.mocked(useSelectionStore).mockReturnValue('USR001');
    vi.mocked(useUserNotificationsQuery).mockReturnValue({
      data: [MOCK_READ_NOTIF, MOCK_UNREAD_1, MOCK_UNREAD_2],
      isLoading: false,
    } as any);

    render(<NotificationsPage />, { wrapper: makeWrapper() });

    expect(screen.getByText('Read notification')).toBeInTheDocument();
    expect(screen.getByText('Unread 1')).toBeInTheDocument();
    expect(screen.getByText('Unread 2')).toBeInTheDocument();
  });

  // S-FE-2: toggle "solo no leídas"
  it('toggling solo-no-leidas filter re-queries with unreadOnly=true', () => {
    vi.mocked(useSelectionStore).mockReturnValue('USR001');
    vi.mocked(useUserNotificationsQuery).mockReturnValue({
      data: [MOCK_UNREAD_1, MOCK_UNREAD_2],
      isLoading: false,
    } as any);

    render(<NotificationsPage />, { wrapper: makeWrapper() });

    const toggle = screen.getByRole('checkbox', { name: /solo no leídas/i });
    fireEvent.click(toggle);

    expect(useUserNotificationsQuery).toHaveBeenCalledWith('USR001', true);
  });

  it('mark-as-read button calls mutation', () => {
    const mutateFn = vi.fn();
    vi.mocked(useSelectionStore).mockReturnValue('USR001');
    vi.mocked(useUserNotificationsQuery).mockReturnValue({
      data: [MOCK_UNREAD_1],
      isLoading: false,
    } as any);
    vi.mocked(useMarkNotificationReadMutation).mockReturnValue({
      mutate: mutateFn,
      isPending: false,
    } as any);

    render(<NotificationsPage />, { wrapper: makeWrapper() });

    const markReadBtn = screen.getByRole('button', { name: /marcar como leída/i });
    fireEvent.click(markReadBtn);

    expect(mutateFn).toHaveBeenCalledWith('NTF-000002');
  });
});
