import { useMutation, useQueryClient } from "@tanstack/react-query";
import {
  updateAdminTimeslot,
  type UpdateAdminTimeslotInput,
} from "@/features/timeslots/api";

export function useUpdateAdminTimeslot() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (input: UpdateAdminTimeslotInput) => updateAdminTimeslot(input),
    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ["admin", "timeslots"] }),
        queryClient.invalidateQueries({ queryKey: ["timeslots"] }),
      ]);
    },
  });
}
