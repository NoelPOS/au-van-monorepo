import { env } from "@/lib/env";
import type { ApiEnvelope } from "@/types/api";

type ApiOptions = RequestInit & {
  query?: Record<string, string | number | boolean | undefined>;
};

function buildUrl(path: string, query?: ApiOptions["query"]) {
  const url = new URL(path, env.apiBaseUrl);
  if (query) {
    Object.entries(query).forEach(([key, value]) => {
      if (value === undefined || value === null || value === "") return;
      url.searchParams.set(key, String(value));
    });
  }
  return url.toString();
}

function readAccessToken() {
  if (typeof window === "undefined") return "";
  return window.localStorage.getItem("auvan_access_token") || "";
}

export async function apiRequest<T>(path: string, options: ApiOptions = {}): Promise<T> {
  const accessToken = readAccessToken();

  const response = await fetch(buildUrl(path, options.query), {
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...(accessToken ? { Authorization: `Bearer ${accessToken}` } : {}),
      ...(options.headers ?? {}),
    },
  });

  const payload = (await response.json().catch(() => null)) as ApiEnvelope<T> | null;

  if (!response.ok || !payload?.success) {
    throw new Error(payload?.error || payload?.message || "Request failed");
  }

  return payload.data;
}
