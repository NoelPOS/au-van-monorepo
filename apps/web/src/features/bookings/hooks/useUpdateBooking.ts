import { useMutation, useQueryClient } from "@tanstack/react-query";
import { updateBooking, type UpdateBookingInput } from "@/features/bookings/api";
import { bookingKeys } from "@/lib/queryKeys";

export function useUpdateBooking() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (input: UpdateBookingInput) => updateBooking(input),
    onSuccess: async (booking) => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: bookingKeys.me() }),
        queryClient.invalidateQueries({ queryKey: bookingKeys.detail(booking.id) }),
      ]);
    },
  });
}
