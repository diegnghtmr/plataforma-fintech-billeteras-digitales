import createClient from 'openapi-fetch';
import type { paths } from './generated/schema';

export type ApiPaths = paths;

/**
 * Factory that creates a typed openapi-fetch client.
 * Reading import.meta.env.VITE_API_BASE_URL inside the function (not at module level)
 * ensures vi.stubEnv works correctly in Vitest — stubs are applied before the
 * function body executes, whereas module-level constants are captured at load time.
 */
export function createApiClient() {
  const baseUrl = import.meta.env['VITE_API_BASE_URL'] ?? 'http://localhost:8080/api/v1';
  return createClient<paths>({ baseUrl });
}

/**
 * Default singleton client for use throughout the application.
 * Uses VITE_API_BASE_URL from the build-time environment, falling back to localhost.
 */
export const apiClient = createApiClient();
