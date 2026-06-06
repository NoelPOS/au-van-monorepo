import { createContext, PropsWithChildren, useContext, useMemo, useState } from "react";
import { clearStoredSession, readStoredSession, storeSession } from "@/lib/auth-storage";
import type { AuthSession, AuthUser } from "@/types/auth";

type AuthContextValue = {
  session: AuthSession | null;
  isAuthenticated: boolean;
  isAdmin: boolean;
  setSession: (session: AuthSession) => void;
  updateSessionUser: (user: AuthUser) => void;
  signOut: () => void;
};

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: PropsWithChildren) {
  const [session, setSessionState] = useState<AuthSession | null>(() => readStoredSession());

  const value = useMemo<AuthContextValue>(
    () => ({
      session,
      isAuthenticated: Boolean(session?.token),
      isAdmin: Boolean(session?.user.isAdmin),
      setSession(nextSession) {
        storeSession(nextSession);
        setSessionState(nextSession);
      },
      updateSessionUser(nextUser) {
        setSessionState((currentSession) => {
          if (!currentSession) return currentSession;

          const nextSession = {
            ...currentSession,
            user: nextUser,
          };

          storeSession(nextSession);
          return nextSession;
        });
      },
      signOut() {
        clearStoredSession();
        setSessionState(null);
      },
    }),
    [session]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);

  if (!context) {
    throw new Error("useAuth must be used within AuthProvider");
  }

  return context;
}
