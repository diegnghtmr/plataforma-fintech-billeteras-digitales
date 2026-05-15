import { useMutation, useQuery } from '@tanstack/react-query';
import { aiChat, aiActionDraft, explainFraud } from '../../api/ai';
import type { AiChatRequest, AiActionDraftRequest } from '../../api/ai';

/**
 * Mutation hook for POST /ai/chat.
 * Returns { mutateAsync, isPending, data, error, reset }.
 */
export function useAiChatMutation() {
  return useMutation({
    mutationFn: (body: AiChatRequest) => aiChat(body),
  });
}

/**
 * Query hook for GET /ai/fraud-events/{id}/explain.
 * Only fetches when enabled=true and id is non-empty.
 */
export function useExplainFraudQuery(
  id: string | undefined,
  actorRole: 'USER' | 'ADMIN',
  actorUserId: string,
  enabled: boolean = false,
) {
  return useQuery({
    queryKey: ['ai', 'fraud-explain', id ?? ''],
    queryFn: () => explainFraud(id!, actorRole, actorUserId),
    enabled: enabled && !!id,
  });
}

/**
 * Mutation hook for POST /ai/action-draft.
 */
export function useAiActionDraftMutation() {
  return useMutation({
    mutationFn: (body: AiActionDraftRequest) => aiActionDraft(body),
  });
}
