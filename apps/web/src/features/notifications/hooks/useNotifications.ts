import { useQuery } from "@tanstack/react-query";
import { getNotifications, type NotificationsQuery } from "@/features/notifications/api";

export function useNotifications(query: NotificationsQuery = {}) {
  return useQuery({
    queryKey: ["notifications", query.page ?? 0, query.size ?? 20],
    queryFn: () => getNotifications(query),
  });
}
