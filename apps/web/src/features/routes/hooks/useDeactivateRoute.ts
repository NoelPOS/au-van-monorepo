import { routeKeys } from "@/lib/queryKeys";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { deactivateRoute } from "@/features/routes/api";

export function useDeactivateRoute() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (routeId: string) => deactivateRoute(routeId),
    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: routeKeys.admin() }),
        queryClient.invalidateQueries({ queryKey: routeKeys.all() }),
      ]);
    },
  });
}
