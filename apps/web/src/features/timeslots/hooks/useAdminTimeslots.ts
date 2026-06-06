import { useQuery } from "@tanstack/react-query";
import { getAdminTimeslots } from "@/features/timeslots/api";

export function useAdminTimeslots(page = 0, size = 50) {
  return useQuery({
    queryKey: ["admin", "timeslots", page, size],
    queryFn: () => getAdminTimeslots(page, size),
  });
}
