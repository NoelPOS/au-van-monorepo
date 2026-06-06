import { apiRequest } from "@/api/client";
import type { AuthUser } from "@/types/auth";

export type UpdateProfileInput = {
  name: string;
  phone: string;
  defaultPickupLocation: string;
};

type UserDto = {
  id: string;
  email?: string | null;
  lineUserId?: string | null;
  authProvider?: string;
  displayName?: string | null;
  name?: string | null;
  phone?: string | null;
  defaultPickupLocation?: string | null;
  profileImageUrl?: string | null;
  isAdmin: boolean;
  createdAt?: string;
};

function mapUser(dto: UserDto): AuthUser {
  return {
    id: dto.id,
    email: dto.email,
    lineUserId: dto.lineUserId,
    authProvider: dto.authProvider,
    displayName: dto.displayName,
    name: dto.name,
    phone: dto.phone,
    defaultPickupLocation: dto.defaultPickupLocation,
    profileImageUrl: dto.profileImageUrl,
    isAdmin: dto.isAdmin,
    createdAt: dto.createdAt,
  };
}

export function getMyProfile() {
  return apiRequest<UserDto>("/api/liff/users/me").then(mapUser);
}

export function updateMyProfile(input: UpdateProfileInput) {
  return apiRequest<UserDto>("/api/liff/users/me", {
    method: "PUT",
    body: JSON.stringify(input),
  }).then(mapUser);
}
