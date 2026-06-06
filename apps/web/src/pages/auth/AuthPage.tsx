import { Navigate } from "react-router-dom";
import { appRoutes } from "@/app/routes";
import { useAuth } from "@/features/auth/AuthProvider";
import { AdminSignInView } from "@/features/auth/components/AdminSignInView";

export function AuthPage() {
  const { isAdmin } = useAuth();

  if (isAdmin) {
    return <Navigate to={appRoutes.admin} replace />;
  }

  return <AdminSignInView />;
}
