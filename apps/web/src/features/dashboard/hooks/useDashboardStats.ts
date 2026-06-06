import { useQuery } from "@tanstack/react-query";
import { getDashboardStats } from "@/features/dashboard/api";

export function useDashboardStats() {
  return useQuery({
    queryKey: ["admin", "dashboard", "stats"],
    queryFn: getDashboardStats,
  });
}
