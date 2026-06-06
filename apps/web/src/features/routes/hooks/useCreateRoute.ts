import { routeKeys } from "@/lib/queryKeys";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { createRoute, type CreateRouteInput } from "@/features/routes/api";

export function useCreateRoute() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (input: CreateRouteInput) => createRoute(input),
    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: routeKeys.admin() }),
        queryClient.invalidateQueries({ queryKey: routeKeys.all() }),
      ]);
    },
  });
}
