import { routeKeys } from "@/lib/queryKeys";
import { useQuery } from "@tanstack/react-query";
import { getRouteById } from "@/features/routes/api";

export function useRoute(routeId: string) {
  return useQuery({
    queryKey: routeKeys.detail(routeId),
    queryFn: () => getRouteById(routeId),
    enabled: Boolean(routeId),
  });
}

