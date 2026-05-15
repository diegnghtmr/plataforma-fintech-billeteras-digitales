import { apiClient, extractApiError } from './index';
import type { components } from './generated/schema';

export type AiChatRequest = components['schemas']['AiChatRequest'];
export type AiChatResponse = components['schemas']['AiChatResponse'];
export type AiActionDraftRequest = components['schemas']['AiActionDraftRequest'];
export type AiActionDraftResponse = components['schemas']['AiActionDraftResponse'];
export type AiFraudExplanationResponse = components['schemas']['AiFraudExplanationResponse'];
export type AiSuggestedAction = components['schemas']['AiSuggestedAction'];
export type AiUsedContext = components['schemas']['AiUsedContext'];
export type AiIntent = components['schemas']['AiIntent'];
export type AiUnavailableError = components['schemas']['AiUnavailableError'];

export async function aiChat(body: AiChatRequest): Promise<AiChatResponse> {
  const { data, error, response } = await apiClient.POST('/ai/chat', { body });

  if (error !== undefined || !data) {
    const apiError = await extractApiError(response);
    throw apiError ?? new Error('Unknown error in AI chat');
  }

  return data;
}

export async function explainFraud(
  fraudEventId: string,
  actorRole: 'USER' | 'ADMIN',
  actorUserId: string,
): Promise<AiFraudExplanationResponse> {
  const { data, error, response } = await apiClient.GET(
    '/ai/fraud-events/{fraudEventId}/explain',
    {
      params: {
        path: { fraudEventId },
        query: { actorRole, actorUserId },
      },
    },
  );

  if (error !== undefined || !data) {
    const apiError = await extractApiError(response);
    throw apiError ?? new Error('Unknown error explaining fraud event');
  }

  return data;
}

export async function aiActionDraft(body: AiActionDraftRequest): Promise<AiActionDraftResponse> {
  const { data, error, response } = await apiClient.POST('/ai/action-draft', { body });

  if (error !== undefined || !data) {
    const apiError = await extractApiError(response);
    throw apiError ?? new Error('Unknown error in AI action draft');
  }

  return data;
}
