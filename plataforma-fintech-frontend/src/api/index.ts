// HTTP client
export { apiClient, createApiClient } from './client';
export type { ApiPaths } from './client';

// Error utilities
export { apiErrorSchema, parseApiError, extractApiError } from './error';
export type { ApiError } from './error';

// Query key registry
export { queryKeys } from './query-keys';
