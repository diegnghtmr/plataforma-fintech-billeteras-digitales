import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { MemoryRouter } from 'react-router-dom';
import type { ReactNode } from 'react';
import { AiChatPage } from '../AiChatPage';

vi.mock('../ChatPanel', () => ({
  ChatPanel: (props: Record<string, unknown>) => (
    <div data-testid="chat-panel" data-scope={props.scope} data-actor-user-id={props.actorUserId} />
  ),
}));

function makeWrapper() {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return function Wrapper({ children }: { children: ReactNode }) {
    return (
      <MemoryRouter>
        <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
      </MemoryRouter>
    );
  };
}

describe('AiChatPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders page title', () => {
    render(<AiChatPage />, { wrapper: makeWrapper() });
    expect(screen.getByRole('heading', { name: /asistente ia/i })).toBeInTheDocument();
  });

  it('renders ChatPanel', () => {
    render(<AiChatPage />, { wrapper: makeWrapper() });
    expect(screen.getByTestId('chat-panel')).toBeInTheDocument();
  });

  it('renders explainer paragraph', () => {
    render(<AiChatPage />, { wrapper: makeWrapper() });
    expect(screen.getByText(/pregunt/i)).toBeInTheDocument();
  });

  it('renders scope selector', () => {
    render(<AiChatPage />, { wrapper: makeWrapper() });
    expect(screen.getByRole('combobox')).toBeInTheDocument();
  });

  it('scope selector shows USER_FINANCIAL_HEALTH option', () => {
    render(<AiChatPage />, { wrapper: makeWrapper() });
    expect(screen.getByRole('option', { name: /salud financiera/i })).toBeInTheDocument();
  });
});
