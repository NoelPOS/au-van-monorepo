import { useQuery } from "@tanstack/react-query";
import { getBooking } from "@/features/bookings/api";
import { bookingKeys } from "@/lib/queryKeys";

export function useBooking(bookingId: string) {
  return useQuery({
    queryKey: bookingKeys.detail(bookingId),
    queryFn: () => getBooking(bookingId),
    enabled: Boolean(bookingId),
  });
}
