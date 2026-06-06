import { apiRequest } from "@/api/client";
import type { PageResponse } from "@/types/api";
import type { AuthUser } from "@/types/auth";

export type UpdateAdminUserInput = {
  userId: string;
  name: string;
  phone: string;
  defaultPickupLocation: string;
  profileImageUrl: string;
  isAdmin: boolean;
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

export function getAdminUsers(page = 0, size = 50) {
  return apiRequest<PageResponse<UserDto>>("/api/admin/users", {
    query: {
      page,
      size,
    },
  }).then((pageResponse) => ({
    ...pageResponse,
    content: pageResponse.content.map(mapUser),
  }));
}

export function updateAdminUser(input: UpdateAdminUserInput) {
  return apiRequest<UserDto>(`/api/admin/users/${input.userId}`, {
    method: "PUT",
    body: JSON.stringify({
      name: input.name,
      phone: input.phone,
      defaultPickupLocation: input.defaultPickupLocation,
      profileImageUrl: input.profileImageUrl || undefined,
      isAdmin: input.isAdmin,
    }),
  }).then(mapUser);
}
