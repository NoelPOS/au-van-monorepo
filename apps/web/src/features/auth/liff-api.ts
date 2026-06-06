import { apiRequest } from "@/api/client";
import type { AuthSession } from "@/types/auth";

type LiffAuthPayload = {
  idToken: string;
  phone?: string;
};

export function authenticateWithLine(payload: LiffAuthPayload) {
  return apiRequest<AuthSession>("/api/liff/auth/line", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

