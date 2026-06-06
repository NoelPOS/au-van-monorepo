import { useQuery } from "@tanstack/react-query";
import { getAdminPayments, type AdminPaymentsQuery } from "@/features/payments/api";

export function useAdminPayments(query: AdminPaymentsQuery = {}) {
  return useQuery({
    queryKey: ["admin", "payments", query.page ?? 0, query.size ?? 20],
    queryFn: () => getAdminPayments(query),
  });
}
