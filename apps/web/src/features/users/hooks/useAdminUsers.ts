import { useQuery } from "@tanstack/react-query";
import { getAdminUsers } from "@/features/users/api";

export function useAdminUsers(page = 0, size = 50) {
  return useQuery({
    queryKey: ["admin", "users", page, size],
    queryFn: () => getAdminUsers(page, size),
  });
}
