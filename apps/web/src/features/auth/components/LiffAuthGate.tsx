import { PropsWithChildren, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { appRoutes } from "@/app/routes";
import { useAuth } from "@/features/auth/AuthProvider";
import { login } from "@/features/auth/api";
import { authenticateWithLine } from "@/features/auth/liff-api";
import { env } from "@/lib/env";
import { bootstrapLiff, reloginWithLine } from "@/lib/liff";

function isExpiredTokenMessage(error: unknown) {
  const message = error instanceof Error ? error.message : String(error || "");
  const normalized = message.toLowerCase();
  return normalized.includes("expired") && normalized.includes("line");
}

export function LiffAuthGate({ children }: PropsWithChildren) {
  const { isAuthenticated, setSession } = useAuth();
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    if (isAuthenticated) return;

    let cancelled = false;

    async function authenticate() {
      if (env.devBypassAuth && env.devAuthEmail && env.devAuthPassword) {
        setIsLoading(true);
        try {
          const session = await login({ email: env.devAuthEmail, password: env.devAuthPassword });
          setSession(session);
        } catch (authError) {
          setError(authError instanceof Error ? authError.message : "Dev login failed");
        } finally {
          setIsLoading(false);
        }
        return;
      }

      if (!env.liffId) {
        setError("LINE login is not configured yet.");
        return;
      }

      setIsLoading(true);
      setError("");

      try {
        const result = await bootstrapLiff(env.liffId);
        if (!result) return;

        const session = await authenticateWithLine({
          idToken: result.idToken,
        });

        if (!cancelled) {
          setSession(session);
        }
      } catch (authError) {
        if (isExpiredTokenMessage(authError)) {
          reloginWithLine();
          return;
        }

        if (!cancelled) {
          setError(authError instanceof Error ? authError.message : "LINE authentication failed");
        }
      } finally {
        if (!cancelled) {
          setIsLoading(false);
        }
      }
    }

    void authenticate();

    return () => {
      cancelled = true;
    };
  }, [isAuthenticated, setSession]);

  if (isAuthenticated) {
    return <>{children}</>;
  }

  return (
    <div className="px-4 py-6">
      <div className="rounded-2xl border border-[#d4dcfb] bg-white px-5 py-6 text-center shadow-[0_10px_24px_rgba(41,68,178,0.08)]">
        <div className="mx-auto mb-3 h-11 w-11 rounded-full border border-[#cfdaff] bg-gradient-to-br from-[#f0f4ff] to-[#e5ecff] p-2.5">
          <div className="h-full w-full animate-spin rounded-full border-2 border-[#b9c8ff] border-t-[#4f62d3]" />
        </div>
        <p className="text-base font-semibold text-[#1f2f8d]">Signing in with LINE</p>
        <p className="mt-1 text-xs text-[#6674b0]">
          {isLoading ? "Refreshing your LIFF session..." : "Waiting for authentication..."}
        </p>

        {error ? (
          <>
            <p className="mt-2 rounded-lg border border-amber-200 bg-amber-50 px-2 py-1.5 text-[11px] text-amber-700">
              {error}
            </p>

            <button
              type="button"
              onClick={reloginWithLine}
              className="mt-4 h-9 w-full rounded-lg bg-[#3f53c9] text-[12px] font-semibold text-white hover:bg-[#3447b4]"
            >
              Retry LINE Login
            </button>

            <div className="mt-2">
              <Link to={appRoutes.auth} className="text-[11px] font-medium text-[#5d6ec6]">
                Use web login (fallback/admin)
              </Link>
            </div>
          </>
        ) : null}
      </div>
    </div>
  );
}

