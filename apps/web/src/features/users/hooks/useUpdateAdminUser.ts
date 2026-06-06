import { useMutation, useQueryClient } from "@tanstack/react-query";
import { updateAdminUser, type UpdateAdminUserInput } from "@/features/users/api";

export function useUpdateAdminUser() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (input: UpdateAdminUserInput) => updateAdminUser(input),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ["admin", "users"] });
    },
  });
}
