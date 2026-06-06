import { useMutation, useQueryClient } from "@tanstack/react-query";
import { cancelBooking } from "@/features/bookings/api";
import { bookingKeys } from "@/lib/queryKeys";

export function useCancelBooking() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (bookingId: string) => cancelBooking(bookingId),
    onSuccess: async (_, bookingId) => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: bookingKeys.me() }),
        queryClient.invalidateQueries({ queryKey: bookingKeys.detail(bookingId) }),
        queryClient.invalidateQueries({ queryKey: bookingKeys.adminAll() }),
      ]);
    },
  });
}
