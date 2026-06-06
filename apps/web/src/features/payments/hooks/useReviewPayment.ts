import { useMutation, useQueryClient } from "@tanstack/react-query";
import { reviewPayment, type ReviewPaymentInput } from "@/features/payments/api";

export function useReviewPayment() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (input: ReviewPaymentInput) => reviewPayment(input),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ["admin", "payments"] });
    },
  });
}
