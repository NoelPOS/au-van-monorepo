import { useQuery } from "@tanstack/react-query";
import { getMyProfile } from "@/features/profile/api";

export function useMyProfile() {
  return useQuery({
    queryKey: ["profile", "me"],
    queryFn: getMyProfile,
  });
}
