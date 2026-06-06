import { useQuery } from "@tanstack/react-query";
import { getTimeslots } from "@/features/timeslots/api";

type UseTimeslotsParams = {
  routeId: string;
  date: string;
};

export function useTimeslots(params: UseTimeslotsParams) {
  return useQuery({
    queryKey: ["timeslots", params.routeId, params.date],
    queryFn: () => getTimeslots(params),
    enabled: Boolean(params.routeId && params.date),
  });
}

