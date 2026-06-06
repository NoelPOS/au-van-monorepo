import { routeKeys } from "@/lib/queryKeys";
import { useQuery } from "@tanstack/react-query";
import { getRoutes } from "@/features/routes/api";

export function useRoutes() {
  return useQuery({
    queryKey: routeKeys.all(),
    queryFn: getRoutes,
  });
}

