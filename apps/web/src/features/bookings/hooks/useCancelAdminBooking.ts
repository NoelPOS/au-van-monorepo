import { useMutation, useQueryClient } from "@tanstack/react-query";
import { cancelAdminBooking } from "@/features/bookings/api";
import { bookingKeys } from "@/lib/queryKeys";

export function useCancelAdminBooking() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (bookingId: string) => cancelAdminBooking(bookingId),
    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: bookingKeys.adminAll() }),
        queryClient.invalidateQueries({ queryKey: bookingKeys.me() }),
      ]);
    },
  });
}
