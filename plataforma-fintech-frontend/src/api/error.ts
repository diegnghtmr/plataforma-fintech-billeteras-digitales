import { z } from 'zod';

export const apiErrorSchema = z.object({
  code: z.string(),
  message: z.string(),
  details: z.array(z.string()).optional(),
});

export type ApiError = z.infer<typeof apiErrorSchema>;

/**
 * Validates an unknown value against the ApiError schema.
 * Returns a typed ApiError on success, null on failure.
 * Never throws.
 */
export function parseApiError(value: unknown): ApiError | null {
  const result = apiErrorSchema.safeParse(value);
  return result.success ? result.data : null;
}

/**
 * Reads a Response body as JSON and delegates to parseApiError.
 * Returns null if body is not valid JSON or not a valid ApiError shape.
 */
export async function extractApiError(response: Response): Promise<ApiError | null> {
  try {
    const body: unknown = await response.json();
    return parseApiError(body);
  } catch {
    return null;
  }
}
