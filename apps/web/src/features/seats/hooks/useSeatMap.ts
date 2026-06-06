import { useQuery } from "@tanstack/react-query";
import { getSeatMap } from "@/features/seats/api";

export function useSeatMap(timeslotId: string) {
  return useQuery({
    queryKey: ["seat-map", timeslotId],
    queryFn: () => getSeatMap(timeslotId),
    enabled: Boolean(timeslotId),
  });
}
