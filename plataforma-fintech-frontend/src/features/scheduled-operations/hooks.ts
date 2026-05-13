import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  getScheduledOperations,
  createScheduledOperation,
  cancelScheduledOperation,
  runScheduledOps,
} from '../../api/scheduled-operations';
import { queryKeys } from '../../api/query-keys';
import type { CreateScheduledOperationRequest } from '../../api/scheduled-operations';

export function useScheduledOperationsQuery() {
  return useQuery({
    queryKey: queryKeys.scheduledOperations.all,
    queryFn: getScheduledOperations,
  });
}

export function useCreateScheduledOperationMutation() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (body: CreateScheduledOperationRequest) => createScheduledOperation(body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.scheduledOperations.all });
    },
  });
}

export function useCancelScheduledOperationMutation() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (operationId: string) => cancelScheduledOperation(operationId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.scheduledOperations.all });
    },
  });
}

export function useRunScheduledOpsMutation() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: () => runScheduledOps(),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.scheduledOperations.all });
      queryClient.invalidateQueries({ queryKey: ['notifications'] });
    },
  });
}
