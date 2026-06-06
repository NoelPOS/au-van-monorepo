import { routeKeys } from "@/lib/queryKeys";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { updateRoute, type UpdateRouteInput } from "@/features/routes/api";

export function useUpdateRoute() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (input: UpdateRouteInput) => updateRoute(input),
    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: routeKeys.admin() }),
        queryClient.invalidateQueries({ queryKey: routeKeys.all() }),
      ]);
    },
  });
}
