import { useQuery } from "@tanstack/react-query";
import { getMyBookings } from "@/features/bookings/api";
import { bookingKeys } from "@/lib/queryKeys";

export function useMyBookings() {
  return useQuery({
    queryKey: bookingKeys.me(),
    queryFn: getMyBookings,
  });
}
