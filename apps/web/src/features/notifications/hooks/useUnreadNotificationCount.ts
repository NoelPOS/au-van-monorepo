import { useQuery } from "@tanstack/react-query";
import { getUnreadNotificationCount } from "@/features/notifications/api";

export function useUnreadNotificationCount() {
  return useQuery({
    queryKey: ["notifications", "unread-count"],
    queryFn: getUnreadNotificationCount,
  });
}
