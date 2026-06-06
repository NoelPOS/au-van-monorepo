import { useMutation, useQueryClient } from "@tanstack/react-query";
import { rescheduleBooking, type RescheduleBookingInput } from "@/features/bookings/api";
import { bookingKeys } from "@/lib/queryKeys";

export function useRescheduleBooking() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (input: RescheduleBookingInput) => rescheduleBooking(input),
    onSuccess: async (booking, variables) => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: bookingKeys.me() }),
        queryClient.invalidateQueries({ queryKey: bookingKeys.detail(variables.bookingId) }),
        queryClient.invalidateQueries({ queryKey: bookingKeys.detail(booking.id) }),
        queryClient.invalidateQueries({ queryKey: bookingKeys.adminAll() }),
      ]);
    },
  });
}
