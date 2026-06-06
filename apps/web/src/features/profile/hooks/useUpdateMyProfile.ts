import { useMutation, useQueryClient } from "@tanstack/react-query";
import { updateMyProfile, type UpdateProfileInput } from "@/features/profile/api";

export function useUpdateMyProfile() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (input: UpdateProfileInput) => updateMyProfile(input),
    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ["profile", "me"] }),
        queryClient.invalidateQueries({ queryKey: ["bookings", "me"] }),
      ]);
    },
  });
}
