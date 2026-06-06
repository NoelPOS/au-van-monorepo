import { useMutation, useQueryClient } from "@tanstack/react-query";
import { createBooking, type CreateBookingInput } from "@/features/bookings/api";
import { bookingKeys } from "@/lib/queryKeys";

export function useCreateBooking() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (input: CreateBookingInput) => createBooking(input),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: bookingKeys.me() });
    },
  });
}
