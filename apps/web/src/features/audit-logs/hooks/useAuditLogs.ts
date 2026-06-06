import { useQuery } from "@tanstack/react-query";
import { getAuditLogs } from "@/features/audit-logs/api";

export function useAuditLogs(page = 0, size = 20) {
  return useQuery({
    queryKey: ["admin", "audit-logs", page, size],
    queryFn: () => getAuditLogs(page, size),
  });
}
