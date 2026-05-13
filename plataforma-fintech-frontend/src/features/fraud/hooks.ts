import { useQuery } from '@tanstack/react-query';
import { getFraudEvents } from '../../api/fraud';
import type { FraudFilters } from '../../api/fraud';
import { queryKeys } from '../../api/query-keys';

export function useFraudEventsQuery(filters?: FraudFilters) {
  return useQuery({
    queryKey: queryKeys.fraud.events(filters as Record<string, unknown> | undefined),
    queryFn: () => getFraudEvents(filters),
  });
}
