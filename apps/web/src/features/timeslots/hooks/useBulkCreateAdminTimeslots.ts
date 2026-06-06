import { useMutation, useQueryClient } from "@tanstack/react-query";
import {
  bulkCreateAdminTimeslots,
  type BulkCreateAdminTimeslotsInput,
} from "@/features/timeslots/api";

export function useBulkCreateAdminTimeslots() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (input: BulkCreateAdminTimeslotsInput) => bulkCreateAdminTimeslots(input),
    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ["admin", "timeslots"] }),
        queryClient.invalidateQueries({ queryKey: ["timeslots"] }),
      ]);
    },
  });
}
