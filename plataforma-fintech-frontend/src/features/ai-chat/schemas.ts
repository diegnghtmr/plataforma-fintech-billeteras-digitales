import { z } from 'zod';

export const AI_SCOPE_VALUES = ['USER', 'ADMIN'] as const;
export type AiScopeValue = (typeof AI_SCOPE_VALUES)[number];

export const ACTOR_ROLE_VALUES = ['USER', 'ADMIN'] as const;
export type ActorRoleValue = (typeof ACTOR_ROLE_VALUES)[number];

/**
 * Zod schema for the AI chat form input.
 * message: 1–1000 chars (RF-AI-08)
 * scope: USER or ADMIN enum
 */
export const aiChatInputSchema = z.object({
  message: z
    .string()
    .min(1, 'El mensaje no puede estar vacío')
    .max(1000, 'El mensaje supera los 1000 caracteres'),
  scope: z.enum(AI_SCOPE_VALUES),
});

export type AiChatInput = z.infer<typeof aiChatInputSchema>;
