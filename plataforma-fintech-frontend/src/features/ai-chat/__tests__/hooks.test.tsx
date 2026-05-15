import { describe, it, expect, vi, beforeEach } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import type { ReactNode } from 'react';
import { useAiChatMutation, useAiActionDraftMutation } from '../hooks';
import { queryKeys } from '../../../api/query-keys';

vi.mock('../../../api/ai', () => ({
  aiChat: vi.fn(),
  aiActionDraft: vi.fn(),
  explainFraud: vi.fn(),
}));

import { aiChat, aiActionDraft } from '../../../api/ai';

function makeWrapper() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  });
  return function Wrapper({ children }: { children: ReactNode }) {
    return <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>;
  };
}

describe('queryKeys.ai', () => {
  it('ai.all equals ["ai"]', () => {
    expect(queryKeys.ai.all).toEqual(['ai']);
  });

  it('ai.chat includes conversationId and scope', () => {
    const key = queryKeys.ai.chat('conv-1', 'USER');
    expect(key).toEqual(['ai', 'chat', 'conv-1', 'USER']);
  });

  it('ai.fraudExplain includes the event id', () => {
    const key = queryKeys.ai.fraudExplain('fe-42');
    expect(key).toEqual(['ai', 'fraud-explain', 'fe-42']);
  });

  it('ai.actionDraft returns stable key', () => {
    expect(queryKeys.ai.actionDraft()).toEqual(['ai', 'action-draft']);
  });
});

describe('useAiChatMutation', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('exists and returns mutateAsync', () => {
    const { result } = renderHook(() => useAiChatMutation(), {
      wrapper: makeWrapper(),
    });
    expect(result.current.mutateAsync).toBeDefined();
    expect(result.current.isPending).toBe(false);
  });

  it('calls aiChat with the typed body', async () => {
    const mockResponse = {
      conversationId: 'conv-1',
      intent: 'EXPLAIN_BALANCE_CHANGE' as const,
      answer: 'Your balance changed.',
      usedContext: { sources: ['WALLET_SNAPSHOT'], scope: 'USER' as const },
      modelUsed: 'meta-llama/llama-3.3-70b-instruct:free',
      latencyMs: 420,
    };
    vi.mocked(aiChat).mockResolvedValue(mockResponse);

    const { result } = renderHook(() => useAiChatMutation(), {
      wrapper: makeWrapper(),
    });

    const body = {
      message: 'mi saldo cambio',
      actorRole: 'USER' as const,
      actorUserId: 'USR001',
      scope: 'USER' as const,
      conversationId: 'conv-1',
    };

    const response = await result.current.mutateAsync(body);

    expect(aiChat).toHaveBeenCalledWith(body);
    expect(response).toEqual(mockResponse);
  });

  it('surfaces error when aiChat rejects', async () => {
    vi.mocked(aiChat).mockRejectedValue({ code: 'AI_UNAVAILABLE', message: 'AI is down' });

    const { result } = renderHook(() => useAiChatMutation(), {
      wrapper: makeWrapper(),
    });

    await expect(
      result.current.mutateAsync({
        message: 'test',
        actorRole: 'USER',
        actorUserId: 'USR001',
        scope: 'USER',
      })
    ).rejects.toMatchObject({ code: 'AI_UNAVAILABLE' });
  });
});

describe('useAiActionDraftMutation', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('exists and returns mutateAsync', () => {
    const { result } = renderHook(() => useAiActionDraftMutation(), {
      wrapper: makeWrapper(),
    });
    expect(result.current.mutateAsync).toBeDefined();
  });

  it('calls aiActionDraft and returns response', async () => {
    const mockDraft = {
      intent: 'DRAFT_SCHEDULED_OPERATION' as const,
      draft: { amount: '50000' },
      missingFields: ['targetWalletId'],
      requiresConfirmation: true,
    };
    vi.mocked(aiActionDraft).mockResolvedValue(mockDraft);

    const { result } = renderHook(() => useAiActionDraftMutation(), {
      wrapper: makeWrapper(),
    });

    const body = {
      message: 'programa una transferencia',
      actorRole: 'USER' as const,
      actorUserId: 'USR001',
      scope: 'USER' as const,
    };

    const response = await result.current.mutateAsync(body);

    expect(aiActionDraft).toHaveBeenCalledWith(body);
    expect(response.requiresConfirmation).toBe(true);
    await waitFor(() => expect(result.current.isSuccess).toBe(true));
  });
});
