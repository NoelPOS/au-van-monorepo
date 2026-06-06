import { useMutation, useQueryClient } from "@tanstack/react-query";
import { cancelAdminTimeslot } from "@/features/timeslots/api";

export function useCancelAdminTimeslot() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (timeslotId: string) => cancelAdminTimeslot(timeslotId),
    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ["admin", "timeslots"] }),
        queryClient.invalidateQueries({ queryKey: ["timeslots"] }),
      ]);
    },
  });
}
