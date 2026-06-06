import { useAuth } from "@/features/auth/AuthProvider";
import { Navigate, Outlet } from "react-router-dom";
import { appRoutes } from "@/app/routes";
import { AdminNavbar } from "@/components/layout/AdminNavbar";

export function AdminLayout() {
  const { isAdmin } = useAuth();

  if (!isAdmin) {
    return <Navigate to={appRoutes.auth} replace />;
  }

  return (
    <div className="min-h-screen bg-background">
      <AdminNavbar />
      <main className="mx-auto max-w-6xl px-6 py-8">
        <Outlet />
      </main>
    </div>
  );
}
