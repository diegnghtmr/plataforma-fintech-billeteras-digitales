import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { MemoryRouter } from 'react-router-dom';
import type { ReactNode } from 'react';
import { ChatPanel } from '../ChatPanel';

vi.mock('../hooks', () => ({
  useAiChatMutation: vi.fn(),
}));

import { useAiChatMutation } from '../hooks';

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

const MOCK_MUTATION_IDLE = {
  mutateAsync: vi.fn(),
  isPending: false,
  data: undefined,
  error: null,
  reset: vi.fn(),
  isSuccess: false,
  isError: false,
};

const DEFAULT_PROPS = {
  actorUserId: 'USR001',
  actorRole: 'USER' as const,
  scope: 'USER' as const,
  conversationId: 'conv-test-1',
};

describe('ChatPanel', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(useAiChatMutation).mockReturnValue(MOCK_MUTATION_IDLE as any);
  });

  it('renders empty-state placeholder', () => {
    render(<ChatPanel {...DEFAULT_PROPS} />, { wrapper: makeWrapper() });
    expect(screen.getByPlaceholderText(/pregunta algo/i)).toBeInTheDocument();
  });

  it('submit button is disabled when input is empty', () => {
    render(<ChatPanel {...DEFAULT_PROPS} />, { wrapper: makeWrapper() });
    const submitBtn = screen.getByRole('button', { name: /enviar/i });
    expect(submitBtn).toBeDisabled();
  });

  it('submit button is disabled when input exceeds 1000 chars', async () => {
    const user = userEvent.setup();
    render(<ChatPanel {...DEFAULT_PROPS} />, { wrapper: makeWrapper() });

    const input = screen.getByRole('textbox');
    await user.type(input, 'a'.repeat(1001));

    const submitBtn = screen.getByRole('button', { name: /enviar/i });
    expect(submitBtn).toBeDisabled();
  });

  it('shows error message when input exceeds 1000 chars', async () => {
    const user = userEvent.setup();
    render(<ChatPanel {...DEFAULT_PROPS} />, { wrapper: makeWrapper() });

    const input = screen.getByRole('textbox');
    await user.type(input, 'a'.repeat(1001));

    expect(screen.getByText(/1000 caracteres/i)).toBeInTheDocument();
  });

  it('submit button is enabled when input has valid content', async () => {
    const user = userEvent.setup();
    render(<ChatPanel {...DEFAULT_PROPS} />, { wrapper: makeWrapper() });

    const input = screen.getByRole('textbox');
    await user.type(input, 'Hola');

    const submitBtn = screen.getByRole('button', { name: /enviar/i });
    expect(submitBtn).not.toBeDisabled();
  });

  it('after submit shows the assistant answer text', async () => {
    const user = userEvent.setup();
    const mockMutateAsync = vi.fn().mockResolvedValue({
      conversationId: 'conv-test-1',
      intent: 'EXPLAIN_BALANCE_CHANGE',
      answer: 'Your balance changed this week.',
      suggestedAction: null,
      usedContext: { sources: ['WALLET_SNAPSHOT'], scope: 'USER' },
    });
    vi.mocked(useAiChatMutation).mockReturnValue({
      ...MOCK_MUTATION_IDLE,
      mutateAsync: mockMutateAsync,
    } as any);

    render(<ChatPanel {...DEFAULT_PROPS} />, { wrapper: makeWrapper() });

    await user.type(screen.getByRole('textbox'), 'mi saldo');
    await user.click(screen.getByRole('button', { name: /enviar/i }));

    await waitFor(() => {
      expect(screen.getByText('Your balance changed this week.')).toBeInTheDocument();
    });
  });

  it('renders suggested-action buttons when API returns them', async () => {
    const user = userEvent.setup();
    const mockMutateAsync = vi.fn().mockResolvedValue({
      conversationId: 'conv-test-1',
      intent: 'EXPLAIN_BALANCE_CHANGE',
      answer: 'Your balance changed.',
      suggestedAction: {
        type: 'DRAFT_TRANSFER',
        label: 'Iniciar transferencia',
        requiresConfirmation: true,
        missingFields: [],
      },
      usedContext: { sources: ['WALLET_SNAPSHOT'], scope: 'USER' },
    });
    vi.mocked(useAiChatMutation).mockReturnValue({
      ...MOCK_MUTATION_IDLE,
      mutateAsync: mockMutateAsync,
    } as any);

    render(<ChatPanel {...DEFAULT_PROPS} />, { wrapper: makeWrapper() });

    await user.type(screen.getByRole('textbox'), 'transferir');
    await user.click(screen.getByRole('button', { name: /enviar/i }));

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /iniciar transferencia/i })).toBeInTheDocument();
    });
  });

  it('shows requiresConfirmation banner when suggestedAction has requiresConfirmation=true', async () => {
    const user = userEvent.setup();
    const mockMutateAsync = vi.fn().mockResolvedValue({
      conversationId: 'conv-test-1',
      intent: 'DRAFT_TRANSFER',
      answer: 'Podés hacer la transferencia.',
      suggestedAction: {
        type: 'DRAFT_TRANSFER',
        label: 'Confirmar transferencia',
        requiresConfirmation: true,
        missingFields: [],
      },
      usedContext: { sources: ['WALLET_SNAPSHOT'], scope: 'USER' },
    });
    vi.mocked(useAiChatMutation).mockReturnValue({
      ...MOCK_MUTATION_IDLE,
      mutateAsync: mockMutateAsync,
    } as any);

    render(<ChatPanel {...DEFAULT_PROPS} />, { wrapper: makeWrapper() });

    await user.type(screen.getByRole('textbox'), 'transferir dinero');
    await user.click(screen.getByRole('button', { name: /enviar/i }));

    await waitFor(() => {
      expect(screen.getByText(/necesita confirmación/i)).toBeInTheDocument();
    });
  });

  it('shows AI_UNAVAILABLE fallback when API returns 503', async () => {
    const user = userEvent.setup();
    const mockMutateAsync = vi.fn().mockRejectedValue({
      code: 'AI_UNAVAILABLE',
      message: 'AI service is unavailable',
    });
    vi.mocked(useAiChatMutation).mockReturnValue({
      ...MOCK_MUTATION_IDLE,
      mutateAsync: mockMutateAsync,
    } as any);

    render(<ChatPanel {...DEFAULT_PROPS} />, { wrapper: makeWrapper() });

    await user.type(screen.getByRole('textbox'), 'pregunta');
    await user.click(screen.getByRole('button', { name: /enviar/i }));

    await waitFor(() => {
      expect(screen.getByText(/ia no disponible/i)).toBeInTheDocument();
    });
  });

  it('shows AI_MESSAGE_TOO_LONG error when API returns 400', async () => {
    const user = userEvent.setup();
    const mockMutateAsync = vi.fn().mockRejectedValue({
      code: 'AI_MESSAGE_TOO_LONG',
      message: 'Message exceeds maximum length',
    });
    vi.mocked(useAiChatMutation).mockReturnValue({
      ...MOCK_MUTATION_IDLE,
      mutateAsync: mockMutateAsync,
    } as any);

    render(<ChatPanel {...DEFAULT_PROPS} />, { wrapper: makeWrapper() });

    await user.type(screen.getByRole('textbox'), 'mensaje largo');
    await user.click(screen.getByRole('button', { name: /enviar/i }));

    await waitFor(() => {
      expect(screen.getByText(/1000 caracteres/i)).toBeInTheDocument();
    });
  });

  it('textarea has accessible label', () => {
    render(<ChatPanel {...DEFAULT_PROPS} />, { wrapper: makeWrapper() });
    const textarea = screen.getByRole('textbox');
    expect(textarea).toHaveAttribute('aria-label');
  });

  it('message list has role=log and aria-live=polite', () => {
    render(<ChatPanel {...DEFAULT_PROPS} />, { wrapper: makeWrapper() });
    const log = screen.getByRole('log');
    expect(log).toHaveAttribute('aria-live', 'polite');
  });
});
