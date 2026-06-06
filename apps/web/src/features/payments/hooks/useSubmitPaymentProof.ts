import { useMutation, useQueryClient } from "@tanstack/react-query";
import {
  submitPaymentProof,
  type SubmitPaymentProofInput,
} from "@/features/payments/api";

export function useSubmitPaymentProof() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (input: SubmitPaymentProofInput) => submitPaymentProof(input),
    onSuccess: async (_, variables) => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ["bookings", "me"] }),
        queryClient.invalidateQueries({ queryKey: ["bookings", variables.bookingId] }),
      ]);
    },
  });
}
