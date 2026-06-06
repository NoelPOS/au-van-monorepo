import { apiRequest } from "@/api/client";
import type { AuthSession } from "@/types/auth";

type LoginPayload = {
  email: string;
  password: string;
};

export function login(payload: LoginPayload) {
  return apiRequest<AuthSession>("/api/auth/login", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

