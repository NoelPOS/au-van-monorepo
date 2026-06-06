import { useQuery } from "@tanstack/react-query";
import { getAdminBookings, type AdminBookingsQuery } from "@/features/bookings/api";
import { bookingKeys } from "@/lib/queryKeys";

export function useAdminBookings(query: AdminBookingsQuery = {}) {
  return useQuery({
    queryKey: bookingKeys.admin(query.page ?? 0, query.size ?? 20),
    queryFn: () => getAdminBookings(query),
  });
}
