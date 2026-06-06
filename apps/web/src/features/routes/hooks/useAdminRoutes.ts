import { routeKeys } from "@/lib/queryKeys";
import { useQuery } from "@tanstack/react-query";
import { getAdminRoutes } from "@/features/routes/api";

export function useAdminRoutes() {
  return useQuery({
    queryKey: routeKeys.admin(),
    queryFn: getAdminRoutes,
  });
}
