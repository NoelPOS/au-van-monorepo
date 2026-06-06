import { useMutation, useQueryClient } from "@tanstack/react-query";
import {
  createAdminTimeslot,
  type CreateAdminTimeslotInput,
} from "@/features/timeslots/api";

export function useCreateAdminTimeslot() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (input: CreateAdminTimeslotInput) => createAdminTimeslot(input),
    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ["admin", "timeslots"] }),
        queryClient.invalidateQueries({ queryKey: ["timeslots"] }),
      ]);
    },
  });
}
